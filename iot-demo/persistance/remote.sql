CREATE USER fuse IDENTIFIED BY sqlsql
go

-------------------------------------------------
--   Create tables
-------------------------------------------------

CREATE TABLE "dba"."Customers" (
    "id"                             integer NOT NULL
   ,"name"                           long varchar NOT NULL
   ,"lastVisit"                      bigint NULL
   ,"averagePurchaseAmount"          numeric(6,2) NULL
   ,PRIMARY KEY ("id" ASC) 
)
go

CREATE TABLE "dba"."CustomerMovements" (
    "id"                             integer NOT NULL DEFAULT global autoincrement(100000)
   ,"customerID"                     integer NOT NULL
   ,"locationX"                      integer NOT NULL
   ,"locationY"                      integer NOT NULL
   ,"ts"                             timestamp NOT NULL
   ,PRIMARY KEY ("id" ASC) 
)
go

CREATE TABLE "dba"."CustomerDepartments" (
    "id"                             integer NOT NULL DEFAULT global autoincrement(100000)
   ,"customerID"                     integer NOT NULL
   ,"departmentID"                   integer NOT NULL
   ,"ts"                             timestamp NOT NULL
   ,PRIMARY KEY ("id" ASC) 
)
go

commit work
go

-------------------------------------------------
--   Create foreign keys
-------------------------------------------------

ALTER TABLE "dba"."CustomerMovements"
    ADD FOREIGN KEY "Customers" ("customerID" ASC)
    REFERENCES "dba"."Customers" ("id")
    
go

ALTER TABLE "dba"."CustomerDepartments"
    ADD FOREIGN KEY "Customers" ("customerID" ASC)
    REFERENCES "dba"."Customers" ("id")
    
go

commit work
go



-------------------------------------------------
--   Create functions
-------------------------------------------------

commit
go

create function "dba"."epochToTS"( in "secondsSinceEpoch" bigint ) 
returns "DATETIME"
begin
  return "DATEADD"("second","secondsSinceEpoch"/1000,'1970/01/01')
end
go

create function "dba"."GlobalINIVariables"( in "iniString" long varchar ) 
returns integer
begin
  declare "splitPoint" integer;
  set "splitPoint" = "CHARINDEX"('=',"iniString");
  execute immediate 'CREATE OR REPLACE DATABASE VARIABLE $' || "SUBSTRING"("iniString",1,"splitPoint"-1) || ' LONG VARCHAR = ''' || "SUBSTRING"("iniString","splitPoint"+1) || '''';
  return 0
end
go

-------------------------------------------------
--   Create procedures
-------------------------------------------------

commit
go


create procedure "dba"."sp_CustomerDepartment"()
begin
  insert into "CustomerDepartments"( "customerID","departmentID","ts" ) 
    select "CustomerID","DepartmentID","epochToTS"("TimeStamp") as "ts"
      from(select *
          from openxml("HTTP_BODY"(),'/CustomerDepartmentEvent') with("CustomerID" long varchar
            'CustomerID',"DepartmentID" integer
            'DepartmentID',"TimeStamp" bigint
            'TimeStamp')) as "dt";
  commit work
end
go

GRANT EXECUTE ON "dba"."sp_CustomerDepartment" TO "fuse"
go

create procedure "dba"."sp_CustomerMovement"()
begin
  insert into "CustomerMovements"( "customerID","locationX","locationY","ts" ) 
    select "CustomerID","LocationX","LocationY","epochToTS"("TimeStamp") as "ts"
      from(select *
          from openxml("HTTP_BODY"(),'/CustomerMovementEvent') with("CustomerID" long varchar
            'CustomerID',"LocationX" integer
            'Location/X',"LocationY" integer
            'Location/Y',"TimeStamp" bigint
            'TimeStamp')) as "dt";
  commit work
end
go

GRANT EXECUTE ON "dba"."sp_CustomerMovement" TO "fuse"
go

create procedure "dba"."sp_CustomerClassification"()
begin
  declare @customerID long varchar;
  declare @customerType integer;
  declare @salesNotificationEventBody long varchar;
  select "CustomerID","CustomerType" into @customerID,@customerType
    from openxml("HTTP_BODY"(),'/CustomerClassificationEvent') with("CustomerID" long varchar
      'CustomerID',"CustomerType" integer
      'CustomerTypeID');
  IF (SELECT AveragePurchaseAmount FROM Customers WHERE id = @customerID) > 180 THEN
    set @salesNotificationEventBody
       = (select 1 as "tag",
        null as "parent",
        @customerID as "SalesNotificationEvent!1!CustomerID!element",
        "name" as "SalesNotificationEvent!1!Name!element",
        "lastVisit" as "SalesNotificationEvent!1!LastVisit!element",
        '$' || "averagePurchaseAmount" as "SalesNotificationEvent!1!AveragePurchaseAmount!element",
        "DATEDIFF"("second",'1970/01/01',"NOW"()) as "SalesNotificationEvent!1!TimeStamp!element"
        from "Customers" where "id" = @customerID for xml explicit);
    select "Value" from "SalesNotification"("$fuse_host","$fuse_port",@salesNotificationEventBody) where "Attribute" = 'Status'
  else
    select null
  end if
end
go

GRANT EXECUTE ON "dba"."sp_CustomerClassification" TO "fuse"
go

create procedure "dba"."SalesNotification"( in "host" long varchar,in "port" long varchar,in "postBody" long varchar ) 
result( "Attribute" long varchar,"Value" long varchar ) 
url 'http://!host:!port/sales/notification'
type 'HTTP:POST:text/xml'
go


CREATE EVENT "dba"."ReadINIFile" TYPE "DatabaseStart"  ENABLE 
HANDLER
begin  
  declare filePath long varchar = SUBSTR(db_property('File'), 0, LENGTH(db_property('File')) - 1) || 'cfg'; 


  declare disappear ARRAY OF ROW(res INTEGER);
  select ARRAY_AGG(ROW("GlobalINIVariables"("row_value"))) INTO disappear from "sa_split_list"("xp_read_file"(filePath),'\x0A') where "CHARINDEX"('=',"row_value") > 0;
end
go

TRIGGER EVENT ReadINIFile
go

-------------------------------------------------
--   Create services
-------------------------------------------------

commit
go

CREATE SERVICE "customer/department" 
    TYPE 'RAW' AUTHORIZATION OFF SECURE OFF URL PATH OFF USER "fuse" USING "METHODS=POST"  AS
call "dba"."sp_CustomerDepartment"()
go

CREATE SERVICE "customer/classification" 
    TYPE 'RAW' AUTHORIZATION OFF SECURE OFF URL PATH OFF USER "fuse" USING "METHODS=POST"  AS
call "dba"."sp_CustomerClassification"()
go

CREATE SERVICE "customer/movement" 
    TYPE 'RAW' AUTHORIZATION OFF SECURE OFF URL PATH OFF USER "fuse" USING "METHODS=POST"  AS
call "dba"."sp_CustomerMovement"()
go

CREATE PUBLICATION store_demo (
  TABLE Customers, TABLE CustomerDepartments, TABLE CustomerMovements
)
go

CREATE SYNCHRONIZATION USER "remote" TYPE tcpip
go

CREATE SYNCHRONIZATION SUBSCRIPTION TO store_demo FOR "remote"
go

