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

DROP TABLE  "dba"."Product"
go

DROP TABLE  "dba"."Department"
go

CREATE TABLE  "dba"."Department" (
  "departmentCode" long varchar  NOT NULL
  ,"departmentName" long varchar  NOT NULL
  ,"departmentDescription" text NOT NULL
  ,"dimension" st_geometry  NOT NULL
  ,PRIMARY KEY ("departmentCode" ASC)
)
go

CREATE TABLE  "dba"."Product" (
  "productCode" long varchar  NOT NULL
  ,"productName" long varchar  NOT NULL
  ,"productSize" long varchar  NOT NULL
  ,"productVendor" long varchar  NOT NULL
  ,"productDescription" text NOT NULL
  ,"buyPrice" numeric(6,2) NOT NULL
  ,"MSRP"  numeric(6,2) NOT NULL
  ,"departmentCode"   long varchar  NOT NULL
  ,PRIMARY KEY ("productCode" ASC)
)
go

CREATE TABLE  "dba"."Inventory" (
  "storeId" long varchar  NOT NULL
  ,"productCode" long varchar  NOT NULL
  ,"quantity" long varchar  NOT NULL
  ,PRIMARY KEY ( "storeId", "productCode" )
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

ALTER TABLE "dba"."Product"
    ADD FOREIGN KEY (dept) 
    REFERENCES "dba"."Department"(departmentCode)
go

ALTER TABLE "dba"."Inventory"
    ADD FOREIGN KEY (product) 
    REFERENCES "dba"."Product"(productCode)
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
  TABLE Customers, TABLE CustomerDepartments, TABLE CustomerMovements, TABLE Product, TABLE Department
)
go

CREATE SYNCHRONIZATION USER "remote" TYPE tcpip
go

CREATE SYNCHRONIZATION SUBSCRIPTION TO store_demo FOR "remote"
go

-------------------------------------------------
--   Create data
-------------------------------------------------

/*Data for department table */

insert into "Department" ("departmentCode","departmentName","departmentDescription","dimension")
values('1000','Womans','Womans clothing and assessories.',ST_Geometry::ST_GeomFromWKT('POLYGON((35 31, 35 57, 181 57, 181 31, 35 31))'))
insert into "Department" ("departmentCode","departmentName","departmentDescription","dimension")
values('1001','Boys','Boys clothing and assessories.',ST_Geometry::ST_GeomFromWKT('POLYGON((31 99, 31 160, 90 160, 90 99, 31 99))'))
insert into "Department" ("departmentCode","departmentName","departmentDescription","dimension")
values('1002','Girls','Girls clothing and assessories.',ST_Geometry::ST_GeomFromWKT('POLYGON((110 99, 110 160, 171 161, 171 99, 110 99))'))
insert into "Department" ("departmentCode","departmentName","departmentDescription","dimension")
values('1003','Mens','Mens clothing and assessories.',ST_Geometry::ST_GeomFromWKT('POLYGON((197 132, 198 159, 268 159, 269 134, 197 132))'))
insert into "Department" ("departmentCode","departmentName","departmentDescription","dimension")
values('1004','Formal','Formal wear for men and women.',ST_Geometry::ST_GeomFromWKT('POLYGON((206 27, 206 102, 230 102, 228 27, 206 27))'))
insert into "Department" ("departmentCode","departmentName","departmentDescription","dimension")
values('1005','Sport','Sports wear for men and women.',ST_Geometry::ST_GeomFromWKT('POLYGON((242 27, 242 103, 265 103, 264 27, 242 27))'))

/* Data for Product table */

INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1000, 'Bathing suit', 'Kids', 'Bellerose', 'A Bathing suit by manufacturer Bellerose', 48.00, 63.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1001, 'Bathing suit', 'Large', 'Bellerose', 'A Bathing suit by manufacturer Bellerose', 48.00, 63.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1002, 'Bathing suit', 'Medium', 'Bellerose', 'A Bathing suit by manufacturer Bellerose', 48.00, 63.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1003, 'Bathing suit', 'Petite', 'Bellerose', 'A Bathing suit by manufacturer Bellerose', 48.00, 63.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1004, 'Bathing suit', 'Small', 'Bellerose', 'A Bathing suit by manufacturer Bellerose', 48.00, 63.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1005, 'Bathing suit', 'X-Large', 'Bellerose', 'A Bathing suit by manufacturer Bellerose', 48.00, 63.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1006, 'Bike short', 'Kids', 'Adidas', 'A Bike short by manufacturer Adidas', 47.00, 85.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1007, 'Bike short', 'Large', 'Adidas', 'A Bike short by manufacturer Adidas', 47.00, 85.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1008, 'Bike short', 'Medium', 'Adidas', 'A Bike short by manufacturer Adidas', 47.00, 85.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1009, 'Bike short', 'Petite', 'Adidas', 'A Bike short by manufacturer Adidas', 47.00, 85.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1010, 'Bike short', 'Small', 'Adidas', 'A Bike short by manufacturer Adidas', 47.00, 85.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1011, 'Bike short', 'X-Large', 'Adidas', 'A Bike short by manufacturer Adidas', 47.00, 85.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1012, 'Sport briefs', 'Kids', 'Polo', 'A Sport briefs by manufacturer Polo', 15.00, 24.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1013, 'Sport briefs', 'Large', 'Polo', 'A Sport briefs by manufacturer Polo', 15.00, 24.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1014, 'Sport briefs', 'Medium', 'Polo', 'A Sport briefs by manufacturer Polo', 15.00, 24.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1015, 'Sport briefs', 'Petite', 'Polo', 'A Sport briefs by manufacturer Polo', 15.00, 24.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1016, 'Sport briefs', 'Small', 'Polo', 'A Sport briefs by manufacturer Polo', 15.00, 24.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1017, 'Sport briefs', 'X-Large', 'Polo', 'A Sport briefs by manufacturer Polo', 15.00, 24.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1018, 'Rain jacket', 'Kids', 'Hugo Boss', 'A Rain jacket by manufacturer Hugo Boss', 24.00, 85.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1019, 'Rain jacket', 'Large', 'Hugo Boss', 'A Rain jacket by manufacturer Hugo Boss', 24.00, 85.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1020, 'Rain jacket', 'Medium', 'Hugo Boss', 'A Rain jacket by manufacturer Hugo Boss', 24.00, 85.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1021, 'Rain jacket', 'Petite', 'Hugo Boss', 'A Rain jacket by manufacturer Hugo Boss', 24.00, 85.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1022, 'Rain jacket', 'Small', 'Hugo Boss', 'A Rain jacket by manufacturer Hugo Boss', 24.00, 85.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1023, 'Rain jacket', 'X-Large', 'Hugo Boss', 'A Rain jacket by manufacturer Hugo Boss', 24.00, 85.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1024, 'Ballet skirt', 'Kids', 'Nununu', 'A Ballet skirt by manufacturer Nununu', 58.00, 46.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1025, 'Ballet skirt', 'Large', 'Nununu', 'A Ballet skirt by manufacturer Nununu', 58.00, 46.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1026, 'Ballet skirt', 'Medium', 'Nununu', 'A Ballet skirt by manufacturer Nununu', 58.00, 46.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1027, 'Ballet skirt', 'Petite', 'Nununu', 'A Ballet skirt by manufacturer Nununu', 58.00, 46.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1028, 'Ballet skirt', 'Small', 'Nununu', 'A Ballet skirt by manufacturer Nununu', 58.00, 46.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1029, 'Ballet skirt', 'X-Large', 'Nununu', 'A Ballet skirt by manufacturer Nununu', 58.00, 46.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1030, 'Tank top', 'Kids', 'Gap', 'A Tank top by manufacturer Gap', 36.00, 87.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1031, 'Tank top', 'Large', 'Gap', 'A Tank top by manufacturer Gap', 36.00, 87.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1032, 'Tank top', 'Medium', 'Gap', 'A Tank top by manufacturer Gap', 36.00, 87.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1033, 'Tank top', 'Petite', 'Gap', 'A Tank top by manufacturer Gap', 36.00, 87.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1034, 'Tank top', 'Small', 'Gap', 'A Tank top by manufacturer Gap', 36.00, 87.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1035, 'Tank top', 'X-Large', 'Gap', 'A Tank top by manufacturer Gap', 36.00, 87.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1036, 'Wool hat', 'Kids', 'Puma', 'A Wool hat by manufacturer Puma', 60.00, 26.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1037, 'Wool hat', 'Large', 'Puma', 'A Wool hat by manufacturer Puma', 60.00, 26.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1038, 'Wool hat', 'Medium', 'Puma', 'A Wool hat by manufacturer Puma', 60.00, 26.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1039, 'Wool hat', 'Petite', 'Puma', 'A Wool hat by manufacturer Puma', 60.00, 26.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1040, 'Wool hat', 'Small', 'Puma', 'A Wool hat by manufacturer Puma', 60.00, 26.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1041, 'Wool hat', 'X-Large', 'Puma', 'A Wool hat by manufacturer Puma', 60.00, 26.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1042, 'Sweat pants', 'Kids', 'J.Crew', 'A Sweat pants by manufacturer J.Crew', 54.00, 63.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1043, 'Sweat pants', 'Large', 'J.Crew', 'A Sweat pants by manufacturer J.Crew', 54.00, 63.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1044, 'Sweat pants', 'Medium', 'J.Crew', 'A Sweat pants by manufacturer J.Crew', 54.00, 63.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1045, 'Sweat pants', 'Petite', 'J.Crew', 'A Sweat pants by manufacturer J.Crew', 54.00, 63.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1046, 'Sweat pants', 'Small', 'J.Crew', 'A Sweat pants by manufacturer J.Crew', 54.00, 63.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1047, 'Sweat pants', 'X-Large', 'J.Crew', 'A Sweat pants by manufacturer J.Crew', 54.00, 63.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1048, 'Dress pants', 'Kids', 'Hugo Boss', 'A Dress pants by manufacturer Hugo Boss', 18.00, 96.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1049, 'Dress pants', 'Large', 'Hugo Boss', 'A Dress pants by manufacturer Hugo Boss', 18.00, 96.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1050, 'Dress pants', 'Medium', 'Hugo Boss', 'A Dress pants by manufacturer Hugo Boss', 18.00, 96.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1051, 'Dress pants', 'Petite', 'Hugo Boss', 'A Dress pants by manufacturer Hugo Boss', 18.00, 96.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1052, 'Dress pants', 'Small', 'Hugo Boss', 'A Dress pants by manufacturer Hugo Boss', 18.00, 96.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1053, 'Dress pants', 'X-Large', 'Hugo Boss', 'A Dress pants by manufacturer Hugo Boss', 18.00, 96.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1054, 'Beach sling', 'Kids', 'Fred Perry', 'A Beach sling by manufacturer Fred Perry', 70.00, 12.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1055, 'Beach sling', 'Large', 'Fred Perry', 'A Beach sling by manufacturer Fred Perry', 70.00, 12.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1056, 'Beach sling', 'Medium', 'Fred Perry', 'A Beach sling by manufacturer Fred Perry', 70.00, 12.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1057, 'Beach sling', 'Petite', 'Fred Perry', 'A Beach sling by manufacturer Fred Perry', 70.00, 12.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1058, 'Beach sling', 'Small', 'Fred Perry', 'A Beach sling by manufacturer Fred Perry', 70.00, 12.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1059, 'Beach sling', 'X-Large', 'Fred Perry', 'A Beach sling by manufacturer Fred Perry', 70.00, 12.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1060, 'Denim cut-offs', 'Kids', 'Aeropostale', 'A Denim cut-offs by manufacturer Aeropostale', 43.00, 81.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1061, 'Denim cut-offs', 'Large', 'Aeropostale', 'A Denim cut-offs by manufacturer Aeropostale', 43.00, 81.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1062, 'Denim cut-offs', 'Medium', 'Aeropostale', 'A Denim cut-offs by manufacturer Aeropostale', 43.00, 81.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1063, 'Denim cut-offs', 'Petite', 'Aeropostale', 'A Denim cut-offs by manufacturer Aeropostale', 43.00, 81.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1064, 'Denim cut-offs', 'Small', 'Aeropostale', 'A Denim cut-offs by manufacturer Aeropostale', 43.00, 81.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1065, 'Denim cut-offs', 'X-Large', 'Aeropostale', 'A Denim cut-offs by manufacturer Aeropostale', 43.00, 81.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1066, 'Short sleeve Henley', 'Kids', 'Fred Perry', 'A Short sleeve Henley by manufacturer Fred Perry', 5.00, 39.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1067, 'Short sleeve Henley', 'Large', 'Fred Perry', 'A Short sleeve Henley by manufacturer Fred Perry', 5.00, 39.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1068, 'Short sleeve Henley', 'Medium', 'Fred Perry', 'A Short sleeve Henley by manufacturer Fred Perry', 5.00, 39.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1069, 'Short sleeve Henley', 'Petite', 'Fred Perry', 'A Short sleeve Henley by manufacturer Fred Perry', 5.00, 39.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1070, 'Short sleeve Henley', 'Small', 'Fred Perry', 'A Short sleeve Henley by manufacturer Fred Perry', 5.00, 39.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1071, 'Short sleeve Henley', 'X-Large', 'Fred Perry', 'A Short sleeve Henley by manufacturer Fred Perry', 5.00, 39.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1072, 'Short sleeve polo', 'Kids', 'Guess', 'A Short sleeve polo by manufacturer Guess', 61.00, 90.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1073, 'Short sleeve polo', 'Large', 'Guess', 'A Short sleeve polo by manufacturer Guess', 61.00, 90.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1074, 'Short sleeve polo', 'Medium', 'Guess', 'A Short sleeve polo by manufacturer Guess', 61.00, 90.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1075, 'Short sleeve polo', 'Petite', 'Guess', 'A Short sleeve polo by manufacturer Guess', 61.00, 90.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1076, 'Short sleeve polo', 'Small', 'Guess', 'A Short sleeve polo by manufacturer Guess', 61.00, 90.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1077, 'Short sleeve polo', 'X-Large', 'Guess', 'A Short sleeve polo by manufacturer Guess', 61.00, 90.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1078, 'Denim cut-offs', 'Kids', 'Nike', 'A Denim cut-offs by manufacturer Nike', 93.00, 19.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1079, 'Denim cut-offs', 'Large', 'Nike', 'A Denim cut-offs by manufacturer Nike', 93.00, 19.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1080, 'Denim cut-offs', 'Medium', 'Nike', 'A Denim cut-offs by manufacturer Nike', 93.00, 19.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1081, 'Denim cut-offs', 'Petite', 'Nike', 'A Denim cut-offs by manufacturer Nike', 93.00, 19.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1082, 'Denim cut-offs', 'Small', 'Nike', 'A Denim cut-offs by manufacturer Nike', 93.00, 19.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1083, 'Denim cut-offs', 'X-Large', 'Nike', 'A Denim cut-offs by manufacturer Nike', 93.00, 19.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1084, 'Overalls', 'Kids', 'Nike', 'A Overalls by manufacturer Nike', 22.00, 72.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1085, 'Overalls', 'Large', 'Nike', 'A Overalls by manufacturer Nike', 22.00, 72.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1086, 'Overalls', 'Medium', 'Nike', 'A Overalls by manufacturer Nike', 22.00, 72.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1087, 'Overalls', 'Petite', 'Nike', 'A Overalls by manufacturer Nike', 22.00, 72.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1088, 'Overalls', 'Small', 'Nike', 'A Overalls by manufacturer Nike', 22.00, 72.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1089, 'Overalls', 'X-Large', 'Nike', 'A Overalls by manufacturer Nike', 22.00, 72.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1090, 'V-neck t-shirt', 'Kids', 'Gymboree', 'A V-neck t-shirt by manufacturer Gymboree', 34.00, 18.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1091, 'V-neck t-shirt', 'Large', 'Gymboree', 'A V-neck t-shirt by manufacturer Gymboree', 34.00, 18.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1092, 'V-neck t-shirt', 'Medium', 'Gymboree', 'A V-neck t-shirt by manufacturer Gymboree', 34.00, 18.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1093, 'V-neck t-shirt', 'Petite', 'Gymboree', 'A V-neck t-shirt by manufacturer Gymboree', 34.00, 18.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1094, 'V-neck t-shirt', 'Small', 'Gymboree', 'A V-neck t-shirt by manufacturer Gymboree', 34.00, 18.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1095, 'V-neck t-shirt', 'X-Large', 'Gymboree', 'A V-neck t-shirt by manufacturer Gymboree', 34.00, 18.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1096, 'Bucket hat', 'Kids', 'Acrylick', 'A Bucket hat by manufacturer Acrylick', 76.00, 41.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1097, 'Bucket hat', 'Large', 'Acrylick', 'A Bucket hat by manufacturer Acrylick', 76.00, 41.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1098, 'Bucket hat', 'Medium', 'Acrylick', 'A Bucket hat by manufacturer Acrylick', 76.00, 41.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1099, 'Bucket hat', 'Petite', 'Acrylick', 'A Bucket hat by manufacturer Acrylick', 76.00, 41.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1100, 'Bucket hat', 'Small', 'Acrylick', 'A Bucket hat by manufacturer Acrylick', 76.00, 41.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1101, 'Bucket hat', 'X-Large', 'Acrylick', 'A Bucket hat by manufacturer Acrylick', 76.00, 41.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1102, 'Cotton oxford', 'Kids', 'Polo', 'A Cotton oxford by manufacturer Polo', 91.00, 71.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1103, 'Cotton oxford', 'Large', 'Polo', 'A Cotton oxford by manufacturer Polo', 91.00, 71.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1104, 'Cotton oxford', 'Medium', 'Polo', 'A Cotton oxford by manufacturer Polo', 91.00, 71.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1105, 'Cotton oxford', 'Petite', 'Polo', 'A Cotton oxford by manufacturer Polo', 91.00, 71.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1106, 'Cotton oxford', 'Small', 'Polo', 'A Cotton oxford by manufacturer Polo', 91.00, 71.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1107, 'Cotton oxford', 'X-Large', 'Polo', 'A Cotton oxford by manufacturer Polo', 91.00, 71.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1108, 'Beach sling', 'Kids', 'Bellerose', 'A Beach sling by manufacturer Bellerose', 85.00, 2.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1109, 'Beach sling', 'Large', 'Bellerose', 'A Beach sling by manufacturer Bellerose', 85.00, 2.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1110, 'Beach sling', 'Medium', 'Bellerose', 'A Beach sling by manufacturer Bellerose', 85.00, 2.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1111, 'Beach sling', 'Petite', 'Bellerose', 'A Beach sling by manufacturer Bellerose', 85.00, 2.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1112, 'Beach sling', 'Small', 'Bellerose', 'A Beach sling by manufacturer Bellerose', 85.00, 2.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1113, 'Beach sling', 'X-Large', 'Bellerose', 'A Beach sling by manufacturer Bellerose', 85.00, 2.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1114, 'Swim trunk', 'Kids', 'Prada', 'A Swim trunk by manufacturer Prada', 11.00, 53.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1115, 'Swim trunk', 'Large', 'Prada', 'A Swim trunk by manufacturer Prada', 11.00, 53.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1116, 'Swim trunk', 'Medium', 'Prada', 'A Swim trunk by manufacturer Prada', 11.00, 53.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1117, 'Swim trunk', 'Petite', 'Prada', 'A Swim trunk by manufacturer Prada', 11.00, 53.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1118, 'Swim trunk', 'Small', 'Prada', 'A Swim trunk by manufacturer Prada', 11.00, 53.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1119, 'Swim trunk', 'X-Large', 'Prada', 'A Swim trunk by manufacturer Prada', 11.00, 53.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1120, 'Beach sling', 'Kids', 'Lacoste', 'A Beach sling by manufacturer Lacoste', 12.00, 62.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1121, 'Beach sling', 'Large', 'Lacoste', 'A Beach sling by manufacturer Lacoste', 12.00, 62.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1122, 'Beach sling', 'Medium', 'Lacoste', 'A Beach sling by manufacturer Lacoste', 12.00, 62.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1123, 'Beach sling', 'Petite', 'Lacoste', 'A Beach sling by manufacturer Lacoste', 12.00, 62.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1124, 'Beach sling', 'Small', 'Lacoste', 'A Beach sling by manufacturer Lacoste', 12.00, 62.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1125, 'Beach sling', 'X-Large', 'Lacoste', 'A Beach sling by manufacturer Lacoste', 12.00, 62.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1126, 'Cotton oxford', 'Kids', 'J.Crew', 'A Cotton oxford by manufacturer J.Crew', 24.00, 86.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1127, 'Cotton oxford', 'Large', 'J.Crew', 'A Cotton oxford by manufacturer J.Crew', 24.00, 86.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1128, 'Cotton oxford', 'Medium', 'J.Crew', 'A Cotton oxford by manufacturer J.Crew', 24.00, 86.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1129, 'Cotton oxford', 'Petite', 'J.Crew', 'A Cotton oxford by manufacturer J.Crew', 24.00, 86.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1130, 'Cotton oxford', 'Small', 'J.Crew', 'A Cotton oxford by manufacturer J.Crew', 24.00, 86.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1131, 'Cotton oxford', 'X-Large', 'J.Crew', 'A Cotton oxford by manufacturer J.Crew', 24.00, 86.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1132, 'Short sleeve polo', 'Kids', 'Bellerose', 'A Short sleeve polo by manufacturer Bellerose', 11.00, 7.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1133, 'Short sleeve polo', 'Large', 'Bellerose', 'A Short sleeve polo by manufacturer Bellerose', 11.00, 7.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1134, 'Short sleeve polo', 'Medium', 'Bellerose', 'A Short sleeve polo by manufacturer Bellerose', 11.00, 7.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1135, 'Short sleeve polo', 'Petite', 'Bellerose', 'A Short sleeve polo by manufacturer Bellerose', 11.00, 7.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1136, 'Short sleeve polo', 'Small', 'Bellerose', 'A Short sleeve polo by manufacturer Bellerose', 11.00, 7.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1137, 'Short sleeve polo', 'X-Large', 'Bellerose', 'A Short sleeve polo by manufacturer Bellerose', 11.00, 7.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1138, 'Beach sling', 'Kids', 'CLSC', 'A Beach sling by manufacturer CLSC', 75.00, 3.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1139, 'Beach sling', 'Large', 'CLSC', 'A Beach sling by manufacturer CLSC', 75.00, 3.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1140, 'Beach sling', 'Medium', 'CLSC', 'A Beach sling by manufacturer CLSC', 75.00, 3.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1141, 'Beach sling', 'Petite', 'CLSC', 'A Beach sling by manufacturer CLSC', 75.00, 3.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1142, 'Beach sling', 'Small', 'CLSC', 'A Beach sling by manufacturer CLSC', 75.00, 3.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1143, 'Beach sling', 'X-Large', 'CLSC', 'A Beach sling by manufacturer CLSC', 75.00, 3.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1144, 'Bike short', 'Kids', 'J.Crew', 'A Bike short by manufacturer J.Crew', 25.00, 33.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1145, 'Bike short', 'Large', 'J.Crew', 'A Bike short by manufacturer J.Crew', 25.00, 33.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1146, 'Bike short', 'Medium', 'J.Crew', 'A Bike short by manufacturer J.Crew', 25.00, 33.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1147, 'Bike short', 'Petite', 'J.Crew', 'A Bike short by manufacturer J.Crew', 25.00, 33.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1148, 'Bike short', 'Small', 'J.Crew', 'A Bike short by manufacturer J.Crew', 25.00, 33.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1149, 'Bike short', 'X-Large', 'J.Crew', 'A Bike short by manufacturer J.Crew', 25.00, 33.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1150, 'Swim trunk', 'Kids', 'Converse', 'A Swim trunk by manufacturer Converse', 31.00, 80.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1151, 'Swim trunk', 'Large', 'Converse', 'A Swim trunk by manufacturer Converse', 31.00, 80.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1152, 'Swim trunk', 'Medium', 'Converse', 'A Swim trunk by manufacturer Converse', 31.00, 80.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1153, 'Swim trunk', 'Petite', 'Converse', 'A Swim trunk by manufacturer Converse', 31.00, 80.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1154, 'Swim trunk', 'Small', 'Converse', 'A Swim trunk by manufacturer Converse', 31.00, 80.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1155, 'Swim trunk', 'X-Large', 'Converse', 'A Swim trunk by manufacturer Converse', 31.00, 80.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1156, 'Yoga skort', 'Kids', 'Prada', 'A Yoga skort by manufacturer Prada', 97.00, 17.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1157, 'Yoga skort', 'Large', 'Prada', 'A Yoga skort by manufacturer Prada', 97.00, 17.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1158, 'Yoga skort', 'Medium', 'Prada', 'A Yoga skort by manufacturer Prada', 97.00, 17.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1159, 'Yoga skort', 'Petite', 'Prada', 'A Yoga skort by manufacturer Prada', 97.00, 17.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1160, 'Yoga skort', 'Small', 'Prada', 'A Yoga skort by manufacturer Prada', 97.00, 17.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1161, 'Yoga skort', 'X-Large', 'Prada', 'A Yoga skort by manufacturer Prada', 97.00, 17.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1162, 'Sweat pants', 'Kids', 'Gucci', 'A Sweat pants by manufacturer Gucci', 31.00, 68.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1163, 'Sweat pants', 'Large', 'Gucci', 'A Sweat pants by manufacturer Gucci', 31.00, 68.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1164, 'Sweat pants', 'Medium', 'Gucci', 'A Sweat pants by manufacturer Gucci', 31.00, 68.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1165, 'Sweat pants', 'Petite', 'Gucci', 'A Sweat pants by manufacturer Gucci', 31.00, 68.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1166, 'Sweat pants', 'Small', 'Gucci', 'A Sweat pants by manufacturer Gucci', 31.00, 68.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1167, 'Sweat pants', 'X-Large', 'Gucci', 'A Sweat pants by manufacturer Gucci', 31.00, 68.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1168, 'Romper', 'Kids', 'H & M', 'A Romper by manufacturer H & M', 99.00, 49.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1169, 'Romper', 'Large', 'H & M', 'A Romper by manufacturer H & M', 99.00, 49.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1170, 'Romper', 'Medium', 'H & M', 'A Romper by manufacturer H & M', 99.00, 49.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1171, 'Romper', 'Petite', 'H & M', 'A Romper by manufacturer H & M', 99.00, 49.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1172, 'Romper', 'Small', 'H & M', 'A Romper by manufacturer H & M', 99.00, 49.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1173, 'Romper', 'X-Large', 'H & M', 'A Romper by manufacturer H & M', 99.00, 49.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1174, 'Cotton oxford', 'Kids', 'Hugo Boss', 'A Cotton oxford by manufacturer Hugo Boss', 95.00, 71.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1175, 'Cotton oxford', 'Large', 'Hugo Boss', 'A Cotton oxford by manufacturer Hugo Boss', 95.00, 71.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1176, 'Cotton oxford', 'Medium', 'Hugo Boss', 'A Cotton oxford by manufacturer Hugo Boss', 95.00, 71.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1177, 'Cotton oxford', 'Petite', 'Hugo Boss', 'A Cotton oxford by manufacturer Hugo Boss', 95.00, 71.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1178, 'Cotton oxford', 'Small', 'Hugo Boss', 'A Cotton oxford by manufacturer Hugo Boss', 95.00, 71.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1179, 'Cotton oxford', 'X-Large', 'Hugo Boss', 'A Cotton oxford by manufacturer Hugo Boss', 95.00, 71.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1180, 'Wool hat', 'Kids', 'Gucci', 'A Wool hat by manufacturer Gucci', 81.00, 68.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1181, 'Wool hat', 'Large', 'Gucci', 'A Wool hat by manufacturer Gucci', 81.00, 68.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1182, 'Wool hat', 'Medium', 'Gucci', 'A Wool hat by manufacturer Gucci', 81.00, 68.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1183, 'Wool hat', 'Petite', 'Gucci', 'A Wool hat by manufacturer Gucci', 81.00, 68.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1184, 'Wool hat', 'Small', 'Gucci', 'A Wool hat by manufacturer Gucci', 81.00, 68.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1185, 'Wool hat', 'X-Large', 'Gucci', 'A Wool hat by manufacturer Gucci', 81.00, 68.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1186, 'Wool hat', 'Kids', 'Lacoste', 'A Wool hat by manufacturer Lacoste', 96.00, 71.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1187, 'Wool hat', 'Large', 'Lacoste', 'A Wool hat by manufacturer Lacoste', 96.00, 71.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1188, 'Wool hat', 'Medium', 'Lacoste', 'A Wool hat by manufacturer Lacoste', 96.00, 71.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1189, 'Wool hat', 'Petite', 'Lacoste', 'A Wool hat by manufacturer Lacoste', 96.00, 71.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1190, 'Wool hat', 'Small', 'Lacoste', 'A Wool hat by manufacturer Lacoste', 96.00, 71.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1191, 'Wool hat', 'X-Large', 'Lacoste', 'A Wool hat by manufacturer Lacoste', 96.00, 71.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1192, 'Bike short', 'Kids', 'J.Crew', 'A Bike short by manufacturer J.Crew', 9.00, 51.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1193, 'Bike short', 'Large', 'J.Crew', 'A Bike short by manufacturer J.Crew', 9.00, 51.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1194, 'Bike short', 'Medium', 'J.Crew', 'A Bike short by manufacturer J.Crew', 9.00, 51.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1195, 'Bike short', 'Petite', 'J.Crew', 'A Bike short by manufacturer J.Crew', 9.00, 51.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1196, 'Bike short', 'Small', 'J.Crew', 'A Bike short by manufacturer J.Crew', 9.00, 51.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1197, 'Bike short', 'X-Large', 'J.Crew', 'A Bike short by manufacturer J.Crew', 9.00, 51.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1198, 'Skinny jean', 'Kids', 'Nununu', 'A Skinny jean by manufacturer Nununu', 55.00, 20.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1199, 'Skinny jean', 'Large', 'Nununu', 'A Skinny jean by manufacturer Nununu', 55.00, 20.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1200, 'Skinny jean', 'Medium', 'Nununu', 'A Skinny jean by manufacturer Nununu', 55.00, 20.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1201, 'Skinny jean', 'Petite', 'Nununu', 'A Skinny jean by manufacturer Nununu', 55.00, 20.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1202, 'Skinny jean', 'Small', 'Nununu', 'A Skinny jean by manufacturer Nununu', 55.00, 20.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1203, 'Skinny jean', 'X-Large', 'Nununu', 'A Skinny jean by manufacturer Nununu', 55.00, 20.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1204, 'Yoga skort', 'Kids', 'Gymboree', 'A Yoga skort by manufacturer Gymboree', 85.00, 54.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1205, 'Yoga skort', 'Large', 'Gymboree', 'A Yoga skort by manufacturer Gymboree', 85.00, 54.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1206, 'Yoga skort', 'Medium', 'Gymboree', 'A Yoga skort by manufacturer Gymboree', 85.00, 54.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1207, 'Yoga skort', 'Petite', 'Gymboree', 'A Yoga skort by manufacturer Gymboree', 85.00, 54.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1208, 'Yoga skort', 'Small', 'Gymboree', 'A Yoga skort by manufacturer Gymboree', 85.00, 54.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1209, 'Yoga skort', 'X-Large', 'Gymboree', 'A Yoga skort by manufacturer Gymboree', 85.00, 54.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1210, 'Bathing suit', 'Kids', 'TinyCottons', 'A Bathing suit by manufacturer TinyCottons', 68.00, 57.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1211, 'Bathing suit', 'Large', 'TinyCottons', 'A Bathing suit by manufacturer TinyCottons', 68.00, 57.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1212, 'Bathing suit', 'Medium', 'TinyCottons', 'A Bathing suit by manufacturer TinyCottons', 68.00, 57.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1213, 'Bathing suit', 'Petite', 'TinyCottons', 'A Bathing suit by manufacturer TinyCottons', 68.00, 57.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1214, 'Bathing suit', 'Small', 'TinyCottons', 'A Bathing suit by manufacturer TinyCottons', 68.00, 57.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1215, 'Bathing suit', 'X-Large', 'TinyCottons', 'A Bathing suit by manufacturer TinyCottons', 68.00, 57.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1216, 'Bathing suit', 'Kids', 'Calvin Klein', 'A Bathing suit by manufacturer Calvin Klein', 14.00, 54.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1217, 'Bathing suit', 'Large', 'Calvin Klein', 'A Bathing suit by manufacturer Calvin Klein', 14.00, 54.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1218, 'Bathing suit', 'Medium', 'Calvin Klein', 'A Bathing suit by manufacturer Calvin Klein', 14.00, 54.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1219, 'Bathing suit', 'Petite', 'Calvin Klein', 'A Bathing suit by manufacturer Calvin Klein', 14.00, 54.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1220, 'Bathing suit', 'Small', 'Calvin Klein', 'A Bathing suit by manufacturer Calvin Klein', 14.00, 54.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1221, 'Bathing suit', 'X-Large', 'Calvin Klein', 'A Bathing suit by manufacturer Calvin Klein', 14.00, 54.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1222, 'Sport coat', 'Kids', 'Aeropostale', 'A Sport coat by manufacturer Aeropostale', 79.00, 35.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1223, 'Sport coat', 'Large', 'Aeropostale', 'A Sport coat by manufacturer Aeropostale', 79.00, 35.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1224, 'Sport coat', 'Medium', 'Aeropostale', 'A Sport coat by manufacturer Aeropostale', 79.00, 35.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1225, 'Sport coat', 'Petite', 'Aeropostale', 'A Sport coat by manufacturer Aeropostale', 79.00, 35.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1226, 'Sport coat', 'Small', 'Aeropostale', 'A Sport coat by manufacturer Aeropostale', 79.00, 35.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1227, 'Sport coat', 'X-Large', 'Aeropostale', 'A Sport coat by manufacturer Aeropostale', 79.00, 35.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1228, 'Wool hat', 'Kids', 'Chanel', 'A Wool hat by manufacturer Chanel', 9.00, 93.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1229, 'Wool hat', 'Large', 'Chanel', 'A Wool hat by manufacturer Chanel', 9.00, 93.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1230, 'Wool hat', 'Medium', 'Chanel', 'A Wool hat by manufacturer Chanel', 9.00, 93.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1231, 'Wool hat', 'Petite', 'Chanel', 'A Wool hat by manufacturer Chanel', 9.00, 93.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1232, 'Wool hat', 'Small', 'Chanel', 'A Wool hat by manufacturer Chanel', 9.00, 93.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1233, 'Wool hat', 'X-Large', 'Chanel', 'A Wool hat by manufacturer Chanel', 9.00, 93.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1234, 'Suspenders', 'Kids', 'Izod', 'A Suspenders by manufacturer Izod', 69.00, 18.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1235, 'Suspenders', 'Large', 'Izod', 'A Suspenders by manufacturer Izod', 69.00, 18.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1236, 'Suspenders', 'Medium', 'Izod', 'A Suspenders by manufacturer Izod', 69.00, 18.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1237, 'Suspenders', 'Petite', 'Izod', 'A Suspenders by manufacturer Izod', 69.00, 18.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1238, 'Suspenders', 'Small', 'Izod', 'A Suspenders by manufacturer Izod', 69.00, 18.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1239, 'Suspenders', 'X-Large', 'Izod', 'A Suspenders by manufacturer Izod', 69.00, 18.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1240, 'Cotton oxford', 'Kids', 'H & M', 'A Cotton oxford by manufacturer H & M', 47.00, 51.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1241, 'Cotton oxford', 'Large', 'H & M', 'A Cotton oxford by manufacturer H & M', 47.00, 51.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1242, 'Cotton oxford', 'Medium', 'H & M', 'A Cotton oxford by manufacturer H & M', 47.00, 51.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1243, 'Cotton oxford', 'Petite', 'H & M', 'A Cotton oxford by manufacturer H & M', 47.00, 51.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1244, 'Cotton oxford', 'Small', 'H & M', 'A Cotton oxford by manufacturer H & M', 47.00, 51.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1245, 'Cotton oxford', 'X-Large', 'H & M', 'A Cotton oxford by manufacturer H & M', 47.00, 51.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1246, 'Sweat pants', 'Kids', 'Diesel', 'A Sweat pants by manufacturer Diesel', 29.00, 62.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1247, 'Sweat pants', 'Large', 'Diesel', 'A Sweat pants by manufacturer Diesel', 29.00, 62.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1248, 'Sweat pants', 'Medium', 'Diesel', 'A Sweat pants by manufacturer Diesel', 29.00, 62.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1249, 'Sweat pants', 'Petite', 'Diesel', 'A Sweat pants by manufacturer Diesel', 29.00, 62.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1250, 'Sweat pants', 'Small', 'Diesel', 'A Sweat pants by manufacturer Diesel', 29.00, 62.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1251, 'Sweat pants', 'X-Large', 'Diesel', 'A Sweat pants by manufacturer Diesel', 29.00, 62.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1252, 'Skinny jean', 'Kids', 'Gymboree', 'A Skinny jean by manufacturer Gymboree', 34.00, 57.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1253, 'Skinny jean', 'Large', 'Gymboree', 'A Skinny jean by manufacturer Gymboree', 34.00, 57.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1254, 'Skinny jean', 'Medium', 'Gymboree', 'A Skinny jean by manufacturer Gymboree', 34.00, 57.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1255, 'Skinny jean', 'Petite', 'Gymboree', 'A Skinny jean by manufacturer Gymboree', 34.00, 57.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1256, 'Skinny jean', 'Small', 'Gymboree', 'A Skinny jean by manufacturer Gymboree', 34.00, 57.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1257, 'Skinny jean', 'X-Large', 'Gymboree', 'A Skinny jean by manufacturer Gymboree', 34.00, 57.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1258, 'Romper', 'Kids', 'Prada', 'A Romper by manufacturer Prada', 9.00, 78.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1259, 'Romper', 'Large', 'Prada', 'A Romper by manufacturer Prada', 9.00, 78.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1260, 'Romper', 'Medium', 'Prada', 'A Romper by manufacturer Prada', 9.00, 78.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1261, 'Romper', 'Petite', 'Prada', 'A Romper by manufacturer Prada', 9.00, 78.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1262, 'Romper', 'Small', 'Prada', 'A Romper by manufacturer Prada', 9.00, 78.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1263, 'Romper', 'X-Large', 'Prada', 'A Romper by manufacturer Prada', 9.00, 78.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1264, 'Tuxedo', 'Kids', 'Puma', 'A Tuxedo by manufacturer Puma', 56.00, 61.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1265, 'Tuxedo', 'Large', 'Puma', 'A Tuxedo by manufacturer Puma', 56.00, 61.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1266, 'Tuxedo', 'Medium', 'Puma', 'A Tuxedo by manufacturer Puma', 56.00, 61.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1267, 'Tuxedo', 'Petite', 'Puma', 'A Tuxedo by manufacturer Puma', 56.00, 61.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1268, 'Tuxedo', 'Small', 'Puma', 'A Tuxedo by manufacturer Puma', 56.00, 61.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1269, 'Tuxedo', 'X-Large', 'Puma', 'A Tuxedo by manufacturer Puma', 56.00, 61.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1270, 'Hawaiian shirt', 'Kids', 'H & M', 'A Hawaiian shirt by manufacturer H & M', 97.00, 70.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1271, 'Hawaiian shirt', 'Large', 'H & M', 'A Hawaiian shirt by manufacturer H & M', 97.00, 70.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1272, 'Hawaiian shirt', 'Medium', 'H & M', 'A Hawaiian shirt by manufacturer H & M', 97.00, 70.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1273, 'Hawaiian shirt', 'Petite', 'H & M', 'A Hawaiian shirt by manufacturer H & M', 97.00, 70.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1274, 'Hawaiian shirt', 'Small', 'H & M', 'A Hawaiian shirt by manufacturer H & M', 97.00, 70.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1275, 'Hawaiian shirt', 'X-Large', 'H & M', 'A Hawaiian shirt by manufacturer H & M', 97.00, 70.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1276, 'Bucket hat', 'Kids', 'Nununu', 'A Bucket hat by manufacturer Nununu', 27.00, 86.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1277, 'Bucket hat', 'Large', 'Nununu', 'A Bucket hat by manufacturer Nununu', 27.00, 86.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1278, 'Bucket hat', 'Medium', 'Nununu', 'A Bucket hat by manufacturer Nununu', 27.00, 86.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1279, 'Bucket hat', 'Petite', 'Nununu', 'A Bucket hat by manufacturer Nununu', 27.00, 86.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1280, 'Bucket hat', 'Small', 'Nununu', 'A Bucket hat by manufacturer Nununu', 27.00, 86.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1281, 'Bucket hat', 'X-Large', 'Nununu', 'A Bucket hat by manufacturer Nununu', 27.00, 86.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1282, 'Tank top', 'Kids', 'Chanel', 'A Tank top by manufacturer Chanel', 6.00, 48.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1283, 'Tank top', 'Large', 'Chanel', 'A Tank top by manufacturer Chanel', 6.00, 48.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1284, 'Tank top', 'Medium', 'Chanel', 'A Tank top by manufacturer Chanel', 6.00, 48.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1285, 'Tank top', 'Petite', 'Chanel', 'A Tank top by manufacturer Chanel', 6.00, 48.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1286, 'Tank top', 'Small', 'Chanel', 'A Tank top by manufacturer Chanel', 6.00, 48.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1287, 'Tank top', 'X-Large', 'Chanel', 'A Tank top by manufacturer Chanel', 6.00, 48.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1288, 'Rain jacket', 'Kids', 'Guess', 'A Rain jacket by manufacturer Guess', 62.00, 3.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1289, 'Rain jacket', 'Large', 'Guess', 'A Rain jacket by manufacturer Guess', 62.00, 3.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1290, 'Rain jacket', 'Medium', 'Guess', 'A Rain jacket by manufacturer Guess', 62.00, 3.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1291, 'Rain jacket', 'Petite', 'Guess', 'A Rain jacket by manufacturer Guess', 62.00, 3.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1292, 'Rain jacket', 'Small', 'Guess', 'A Rain jacket by manufacturer Guess', 62.00, 3.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1293, 'Rain jacket', 'X-Large', 'Guess', 'A Rain jacket by manufacturer Guess', 62.00, 3.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1294, 'Beach sling', 'Kids', 'H & M', 'A Beach sling by manufacturer H & M', 92.00, 45.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1295, 'Beach sling', 'Large', 'H & M', 'A Beach sling by manufacturer H & M', 92.00, 45.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1296, 'Beach sling', 'Medium', 'H & M', 'A Beach sling by manufacturer H & M', 92.00, 45.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1297, 'Beach sling', 'Petite', 'H & M', 'A Beach sling by manufacturer H & M', 92.00, 45.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1298, 'Beach sling', 'Small', 'H & M', 'A Beach sling by manufacturer H & M', 92.00, 45.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1299, 'Beach sling', 'X-Large', 'H & M', 'A Beach sling by manufacturer H & M', 92.00, 45.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1300, 'Bathrobe', 'Kids', 'Gucci', 'A Bathrobe by manufacturer Gucci', 70.00, 62.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1301, 'Bathrobe', 'Large', 'Gucci', 'A Bathrobe by manufacturer Gucci', 70.00, 62.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1302, 'Bathrobe', 'Medium', 'Gucci', 'A Bathrobe by manufacturer Gucci', 70.00, 62.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1303, 'Bathrobe', 'Petite', 'Gucci', 'A Bathrobe by manufacturer Gucci', 70.00, 62.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1304, 'Bathrobe', 'Small', 'Gucci', 'A Bathrobe by manufacturer Gucci', 70.00, 62.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1305, 'Bathrobe', 'X-Large', 'Gucci', 'A Bathrobe by manufacturer Gucci', 70.00, 62.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1306, 'Wool hat', 'Kids', 'Nike', 'A Wool hat by manufacturer Nike', 95.00, 1.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1307, 'Wool hat', 'Large', 'Nike', 'A Wool hat by manufacturer Nike', 95.00, 1.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1308, 'Wool hat', 'Medium', 'Nike', 'A Wool hat by manufacturer Nike', 95.00, 1.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1309, 'Wool hat', 'Petite', 'Nike', 'A Wool hat by manufacturer Nike', 95.00, 1.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1310, 'Wool hat', 'Small', 'Nike', 'A Wool hat by manufacturer Nike', 95.00, 1.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1311, 'Wool hat', 'X-Large', 'Nike', 'A Wool hat by manufacturer Nike', 95.00, 1.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1312, 'Yoga skort', 'Kids', 'Gucci', 'A Yoga skort by manufacturer Gucci', 92.00, 99.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1313, 'Yoga skort', 'Large', 'Gucci', 'A Yoga skort by manufacturer Gucci', 92.00, 99.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1314, 'Yoga skort', 'Medium', 'Gucci', 'A Yoga skort by manufacturer Gucci', 92.00, 99.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1315, 'Yoga skort', 'Petite', 'Gucci', 'A Yoga skort by manufacturer Gucci', 92.00, 99.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1316, 'Yoga skort', 'Small', 'Gucci', 'A Yoga skort by manufacturer Gucci', 92.00, 99.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1317, 'Yoga skort', 'X-Large', 'Gucci', 'A Yoga skort by manufacturer Gucci', 92.00, 99.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1318, 'Bike short', 'Kids', 'Polo', 'A Bike short by manufacturer Polo', 95.00, 11.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1319, 'Bike short', 'Large', 'Polo', 'A Bike short by manufacturer Polo', 95.00, 11.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1320, 'Bike short', 'Medium', 'Polo', 'A Bike short by manufacturer Polo', 95.00, 11.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1321, 'Bike short', 'Petite', 'Polo', 'A Bike short by manufacturer Polo', 95.00, 11.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1322, 'Bike short', 'Small', 'Polo', 'A Bike short by manufacturer Polo', 95.00, 11.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1323, 'Bike short', 'X-Large', 'Polo', 'A Bike short by manufacturer Polo', 95.00, 11.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1324, 'Beach sling', 'Kids', 'Chanel', 'A Beach sling by manufacturer Chanel', 25.00, 32.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1325, 'Beach sling', 'Large', 'Chanel', 'A Beach sling by manufacturer Chanel', 25.00, 32.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1326, 'Beach sling', 'Medium', 'Chanel', 'A Beach sling by manufacturer Chanel', 25.00, 32.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1327, 'Beach sling', 'Petite', 'Chanel', 'A Beach sling by manufacturer Chanel', 25.00, 32.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1328, 'Beach sling', 'Small', 'Chanel', 'A Beach sling by manufacturer Chanel', 25.00, 32.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1329, 'Beach sling', 'X-Large', 'Chanel', 'A Beach sling by manufacturer Chanel', 25.00, 32.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1330, 'Denim cut-offs', 'Kids', 'Armani', 'A Denim cut-offs by manufacturer Armani', 39.00, 36.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1331, 'Denim cut-offs', 'Large', 'Armani', 'A Denim cut-offs by manufacturer Armani', 39.00, 36.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1332, 'Denim cut-offs', 'Medium', 'Armani', 'A Denim cut-offs by manufacturer Armani', 39.00, 36.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1333, 'Denim cut-offs', 'Petite', 'Armani', 'A Denim cut-offs by manufacturer Armani', 39.00, 36.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1334, 'Denim cut-offs', 'Small', 'Armani', 'A Denim cut-offs by manufacturer Armani', 39.00, 36.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1335, 'Denim cut-offs', 'X-Large', 'Armani', 'A Denim cut-offs by manufacturer Armani', 39.00, 36.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1336, 'Rain jacket', 'Kids', 'Converse', 'A Rain jacket by manufacturer Converse', 79.00, 35.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1337, 'Rain jacket', 'Large', 'Converse', 'A Rain jacket by manufacturer Converse', 79.00, 35.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1338, 'Rain jacket', 'Medium', 'Converse', 'A Rain jacket by manufacturer Converse', 79.00, 35.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1339, 'Rain jacket', 'Petite', 'Converse', 'A Rain jacket by manufacturer Converse', 79.00, 35.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1340, 'Rain jacket', 'Small', 'Converse', 'A Rain jacket by manufacturer Converse', 79.00, 35.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1341, 'Rain jacket', 'X-Large', 'Converse', 'A Rain jacket by manufacturer Converse', 79.00, 35.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1342, 'Skinny jean', 'Kids', 'ZARA', 'A Skinny jean by manufacturer ZARA', 89.00, 82.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1343, 'Skinny jean', 'Large', 'ZARA', 'A Skinny jean by manufacturer ZARA', 89.00, 82.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1344, 'Skinny jean', 'Medium', 'ZARA', 'A Skinny jean by manufacturer ZARA', 89.00, 82.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1345, 'Skinny jean', 'Petite', 'ZARA', 'A Skinny jean by manufacturer ZARA', 89.00, 82.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1346, 'Skinny jean', 'Small', 'ZARA', 'A Skinny jean by manufacturer ZARA', 89.00, 82.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1347, 'Skinny jean', 'X-Large', 'ZARA', 'A Skinny jean by manufacturer ZARA', 89.00, 82.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1348, 'V-neck t-shirt', 'Kids', 'Fred Perry', 'A V-neck t-shirt by manufacturer Fred Perry', 65.00, 83.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1349, 'V-neck t-shirt', 'Large', 'Fred Perry', 'A V-neck t-shirt by manufacturer Fred Perry', 65.00, 83.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1350, 'V-neck t-shirt', 'Medium', 'Fred Perry', 'A V-neck t-shirt by manufacturer Fred Perry', 65.00, 83.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1351, 'V-neck t-shirt', 'Petite', 'Fred Perry', 'A V-neck t-shirt by manufacturer Fred Perry', 65.00, 83.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1352, 'V-neck t-shirt', 'Small', 'Fred Perry', 'A V-neck t-shirt by manufacturer Fred Perry', 65.00, 83.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1353, 'V-neck t-shirt', 'X-Large', 'Fred Perry', 'A V-neck t-shirt by manufacturer Fred Perry', 65.00, 83.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1354, 'V-neck t-shirt', 'Kids', 'Diesel', 'A V-neck t-shirt by manufacturer Diesel', 83.00, 48.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1355, 'V-neck t-shirt', 'Large', 'Diesel', 'A V-neck t-shirt by manufacturer Diesel', 83.00, 48.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1356, 'V-neck t-shirt', 'Medium', 'Diesel', 'A V-neck t-shirt by manufacturer Diesel', 83.00, 48.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1357, 'V-neck t-shirt', 'Petite', 'Diesel', 'A V-neck t-shirt by manufacturer Diesel', 83.00, 48.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1358, 'V-neck t-shirt', 'Small', 'Diesel', 'A V-neck t-shirt by manufacturer Diesel', 83.00, 48.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1359, 'V-neck t-shirt', 'X-Large', 'Diesel', 'A V-neck t-shirt by manufacturer Diesel', 83.00, 48.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1360, 'Tank top', 'Kids', 'Acrylick', 'A Tank top by manufacturer Acrylick', 75.00, 52.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1361, 'Tank top', 'Large', 'Acrylick', 'A Tank top by manufacturer Acrylick', 75.00, 52.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1362, 'Tank top', 'Medium', 'Acrylick', 'A Tank top by manufacturer Acrylick', 75.00, 52.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1363, 'Tank top', 'Petite', 'Acrylick', 'A Tank top by manufacturer Acrylick', 75.00, 52.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1364, 'Tank top', 'Small', 'Acrylick', 'A Tank top by manufacturer Acrylick', 75.00, 52.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1365, 'Tank top', 'X-Large', 'Acrylick', 'A Tank top by manufacturer Acrylick', 75.00, 52.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1366, 'Overalls', 'Kids', 'Gap', 'A Overalls by manufacturer Gap', 70.00, 60.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1367, 'Overalls', 'Large', 'Gap', 'A Overalls by manufacturer Gap', 70.00, 60.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1368, 'Overalls', 'Medium', 'Gap', 'A Overalls by manufacturer Gap', 70.00, 60.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1369, 'Overalls', 'Petite', 'Gap', 'A Overalls by manufacturer Gap', 70.00, 60.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1370, 'Overalls', 'Small', 'Gap', 'A Overalls by manufacturer Gap', 70.00, 60.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1371, 'Overalls', 'X-Large', 'Gap', 'A Overalls by manufacturer Gap', 70.00, 60.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1372, 'Sweatshirt', 'Kids', 'ZARA', 'A Sweatshirt by manufacturer ZARA', 16.00, 15.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1373, 'Sweatshirt', 'Large', 'ZARA', 'A Sweatshirt by manufacturer ZARA', 16.00, 15.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1374, 'Sweatshirt', 'Medium', 'ZARA', 'A Sweatshirt by manufacturer ZARA', 16.00, 15.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1375, 'Sweatshirt', 'Petite', 'ZARA', 'A Sweatshirt by manufacturer ZARA', 16.00, 15.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1376, 'Sweatshirt', 'Small', 'ZARA', 'A Sweatshirt by manufacturer ZARA', 16.00, 15.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1377, 'Sweatshirt', 'X-Large', 'ZARA', 'A Sweatshirt by manufacturer ZARA', 16.00, 15.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1378, 'Wool hat', 'Kids', 'Carhartt', 'A Wool hat by manufacturer Carhartt', 87.00, 42.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1379, 'Wool hat', 'Large', 'Carhartt', 'A Wool hat by manufacturer Carhartt', 87.00, 42.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1380, 'Wool hat', 'Medium', 'Carhartt', 'A Wool hat by manufacturer Carhartt', 87.00, 42.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1381, 'Wool hat', 'Petite', 'Carhartt', 'A Wool hat by manufacturer Carhartt', 87.00, 42.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1382, 'Wool hat', 'Small', 'Carhartt', 'A Wool hat by manufacturer Carhartt', 87.00, 42.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1383, 'Wool hat', 'X-Large', 'Carhartt', 'A Wool hat by manufacturer Carhartt', 87.00, 42.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1384, 'Tank top', 'Kids', 'Gymboree', 'A Tank top by manufacturer Gymboree', 79.00, 27.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1385, 'Tank top', 'Large', 'Gymboree', 'A Tank top by manufacturer Gymboree', 79.00, 27.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1386, 'Tank top', 'Medium', 'Gymboree', 'A Tank top by manufacturer Gymboree', 79.00, 27.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1387, 'Tank top', 'Petite', 'Gymboree', 'A Tank top by manufacturer Gymboree', 79.00, 27.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1388, 'Tank top', 'Small', 'Gymboree', 'A Tank top by manufacturer Gymboree', 79.00, 27.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1389, 'Tank top', 'X-Large', 'Gymboree', 'A Tank top by manufacturer Gymboree', 79.00, 27.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1390, 'Tuxedo', 'Kids', 'Fred Perry', 'A Tuxedo by manufacturer Fred Perry', 46.00, 33.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1391, 'Tuxedo', 'Large', 'Fred Perry', 'A Tuxedo by manufacturer Fred Perry', 46.00, 33.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1392, 'Tuxedo', 'Medium', 'Fred Perry', 'A Tuxedo by manufacturer Fred Perry', 46.00, 33.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1393, 'Tuxedo', 'Petite', 'Fred Perry', 'A Tuxedo by manufacturer Fred Perry', 46.00, 33.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1394, 'Tuxedo', 'Small', 'Fred Perry', 'A Tuxedo by manufacturer Fred Perry', 46.00, 33.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1395, 'Tuxedo', 'X-Large', 'Fred Perry', 'A Tuxedo by manufacturer Fred Perry', 46.00, 33.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1396, 'Ballet skirt', 'Kids', 'Hugo Boss', 'A Ballet skirt by manufacturer Hugo Boss', 14.00, 11.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1397, 'Ballet skirt', 'Large', 'Hugo Boss', 'A Ballet skirt by manufacturer Hugo Boss', 14.00, 11.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1398, 'Ballet skirt', 'Medium', 'Hugo Boss', 'A Ballet skirt by manufacturer Hugo Boss', 14.00, 11.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1399, 'Ballet skirt', 'Petite', 'Hugo Boss', 'A Ballet skirt by manufacturer Hugo Boss', 14.00, 11.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1400, 'Ballet skirt', 'Small', 'Hugo Boss', 'A Ballet skirt by manufacturer Hugo Boss', 14.00, 11.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1401, 'Ballet skirt', 'X-Large', 'Hugo Boss', 'A Ballet skirt by manufacturer Hugo Boss', 14.00, 11.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1402, 'Bucket hat', 'Kids', 'Carhartt', 'A Bucket hat by manufacturer Carhartt', 65.00, 47.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1403, 'Bucket hat', 'Large', 'Carhartt', 'A Bucket hat by manufacturer Carhartt', 65.00, 47.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1404, 'Bucket hat', 'Medium', 'Carhartt', 'A Bucket hat by manufacturer Carhartt', 65.00, 47.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1405, 'Bucket hat', 'Petite', 'Carhartt', 'A Bucket hat by manufacturer Carhartt', 65.00, 47.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1406, 'Bucket hat', 'Small', 'Carhartt', 'A Bucket hat by manufacturer Carhartt', 65.00, 47.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1407, 'Bucket hat', 'X-Large', 'Carhartt', 'A Bucket hat by manufacturer Carhartt', 65.00, 47.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1408, 'Rain jacket', 'Kids', 'Carhartt', 'A Rain jacket by manufacturer Carhartt', 28.00, 55.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1409, 'Rain jacket', 'Large', 'Carhartt', 'A Rain jacket by manufacturer Carhartt', 28.00, 55.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1410, 'Rain jacket', 'Medium', 'Carhartt', 'A Rain jacket by manufacturer Carhartt', 28.00, 55.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1411, 'Rain jacket', 'Petite', 'Carhartt', 'A Rain jacket by manufacturer Carhartt', 28.00, 55.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1412, 'Rain jacket', 'Small', 'Carhartt', 'A Rain jacket by manufacturer Carhartt', 28.00, 55.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1413, 'Rain jacket', 'X-Large', 'Carhartt', 'A Rain jacket by manufacturer Carhartt', 28.00, 55.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1414, 'Beach sling', 'Kids', 'Prada', 'A Beach sling by manufacturer Prada', 93.00, 28.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1415, 'Beach sling', 'Large', 'Prada', 'A Beach sling by manufacturer Prada', 93.00, 28.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1416, 'Beach sling', 'Medium', 'Prada', 'A Beach sling by manufacturer Prada', 93.00, 28.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1417, 'Beach sling', 'Petite', 'Prada', 'A Beach sling by manufacturer Prada', 93.00, 28.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1418, 'Beach sling', 'Small', 'Prada', 'A Beach sling by manufacturer Prada', 93.00, 28.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1419, 'Beach sling', 'X-Large', 'Prada', 'A Beach sling by manufacturer Prada', 93.00, 28.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1420, 'Sweat pants', 'Kids', 'Converse', 'A Sweat pants by manufacturer Converse', 98.00, 71.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1421, 'Sweat pants', 'Large', 'Converse', 'A Sweat pants by manufacturer Converse', 98.00, 71.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1422, 'Sweat pants', 'Medium', 'Converse', 'A Sweat pants by manufacturer Converse', 98.00, 71.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1423, 'Sweat pants', 'Petite', 'Converse', 'A Sweat pants by manufacturer Converse', 98.00, 71.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1424, 'Sweat pants', 'Small', 'Converse', 'A Sweat pants by manufacturer Converse', 98.00, 71.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1425, 'Sweat pants', 'X-Large', 'Converse', 'A Sweat pants by manufacturer Converse', 98.00, 71.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1426, 'Denim cut-offs', 'Kids', 'J.Crew', 'A Denim cut-offs by manufacturer J.Crew', 3.00, 82.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1427, 'Denim cut-offs', 'Large', 'J.Crew', 'A Denim cut-offs by manufacturer J.Crew', 3.00, 82.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1428, 'Denim cut-offs', 'Medium', 'J.Crew', 'A Denim cut-offs by manufacturer J.Crew', 3.00, 82.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1429, 'Denim cut-offs', 'Petite', 'J.Crew', 'A Denim cut-offs by manufacturer J.Crew', 3.00, 82.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1430, 'Denim cut-offs', 'Small', 'J.Crew', 'A Denim cut-offs by manufacturer J.Crew', 3.00, 82.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1431, 'Denim cut-offs', 'X-Large', 'J.Crew', 'A Denim cut-offs by manufacturer J.Crew', 3.00, 82.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1432, 'Romper', 'Kids', 'Aeropostale', 'A Romper by manufacturer Aeropostale', 19.00, 85.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1433, 'Romper', 'Large', 'Aeropostale', 'A Romper by manufacturer Aeropostale', 19.00, 85.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1434, 'Romper', 'Medium', 'Aeropostale', 'A Romper by manufacturer Aeropostale', 19.00, 85.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1435, 'Romper', 'Petite', 'Aeropostale', 'A Romper by manufacturer Aeropostale', 19.00, 85.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1436, 'Romper', 'Small', 'Aeropostale', 'A Romper by manufacturer Aeropostale', 19.00, 85.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1437, 'Romper', 'X-Large', 'Aeropostale', 'A Romper by manufacturer Aeropostale', 19.00, 85.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1438, 'Bike short', 'Kids', 'Puma', 'A Bike short by manufacturer Puma', 8.00, 54.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1439, 'Bike short', 'Large', 'Puma', 'A Bike short by manufacturer Puma', 8.00, 54.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1440, 'Bike short', 'Medium', 'Puma', 'A Bike short by manufacturer Puma', 8.00, 54.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1441, 'Bike short', 'Petite', 'Puma', 'A Bike short by manufacturer Puma', 8.00, 54.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1442, 'Bike short', 'Small', 'Puma', 'A Bike short by manufacturer Puma', 8.00, 54.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1443, 'Bike short', 'X-Large', 'Puma', 'A Bike short by manufacturer Puma', 8.00, 54.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1444, 'Bike short', 'Kids', 'Puma', 'A Bike short by manufacturer Puma', 51.00, 9.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1445, 'Bike short', 'Large', 'Puma', 'A Bike short by manufacturer Puma', 51.00, 9.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1446, 'Bike short', 'Medium', 'Puma', 'A Bike short by manufacturer Puma', 51.00, 9.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1447, 'Bike short', 'Petite', 'Puma', 'A Bike short by manufacturer Puma', 51.00, 9.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1448, 'Bike short', 'Small', 'Puma', 'A Bike short by manufacturer Puma', 51.00, 9.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1449, 'Bike short', 'X-Large', 'Puma', 'A Bike short by manufacturer Puma', 51.00, 9.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1450, 'Backpack', 'Kids', 'Armani', 'A Backpack by manufacturer Armani', 60.00, 93.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1451, 'Backpack', 'Large', 'Armani', 'A Backpack by manufacturer Armani', 60.00, 93.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1452, 'Backpack', 'Medium', 'Armani', 'A Backpack by manufacturer Armani', 60.00, 93.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1453, 'Backpack', 'Petite', 'Armani', 'A Backpack by manufacturer Armani', 60.00, 93.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1454, 'Backpack', 'Small', 'Armani', 'A Backpack by manufacturer Armani', 60.00, 93.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1455, 'Backpack', 'X-Large', 'Armani', 'A Backpack by manufacturer Armani', 60.00, 93.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1456, 'Denim cut-offs', 'Kids', 'Gap', 'A Denim cut-offs by manufacturer Gap', 41.00, 96.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1457, 'Denim cut-offs', 'Large', 'Gap', 'A Denim cut-offs by manufacturer Gap', 41.00, 96.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1458, 'Denim cut-offs', 'Medium', 'Gap', 'A Denim cut-offs by manufacturer Gap', 41.00, 96.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1459, 'Denim cut-offs', 'Petite', 'Gap', 'A Denim cut-offs by manufacturer Gap', 41.00, 96.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1460, 'Denim cut-offs', 'Small', 'Gap', 'A Denim cut-offs by manufacturer Gap', 41.00, 96.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1461, 'Denim cut-offs', 'X-Large', 'Gap', 'A Denim cut-offs by manufacturer Gap', 41.00, 96.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1462, 'Denim cut-offs', 'Kids', 'Calvin Klein', 'A Denim cut-offs by manufacturer Calvin Klein', 58.00, 80.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1463, 'Denim cut-offs', 'Large', 'Calvin Klein', 'A Denim cut-offs by manufacturer Calvin Klein', 58.00, 80.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1464, 'Denim cut-offs', 'Medium', 'Calvin Klein', 'A Denim cut-offs by manufacturer Calvin Klein', 58.00, 80.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1465, 'Denim cut-offs', 'Petite', 'Calvin Klein', 'A Denim cut-offs by manufacturer Calvin Klein', 58.00, 80.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1466, 'Denim cut-offs', 'Small', 'Calvin Klein', 'A Denim cut-offs by manufacturer Calvin Klein', 58.00, 80.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1467, 'Denim cut-offs', 'X-Large', 'Calvin Klein', 'A Denim cut-offs by manufacturer Calvin Klein', 58.00, 80.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1468, 'Pajama pants', 'Kids', 'Fred Perry', 'A Pajama pants by manufacturer Fred Perry', 32.00, 25.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1469, 'Pajama pants', 'Large', 'Fred Perry', 'A Pajama pants by manufacturer Fred Perry', 32.00, 25.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1470, 'Pajama pants', 'Medium', 'Fred Perry', 'A Pajama pants by manufacturer Fred Perry', 32.00, 25.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1471, 'Pajama pants', 'Petite', 'Fred Perry', 'A Pajama pants by manufacturer Fred Perry', 32.00, 25.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1472, 'Pajama pants', 'Small', 'Fred Perry', 'A Pajama pants by manufacturer Fred Perry', 32.00, 25.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1473, 'Pajama pants', 'X-Large', 'Fred Perry', 'A Pajama pants by manufacturer Fred Perry', 32.00, 25.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1474, 'Denim cut-offs', 'Kids', 'J.Crew', 'A Denim cut-offs by manufacturer J.Crew', 89.00, 34.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1475, 'Denim cut-offs', 'Large', 'J.Crew', 'A Denim cut-offs by manufacturer J.Crew', 89.00, 34.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1476, 'Denim cut-offs', 'Medium', 'J.Crew', 'A Denim cut-offs by manufacturer J.Crew', 89.00, 34.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1477, 'Denim cut-offs', 'Petite', 'J.Crew', 'A Denim cut-offs by manufacturer J.Crew', 89.00, 34.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1478, 'Denim cut-offs', 'Small', 'J.Crew', 'A Denim cut-offs by manufacturer J.Crew', 89.00, 34.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1479, 'Denim cut-offs', 'X-Large', 'J.Crew', 'A Denim cut-offs by manufacturer J.Crew', 89.00, 34.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1480, 'Romper', 'Kids', 'Acrylick', 'A Romper by manufacturer Acrylick', 20.00, 23.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1481, 'Romper', 'Large', 'Acrylick', 'A Romper by manufacturer Acrylick', 20.00, 23.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1482, 'Romper', 'Medium', 'Acrylick', 'A Romper by manufacturer Acrylick', 20.00, 23.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1483, 'Romper', 'Petite', 'Acrylick', 'A Romper by manufacturer Acrylick', 20.00, 23.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1484, 'Romper', 'Small', 'Acrylick', 'A Romper by manufacturer Acrylick', 20.00, 23.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1485, 'Romper', 'X-Large', 'Acrylick', 'A Romper by manufacturer Acrylick', 20.00, 23.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1486, 'Sport briefs', 'Kids', 'CLSC', 'A Sport briefs by manufacturer CLSC', 85.00, 56.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1487, 'Sport briefs', 'Large', 'CLSC', 'A Sport briefs by manufacturer CLSC', 85.00, 56.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1488, 'Sport briefs', 'Medium', 'CLSC', 'A Sport briefs by manufacturer CLSC', 85.00, 56.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1489, 'Sport briefs', 'Petite', 'CLSC', 'A Sport briefs by manufacturer CLSC', 85.00, 56.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1490, 'Sport briefs', 'Small', 'CLSC', 'A Sport briefs by manufacturer CLSC', 85.00, 56.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1491, 'Sport briefs', 'X-Large', 'CLSC', 'A Sport briefs by manufacturer CLSC', 85.00, 56.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1492, 'Sport coat', 'Kids', 'Versace', 'A Sport coat by manufacturer Versace', 41.00, 4.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1493, 'Sport coat', 'Large', 'Versace', 'A Sport coat by manufacturer Versace', 41.00, 4.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1494, 'Sport coat', 'Medium', 'Versace', 'A Sport coat by manufacturer Versace', 41.00, 4.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1495, 'Sport coat', 'Petite', 'Versace', 'A Sport coat by manufacturer Versace', 41.00, 4.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1496, 'Sport coat', 'Small', 'Versace', 'A Sport coat by manufacturer Versace', 41.00, 4.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1497, 'Sport coat', 'X-Large', 'Versace', 'A Sport coat by manufacturer Versace', 41.00, 4.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1498, 'Swim trunk', 'Kids', 'Nike', 'A Swim trunk by manufacturer Nike', 38.00, 40.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1499, 'Swim trunk', 'Large', 'Nike', 'A Swim trunk by manufacturer Nike', 38.00, 40.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1500, 'Swim trunk', 'Medium', 'Nike', 'A Swim trunk by manufacturer Nike', 38.00, 40.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1501, 'Swim trunk', 'Petite', 'Nike', 'A Swim trunk by manufacturer Nike', 38.00, 40.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1502, 'Swim trunk', 'Small', 'Nike', 'A Swim trunk by manufacturer Nike', 38.00, 40.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1503, 'Swim trunk', 'X-Large', 'Nike', 'A Swim trunk by manufacturer Nike', 38.00, 40.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1504, 'Skinny jean', 'Kids', 'Carhartt', 'A Skinny jean by manufacturer Carhartt', 83.00, 51.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1505, 'Skinny jean', 'Large', 'Carhartt', 'A Skinny jean by manufacturer Carhartt', 83.00, 51.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1506, 'Skinny jean', 'Medium', 'Carhartt', 'A Skinny jean by manufacturer Carhartt', 83.00, 51.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1507, 'Skinny jean', 'Petite', 'Carhartt', 'A Skinny jean by manufacturer Carhartt', 83.00, 51.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1508, 'Skinny jean', 'Small', 'Carhartt', 'A Skinny jean by manufacturer Carhartt', 83.00, 51.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1509, 'Skinny jean', 'X-Large', 'Carhartt', 'A Skinny jean by manufacturer Carhartt', 83.00, 51.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1510, 'Bathing suit', 'Kids', 'Carhartt', 'A Bathing suit by manufacturer Carhartt', 92.00, 23.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1511, 'Bathing suit', 'Large', 'Carhartt', 'A Bathing suit by manufacturer Carhartt', 92.00, 23.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1512, 'Bathing suit', 'Medium', 'Carhartt', 'A Bathing suit by manufacturer Carhartt', 92.00, 23.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1513, 'Bathing suit', 'Petite', 'Carhartt', 'A Bathing suit by manufacturer Carhartt', 92.00, 23.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1514, 'Bathing suit', 'Small', 'Carhartt', 'A Bathing suit by manufacturer Carhartt', 92.00, 23.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1515, 'Bathing suit', 'X-Large', 'Carhartt', 'A Bathing suit by manufacturer Carhartt', 92.00, 23.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1516, 'Tank top', 'Kids', 'Gucci', 'A Tank top by manufacturer Gucci', 8.00, 76.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1517, 'Tank top', 'Large', 'Gucci', 'A Tank top by manufacturer Gucci', 8.00, 76.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1518, 'Tank top', 'Medium', 'Gucci', 'A Tank top by manufacturer Gucci', 8.00, 76.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1519, 'Tank top', 'Petite', 'Gucci', 'A Tank top by manufacturer Gucci', 8.00, 76.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1520, 'Tank top', 'Small', 'Gucci', 'A Tank top by manufacturer Gucci', 8.00, 76.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1521, 'Tank top', 'X-Large', 'Gucci', 'A Tank top by manufacturer Gucci', 8.00, 76.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1522, 'Tuxedo', 'Kids', 'Converse', 'A Tuxedo by manufacturer Converse', 4.00, 31.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1523, 'Tuxedo', 'Large', 'Converse', 'A Tuxedo by manufacturer Converse', 4.00, 31.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1524, 'Tuxedo', 'Medium', 'Converse', 'A Tuxedo by manufacturer Converse', 4.00, 31.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1525, 'Tuxedo', 'Petite', 'Converse', 'A Tuxedo by manufacturer Converse', 4.00, 31.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1526, 'Tuxedo', 'Small', 'Converse', 'A Tuxedo by manufacturer Converse', 4.00, 31.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1527, 'Tuxedo', 'X-Large', 'Converse', 'A Tuxedo by manufacturer Converse', 4.00, 31.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1528, 'Overalls', 'Kids', 'Nununu', 'A Overalls by manufacturer Nununu', 67.00, 3.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1529, 'Overalls', 'Large', 'Nununu', 'A Overalls by manufacturer Nununu', 67.00, 3.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1530, 'Overalls', 'Medium', 'Nununu', 'A Overalls by manufacturer Nununu', 67.00, 3.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1531, 'Overalls', 'Petite', 'Nununu', 'A Overalls by manufacturer Nununu', 67.00, 3.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1532, 'Overalls', 'Small', 'Nununu', 'A Overalls by manufacturer Nununu', 67.00, 3.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1533, 'Overalls', 'X-Large', 'Nununu', 'A Overalls by manufacturer Nununu', 67.00, 3.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1534, 'Pajama pants', 'Kids', 'Diesel', 'A Pajama pants by manufacturer Diesel', 13.00, 82.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1535, 'Pajama pants', 'Large', 'Diesel', 'A Pajama pants by manufacturer Diesel', 13.00, 82.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1536, 'Pajama pants', 'Medium', 'Diesel', 'A Pajama pants by manufacturer Diesel', 13.00, 82.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1537, 'Pajama pants', 'Petite', 'Diesel', 'A Pajama pants by manufacturer Diesel', 13.00, 82.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1538, 'Pajama pants', 'Small', 'Diesel', 'A Pajama pants by manufacturer Diesel', 13.00, 82.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1539, 'Pajama pants', 'X-Large', 'Diesel', 'A Pajama pants by manufacturer Diesel', 13.00, 82.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1540, 'Bathrobe', 'Kids', 'Hugo Boss', 'A Bathrobe by manufacturer Hugo Boss', 91.00, 94.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1541, 'Bathrobe', 'Large', 'Hugo Boss', 'A Bathrobe by manufacturer Hugo Boss', 91.00, 94.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1542, 'Bathrobe', 'Medium', 'Hugo Boss', 'A Bathrobe by manufacturer Hugo Boss', 91.00, 94.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1543, 'Bathrobe', 'Petite', 'Hugo Boss', 'A Bathrobe by manufacturer Hugo Boss', 91.00, 94.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1544, 'Bathrobe', 'Small', 'Hugo Boss', 'A Bathrobe by manufacturer Hugo Boss', 91.00, 94.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1545, 'Bathrobe', 'X-Large', 'Hugo Boss', 'A Bathrobe by manufacturer Hugo Boss', 91.00, 94.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1546, 'Tuxedo', 'Kids', 'Nununu', 'A Tuxedo by manufacturer Nununu', 91.00, 68.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1547, 'Tuxedo', 'Large', 'Nununu', 'A Tuxedo by manufacturer Nununu', 91.00, 68.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1548, 'Tuxedo', 'Medium', 'Nununu', 'A Tuxedo by manufacturer Nununu', 91.00, 68.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1549, 'Tuxedo', 'Petite', 'Nununu', 'A Tuxedo by manufacturer Nununu', 91.00, 68.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1550, 'Tuxedo', 'Small', 'Nununu', 'A Tuxedo by manufacturer Nununu', 91.00, 68.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1551, 'Tuxedo', 'X-Large', 'Nununu', 'A Tuxedo by manufacturer Nununu', 91.00, 68.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1552, 'Hawaiian shirt', 'Kids', 'CLSC', 'A Hawaiian shirt by manufacturer CLSC', 76.00, 42.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1553, 'Hawaiian shirt', 'Large', 'CLSC', 'A Hawaiian shirt by manufacturer CLSC', 76.00, 42.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1554, 'Hawaiian shirt', 'Medium', 'CLSC', 'A Hawaiian shirt by manufacturer CLSC', 76.00, 42.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1555, 'Hawaiian shirt', 'Petite', 'CLSC', 'A Hawaiian shirt by manufacturer CLSC', 76.00, 42.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1556, 'Hawaiian shirt', 'Small', 'CLSC', 'A Hawaiian shirt by manufacturer CLSC', 76.00, 42.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1557, 'Hawaiian shirt', 'X-Large', 'CLSC', 'A Hawaiian shirt by manufacturer CLSC', 76.00, 42.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1558, 'Backpack', 'Kids', 'Armani', 'A Backpack by manufacturer Armani', 61.00, 29.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1559, 'Backpack', 'Large', 'Armani', 'A Backpack by manufacturer Armani', 61.00, 29.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1560, 'Backpack', 'Medium', 'Armani', 'A Backpack by manufacturer Armani', 61.00, 29.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1561, 'Backpack', 'Petite', 'Armani', 'A Backpack by manufacturer Armani', 61.00, 29.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1562, 'Backpack', 'Small', 'Armani', 'A Backpack by manufacturer Armani', 61.00, 29.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1563, 'Backpack', 'X-Large', 'Armani', 'A Backpack by manufacturer Armani', 61.00, 29.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1564, 'Tuxedo', 'Kids', 'Guess', 'A Tuxedo by manufacturer Guess', 7.00, 74.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1565, 'Tuxedo', 'Large', 'Guess', 'A Tuxedo by manufacturer Guess', 7.00, 74.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1566, 'Tuxedo', 'Medium', 'Guess', 'A Tuxedo by manufacturer Guess', 7.00, 74.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1567, 'Tuxedo', 'Petite', 'Guess', 'A Tuxedo by manufacturer Guess', 7.00, 74.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1568, 'Tuxedo', 'Small', 'Guess', 'A Tuxedo by manufacturer Guess', 7.00, 74.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1569, 'Tuxedo', 'X-Large', 'Guess', 'A Tuxedo by manufacturer Guess', 7.00, 74.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1570, 'Sport coat', 'Kids', 'Fred Perry', 'A Sport coat by manufacturer Fred Perry', 31.00, 10.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1571, 'Sport coat', 'Large', 'Fred Perry', 'A Sport coat by manufacturer Fred Perry', 31.00, 10.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1572, 'Sport coat', 'Medium', 'Fred Perry', 'A Sport coat by manufacturer Fred Perry', 31.00, 10.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1573, 'Sport coat', 'Petite', 'Fred Perry', 'A Sport coat by manufacturer Fred Perry', 31.00, 10.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1574, 'Sport coat', 'Small', 'Fred Perry', 'A Sport coat by manufacturer Fred Perry', 31.00, 10.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1575, 'Sport coat', 'X-Large', 'Fred Perry', 'A Sport coat by manufacturer Fred Perry', 31.00, 10.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1576, 'Backpack', 'Kids', 'Diesel', 'A Backpack by manufacturer Diesel', 73.00, 51.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1577, 'Backpack', 'Large', 'Diesel', 'A Backpack by manufacturer Diesel', 73.00, 51.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1578, 'Backpack', 'Medium', 'Diesel', 'A Backpack by manufacturer Diesel', 73.00, 51.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1579, 'Backpack', 'Petite', 'Diesel', 'A Backpack by manufacturer Diesel', 73.00, 51.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1580, 'Backpack', 'Small', 'Diesel', 'A Backpack by manufacturer Diesel', 73.00, 51.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1581, 'Backpack', 'X-Large', 'Diesel', 'A Backpack by manufacturer Diesel', 73.00, 51.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1582, 'Denim cut-offs', 'Kids', 'Carhartt', 'A Denim cut-offs by manufacturer Carhartt', 6.00, 12.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1583, 'Denim cut-offs', 'Large', 'Carhartt', 'A Denim cut-offs by manufacturer Carhartt', 6.00, 12.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1584, 'Denim cut-offs', 'Medium', 'Carhartt', 'A Denim cut-offs by manufacturer Carhartt', 6.00, 12.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1585, 'Denim cut-offs', 'Petite', 'Carhartt', 'A Denim cut-offs by manufacturer Carhartt', 6.00, 12.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1586, 'Denim cut-offs', 'Small', 'Carhartt', 'A Denim cut-offs by manufacturer Carhartt', 6.00, 12.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1587, 'Denim cut-offs', 'X-Large', 'Carhartt', 'A Denim cut-offs by manufacturer Carhartt', 6.00, 12.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1588, 'Suspenders', 'Kids', 'Prada', 'A Suspenders by manufacturer Prada', 16.00, 55.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1589, 'Suspenders', 'Large', 'Prada', 'A Suspenders by manufacturer Prada', 16.00, 55.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1590, 'Suspenders', 'Medium', 'Prada', 'A Suspenders by manufacturer Prada', 16.00, 55.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1591, 'Suspenders', 'Petite', 'Prada', 'A Suspenders by manufacturer Prada', 16.00, 55.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1592, 'Suspenders', 'Small', 'Prada', 'A Suspenders by manufacturer Prada', 16.00, 55.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1593, 'Suspenders', 'X-Large', 'Prada', 'A Suspenders by manufacturer Prada', 16.00, 55.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1594, 'Backpack', 'Kids', 'J.Crew', 'A Backpack by manufacturer J.Crew', 29.00, 12.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1595, 'Backpack', 'Large', 'J.Crew', 'A Backpack by manufacturer J.Crew', 29.00, 12.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1596, 'Backpack', 'Medium', 'J.Crew', 'A Backpack by manufacturer J.Crew', 29.00, 12.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1597, 'Backpack', 'Petite', 'J.Crew', 'A Backpack by manufacturer J.Crew', 29.00, 12.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1598, 'Backpack', 'Small', 'J.Crew', 'A Backpack by manufacturer J.Crew', 29.00, 12.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1599, 'Backpack', 'X-Large', 'J.Crew', 'A Backpack by manufacturer J.Crew', 29.00, 12.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1600, 'Sport briefs', 'Kids', 'Lacoste', 'A Sport briefs by manufacturer Lacoste', 29.00, 60.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1601, 'Sport briefs', 'Large', 'Lacoste', 'A Sport briefs by manufacturer Lacoste', 29.00, 60.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1602, 'Sport briefs', 'Medium', 'Lacoste', 'A Sport briefs by manufacturer Lacoste', 29.00, 60.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1603, 'Sport briefs', 'Petite', 'Lacoste', 'A Sport briefs by manufacturer Lacoste', 29.00, 60.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1604, 'Sport briefs', 'Small', 'Lacoste', 'A Sport briefs by manufacturer Lacoste', 29.00, 60.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1605, 'Sport briefs', 'X-Large', 'Lacoste', 'A Sport briefs by manufacturer Lacoste', 29.00, 60.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1606, 'Sport coat', 'Kids', 'Nike', 'A Sport coat by manufacturer Nike', 92.00, 20.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1607, 'Sport coat', 'Large', 'Nike', 'A Sport coat by manufacturer Nike', 92.00, 20.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1608, 'Sport coat', 'Medium', 'Nike', 'A Sport coat by manufacturer Nike', 92.00, 20.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1609, 'Sport coat', 'Petite', 'Nike', 'A Sport coat by manufacturer Nike', 92.00, 20.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1610, 'Sport coat', 'Small', 'Nike', 'A Sport coat by manufacturer Nike', 92.00, 20.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1611, 'Sport coat', 'X-Large', 'Nike', 'A Sport coat by manufacturer Nike', 92.00, 20.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1612, 'Rain jacket', 'Kids', 'Armani', 'A Rain jacket by manufacturer Armani', 66.00, 99.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1613, 'Rain jacket', 'Large', 'Armani', 'A Rain jacket by manufacturer Armani', 66.00, 99.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1614, 'Rain jacket', 'Medium', 'Armani', 'A Rain jacket by manufacturer Armani', 66.00, 99.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1615, 'Rain jacket', 'Petite', 'Armani', 'A Rain jacket by manufacturer Armani', 66.00, 99.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1616, 'Rain jacket', 'Small', 'Armani', 'A Rain jacket by manufacturer Armani', 66.00, 99.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1617, 'Rain jacket', 'X-Large', 'Armani', 'A Rain jacket by manufacturer Armani', 66.00, 99.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1618, 'Sweat pants', 'Kids', 'Polo', 'A Sweat pants by manufacturer Polo', 50.00, 84.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1619, 'Sweat pants', 'Large', 'Polo', 'A Sweat pants by manufacturer Polo', 50.00, 84.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1620, 'Sweat pants', 'Medium', 'Polo', 'A Sweat pants by manufacturer Polo', 50.00, 84.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1621, 'Sweat pants', 'Petite', 'Polo', 'A Sweat pants by manufacturer Polo', 50.00, 84.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1622, 'Sweat pants', 'Small', 'Polo', 'A Sweat pants by manufacturer Polo', 50.00, 84.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1623, 'Sweat pants', 'X-Large', 'Polo', 'A Sweat pants by manufacturer Polo', 50.00, 84.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1624, 'Wool hat', 'Kids', 'Acrylick', 'A Wool hat by manufacturer Acrylick', 37.00, 69.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1625, 'Wool hat', 'Large', 'Acrylick', 'A Wool hat by manufacturer Acrylick', 37.00, 69.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1626, 'Wool hat', 'Medium', 'Acrylick', 'A Wool hat by manufacturer Acrylick', 37.00, 69.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1627, 'Wool hat', 'Petite', 'Acrylick', 'A Wool hat by manufacturer Acrylick', 37.00, 69.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1628, 'Wool hat', 'Small', 'Acrylick', 'A Wool hat by manufacturer Acrylick', 37.00, 69.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1629, 'Wool hat', 'X-Large', 'Acrylick', 'A Wool hat by manufacturer Acrylick', 37.00, 69.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1630, 'Short sleeve polo', 'Kids', 'Diesel', 'A Short sleeve polo by manufacturer Diesel', 70.00, 46.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1631, 'Short sleeve polo', 'Large', 'Diesel', 'A Short sleeve polo by manufacturer Diesel', 70.00, 46.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1632, 'Short sleeve polo', 'Medium', 'Diesel', 'A Short sleeve polo by manufacturer Diesel', 70.00, 46.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1633, 'Short sleeve polo', 'Petite', 'Diesel', 'A Short sleeve polo by manufacturer Diesel', 70.00, 46.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1634, 'Short sleeve polo', 'Small', 'Diesel', 'A Short sleeve polo by manufacturer Diesel', 70.00, 46.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1635, 'Short sleeve polo', 'X-Large', 'Diesel', 'A Short sleeve polo by manufacturer Diesel', 70.00, 46.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1636, 'Jeans', 'Kids', 'Nununu', 'A Jeans by manufacturer Nununu', 44.00, 24.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1637, 'Jeans', 'Large', 'Nununu', 'A Jeans by manufacturer Nununu', 44.00, 24.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1638, 'Jeans', 'Medium', 'Nununu', 'A Jeans by manufacturer Nununu', 44.00, 24.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1639, 'Jeans', 'Petite', 'Nununu', 'A Jeans by manufacturer Nununu', 44.00, 24.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1640, 'Jeans', 'Small', 'Nununu', 'A Jeans by manufacturer Nununu', 44.00, 24.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1641, 'Jeans', 'X-Large', 'Nununu', 'A Jeans by manufacturer Nununu', 44.00, 24.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1642, 'Romper', 'Kids', 'Gap', 'A Romper by manufacturer Gap', 33.00, 97.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1643, 'Romper', 'Large', 'Gap', 'A Romper by manufacturer Gap', 33.00, 97.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1644, 'Romper', 'Medium', 'Gap', 'A Romper by manufacturer Gap', 33.00, 97.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1645, 'Romper', 'Petite', 'Gap', 'A Romper by manufacturer Gap', 33.00, 97.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1646, 'Romper', 'Small', 'Gap', 'A Romper by manufacturer Gap', 33.00, 97.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1647, 'Romper', 'X-Large', 'Gap', 'A Romper by manufacturer Gap', 33.00, 97.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1648, 'Beach sling', 'Kids', 'Izod', 'A Beach sling by manufacturer Izod', 96.00, 18.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1649, 'Beach sling', 'Large', 'Izod', 'A Beach sling by manufacturer Izod', 96.00, 18.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1650, 'Beach sling', 'Medium', 'Izod', 'A Beach sling by manufacturer Izod', 96.00, 18.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1651, 'Beach sling', 'Petite', 'Izod', 'A Beach sling by manufacturer Izod', 96.00, 18.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1652, 'Beach sling', 'Small', 'Izod', 'A Beach sling by manufacturer Izod', 96.00, 18.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1653, 'Beach sling', 'X-Large', 'Izod', 'A Beach sling by manufacturer Izod', 96.00, 18.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1654, 'Beach sling', 'Kids', 'Lacoste', 'A Beach sling by manufacturer Lacoste', 99.00, 69.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1655, 'Beach sling', 'Large', 'Lacoste', 'A Beach sling by manufacturer Lacoste', 99.00, 69.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1656, 'Beach sling', 'Medium', 'Lacoste', 'A Beach sling by manufacturer Lacoste', 99.00, 69.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1657, 'Beach sling', 'Petite', 'Lacoste', 'A Beach sling by manufacturer Lacoste', 99.00, 69.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1658, 'Beach sling', 'Small', 'Lacoste', 'A Beach sling by manufacturer Lacoste', 99.00, 69.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1659, 'Beach sling', 'X-Large', 'Lacoste', 'A Beach sling by manufacturer Lacoste', 99.00, 69.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1660, 'Sport coat', 'Kids', 'Prada', 'A Sport coat by manufacturer Prada', 66.00, 96.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1661, 'Sport coat', 'Large', 'Prada', 'A Sport coat by manufacturer Prada', 66.00, 96.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1662, 'Sport coat', 'Medium', 'Prada', 'A Sport coat by manufacturer Prada', 66.00, 96.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1663, 'Sport coat', 'Petite', 'Prada', 'A Sport coat by manufacturer Prada', 66.00, 96.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1664, 'Sport coat', 'Small', 'Prada', 'A Sport coat by manufacturer Prada', 66.00, 96.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1665, 'Sport coat', 'X-Large', 'Prada', 'A Sport coat by manufacturer Prada', 66.00, 96.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1666, 'Suspenders', 'Kids', 'Gymboree', 'A Suspenders by manufacturer Gymboree', 30.00, 41.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1667, 'Suspenders', 'Large', 'Gymboree', 'A Suspenders by manufacturer Gymboree', 30.00, 41.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1668, 'Suspenders', 'Medium', 'Gymboree', 'A Suspenders by manufacturer Gymboree', 30.00, 41.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1669, 'Suspenders', 'Petite', 'Gymboree', 'A Suspenders by manufacturer Gymboree', 30.00, 41.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1670, 'Suspenders', 'Small', 'Gymboree', 'A Suspenders by manufacturer Gymboree', 30.00, 41.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1671, 'Suspenders', 'X-Large', 'Gymboree', 'A Suspenders by manufacturer Gymboree', 30.00, 41.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1672, 'Romper', 'Kids', 'Puma', 'A Romper by manufacturer Puma', 96.00, 2.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1673, 'Romper', 'Large', 'Puma', 'A Romper by manufacturer Puma', 96.00, 2.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1674, 'Romper', 'Medium', 'Puma', 'A Romper by manufacturer Puma', 96.00, 2.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1675, 'Romper', 'Petite', 'Puma', 'A Romper by manufacturer Puma', 96.00, 2.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1676, 'Romper', 'Small', 'Puma', 'A Romper by manufacturer Puma', 96.00, 2.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1677, 'Romper', 'X-Large', 'Puma', 'A Romper by manufacturer Puma', 96.00, 2.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1678, 'Overalls', 'Kids', 'Versace', 'A Overalls by manufacturer Versace', 70.00, 26.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1679, 'Overalls', 'Large', 'Versace', 'A Overalls by manufacturer Versace', 70.00, 26.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1680, 'Overalls', 'Medium', 'Versace', 'A Overalls by manufacturer Versace', 70.00, 26.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1681, 'Overalls', 'Petite', 'Versace', 'A Overalls by manufacturer Versace', 70.00, 26.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1682, 'Overalls', 'Small', 'Versace', 'A Overalls by manufacturer Versace', 70.00, 26.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1683, 'Overalls', 'X-Large', 'Versace', 'A Overalls by manufacturer Versace', 70.00, 26.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1684, 'Tuxedo', 'Kids', 'Converse', 'A Tuxedo by manufacturer Converse', 44.00, 71.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1685, 'Tuxedo', 'Large', 'Converse', 'A Tuxedo by manufacturer Converse', 44.00, 71.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1686, 'Tuxedo', 'Medium', 'Converse', 'A Tuxedo by manufacturer Converse', 44.00, 71.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1687, 'Tuxedo', 'Petite', 'Converse', 'A Tuxedo by manufacturer Converse', 44.00, 71.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1688, 'Tuxedo', 'Small', 'Converse', 'A Tuxedo by manufacturer Converse', 44.00, 71.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1689, 'Tuxedo', 'X-Large', 'Converse', 'A Tuxedo by manufacturer Converse', 44.00, 71.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1690, 'V-neck t-shirt', 'Kids', 'Gucci', 'A V-neck t-shirt by manufacturer Gucci', 53.00, 13.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1691, 'V-neck t-shirt', 'Large', 'Gucci', 'A V-neck t-shirt by manufacturer Gucci', 53.00, 13.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1692, 'V-neck t-shirt', 'Medium', 'Gucci', 'A V-neck t-shirt by manufacturer Gucci', 53.00, 13.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1693, 'V-neck t-shirt', 'Petite', 'Gucci', 'A V-neck t-shirt by manufacturer Gucci', 53.00, 13.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1694, 'V-neck t-shirt', 'Small', 'Gucci', 'A V-neck t-shirt by manufacturer Gucci', 53.00, 13.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1695, 'V-neck t-shirt', 'X-Large', 'Gucci', 'A V-neck t-shirt by manufacturer Gucci', 53.00, 13.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1696, 'Bike short', 'Kids', 'Bellerose', 'A Bike short by manufacturer Bellerose', 55.00, 81.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1697, 'Bike short', 'Large', 'Bellerose', 'A Bike short by manufacturer Bellerose', 55.00, 81.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1698, 'Bike short', 'Medium', 'Bellerose', 'A Bike short by manufacturer Bellerose', 55.00, 81.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1699, 'Bike short', 'Petite', 'Bellerose', 'A Bike short by manufacturer Bellerose', 55.00, 81.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1700, 'Bike short', 'Small', 'Bellerose', 'A Bike short by manufacturer Bellerose', 55.00, 81.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1701, 'Bike short', 'X-Large', 'Bellerose', 'A Bike short by manufacturer Bellerose', 55.00, 81.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1702, 'Flannel shirt', 'Kids', 'Prada', 'A Flannel shirt by manufacturer Prada', 21.00, 2.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1703, 'Flannel shirt', 'Large', 'Prada', 'A Flannel shirt by manufacturer Prada', 21.00, 2.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1704, 'Flannel shirt', 'Medium', 'Prada', 'A Flannel shirt by manufacturer Prada', 21.00, 2.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1705, 'Flannel shirt', 'Petite', 'Prada', 'A Flannel shirt by manufacturer Prada', 21.00, 2.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1706, 'Flannel shirt', 'Small', 'Prada', 'A Flannel shirt by manufacturer Prada', 21.00, 2.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1707, 'Flannel shirt', 'X-Large', 'Prada', 'A Flannel shirt by manufacturer Prada', 21.00, 2.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1708, 'Flannel shirt', 'Kids', 'Gap', 'A Flannel shirt by manufacturer Gap', 59.00, 34.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1709, 'Flannel shirt', 'Large', 'Gap', 'A Flannel shirt by manufacturer Gap', 59.00, 34.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1710, 'Flannel shirt', 'Medium', 'Gap', 'A Flannel shirt by manufacturer Gap', 59.00, 34.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1711, 'Flannel shirt', 'Petite', 'Gap', 'A Flannel shirt by manufacturer Gap', 59.00, 34.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1712, 'Flannel shirt', 'Small', 'Gap', 'A Flannel shirt by manufacturer Gap', 59.00, 34.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1713, 'Flannel shirt', 'X-Large', 'Gap', 'A Flannel shirt by manufacturer Gap', 59.00, 34.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1714, 'Skinny jean', 'Kids', 'Fred Perry', 'A Skinny jean by manufacturer Fred Perry', 59.00, 17.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1715, 'Skinny jean', 'Large', 'Fred Perry', 'A Skinny jean by manufacturer Fred Perry', 59.00, 17.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1716, 'Skinny jean', 'Medium', 'Fred Perry', 'A Skinny jean by manufacturer Fred Perry', 59.00, 17.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1717, 'Skinny jean', 'Petite', 'Fred Perry', 'A Skinny jean by manufacturer Fred Perry', 59.00, 17.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1718, 'Skinny jean', 'Small', 'Fred Perry', 'A Skinny jean by manufacturer Fred Perry', 59.00, 17.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1719, 'Skinny jean', 'X-Large', 'Fred Perry', 'A Skinny jean by manufacturer Fred Perry', 59.00, 17.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1720, 'Onesy', 'Kids', 'Nununu', 'A Onesy by manufacturer Nununu', 40.00, 11.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1721, 'Onesy', 'Large', 'Nununu', 'A Onesy by manufacturer Nununu', 40.00, 11.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1722, 'Onesy', 'Medium', 'Nununu', 'A Onesy by manufacturer Nununu', 40.00, 11.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1723, 'Onesy', 'Petite', 'Nununu', 'A Onesy by manufacturer Nununu', 40.00, 11.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1724, 'Onesy', 'Small', 'Nununu', 'A Onesy by manufacturer Nununu', 40.00, 11.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1725, 'Onesy', 'X-Large', 'Nununu', 'A Onesy by manufacturer Nununu', 40.00, 11.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1726, 'Tuxedo', 'Kids', 'Bellerose', 'A Tuxedo by manufacturer Bellerose', 72.00, 43.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1727, 'Tuxedo', 'Large', 'Bellerose', 'A Tuxedo by manufacturer Bellerose', 72.00, 43.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1728, 'Tuxedo', 'Medium', 'Bellerose', 'A Tuxedo by manufacturer Bellerose', 72.00, 43.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1729, 'Tuxedo', 'Petite', 'Bellerose', 'A Tuxedo by manufacturer Bellerose', 72.00, 43.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1730, 'Tuxedo', 'Small', 'Bellerose', 'A Tuxedo by manufacturer Bellerose', 72.00, 43.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1731, 'Tuxedo', 'X-Large', 'Bellerose', 'A Tuxedo by manufacturer Bellerose', 72.00, 43.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1732, 'Bathing suit', 'Kids', 'Puma', 'A Bathing suit by manufacturer Puma', 70.00, 77.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1733, 'Bathing suit', 'Large', 'Puma', 'A Bathing suit by manufacturer Puma', 70.00, 77.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1734, 'Bathing suit', 'Medium', 'Puma', 'A Bathing suit by manufacturer Puma', 70.00, 77.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1735, 'Bathing suit', 'Petite', 'Puma', 'A Bathing suit by manufacturer Puma', 70.00, 77.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1736, 'Bathing suit', 'Small', 'Puma', 'A Bathing suit by manufacturer Puma', 70.00, 77.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1737, 'Bathing suit', 'X-Large', 'Puma', 'A Bathing suit by manufacturer Puma', 70.00, 77.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1738, 'Vest top', 'Kids', 'ZARA', 'A Vest top by manufacturer ZARA', 55.00, 42.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1739, 'Vest top', 'Large', 'ZARA', 'A Vest top by manufacturer ZARA', 55.00, 42.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1740, 'Vest top', 'Medium', 'ZARA', 'A Vest top by manufacturer ZARA', 55.00, 42.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1741, 'Vest top', 'Petite', 'ZARA', 'A Vest top by manufacturer ZARA', 55.00, 42.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1742, 'Vest top', 'Small', 'ZARA', 'A Vest top by manufacturer ZARA', 55.00, 42.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1743, 'Vest top', 'X-Large', 'ZARA', 'A Vest top by manufacturer ZARA', 55.00, 42.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1744, 'Romper', 'Kids', 'Gap', 'A Romper by manufacturer Gap', 26.00, 82.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1745, 'Romper', 'Large', 'Gap', 'A Romper by manufacturer Gap', 26.00, 82.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1746, 'Romper', 'Medium', 'Gap', 'A Romper by manufacturer Gap', 26.00, 82.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1747, 'Romper', 'Petite', 'Gap', 'A Romper by manufacturer Gap', 26.00, 82.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1748, 'Romper', 'Small', 'Gap', 'A Romper by manufacturer Gap', 26.00, 82.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1749, 'Romper', 'X-Large', 'Gap', 'A Romper by manufacturer Gap', 26.00, 82.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1750, 'Vest top', 'Kids', 'Polo', 'A Vest top by manufacturer Polo', 81.00, 81.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1751, 'Vest top', 'Large', 'Polo', 'A Vest top by manufacturer Polo', 81.00, 81.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1752, 'Vest top', 'Medium', 'Polo', 'A Vest top by manufacturer Polo', 81.00, 81.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1753, 'Vest top', 'Petite', 'Polo', 'A Vest top by manufacturer Polo', 81.00, 81.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1754, 'Vest top', 'Small', 'Polo', 'A Vest top by manufacturer Polo', 81.00, 81.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1755, 'Vest top', 'X-Large', 'Polo', 'A Vest top by manufacturer Polo', 81.00, 81.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1756, 'Jeans', 'Kids', 'Gymboree', 'A Jeans by manufacturer Gymboree', 45.00, 66.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1757, 'Jeans', 'Large', 'Gymboree', 'A Jeans by manufacturer Gymboree', 45.00, 66.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1758, 'Jeans', 'Medium', 'Gymboree', 'A Jeans by manufacturer Gymboree', 45.00, 66.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1759, 'Jeans', 'Petite', 'Gymboree', 'A Jeans by manufacturer Gymboree', 45.00, 66.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1760, 'Jeans', 'Small', 'Gymboree', 'A Jeans by manufacturer Gymboree', 45.00, 66.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1761, 'Jeans', 'X-Large', 'Gymboree', 'A Jeans by manufacturer Gymboree', 45.00, 66.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1762, 'Bathrobe', 'Kids', 'Nununu', 'A Bathrobe by manufacturer Nununu', 26.00, 65.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1763, 'Bathrobe', 'Large', 'Nununu', 'A Bathrobe by manufacturer Nununu', 26.00, 65.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1764, 'Bathrobe', 'Medium', 'Nununu', 'A Bathrobe by manufacturer Nununu', 26.00, 65.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1765, 'Bathrobe', 'Petite', 'Nununu', 'A Bathrobe by manufacturer Nununu', 26.00, 65.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1766, 'Bathrobe', 'Small', 'Nununu', 'A Bathrobe by manufacturer Nununu', 26.00, 65.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1767, 'Bathrobe', 'X-Large', 'Nununu', 'A Bathrobe by manufacturer Nununu', 26.00, 65.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1768, 'Cargo short', 'Kids', 'Izod', 'A Cargo short by manufacturer Izod', 65.00, 75.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1769, 'Cargo short', 'Large', 'Izod', 'A Cargo short by manufacturer Izod', 65.00, 75.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1770, 'Cargo short', 'Medium', 'Izod', 'A Cargo short by manufacturer Izod', 65.00, 75.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1771, 'Cargo short', 'Petite', 'Izod', 'A Cargo short by manufacturer Izod', 65.00, 75.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1772, 'Cargo short', 'Small', 'Izod', 'A Cargo short by manufacturer Izod', 65.00, 75.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1773, 'Cargo short', 'X-Large', 'Izod', 'A Cargo short by manufacturer Izod', 65.00, 75.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1774, 'Vest top', 'Kids', 'Adidas', 'A Vest top by manufacturer Adidas', 14.00, 94.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1775, 'Vest top', 'Large', 'Adidas', 'A Vest top by manufacturer Adidas', 14.00, 94.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1776, 'Vest top', 'Medium', 'Adidas', 'A Vest top by manufacturer Adidas', 14.00, 94.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1777, 'Vest top', 'Petite', 'Adidas', 'A Vest top by manufacturer Adidas', 14.00, 94.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1778, 'Vest top', 'Small', 'Adidas', 'A Vest top by manufacturer Adidas', 14.00, 94.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1779, 'Vest top', 'X-Large', 'Adidas', 'A Vest top by manufacturer Adidas', 14.00, 94.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1780, 'Cargo short', 'Kids', 'CLSC', 'A Cargo short by manufacturer CLSC', 7.00, 56.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1781, 'Cargo short', 'Large', 'CLSC', 'A Cargo short by manufacturer CLSC', 7.00, 56.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1782, 'Cargo short', 'Medium', 'CLSC', 'A Cargo short by manufacturer CLSC', 7.00, 56.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1783, 'Cargo short', 'Petite', 'CLSC', 'A Cargo short by manufacturer CLSC', 7.00, 56.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1784, 'Cargo short', 'Small', 'CLSC', 'A Cargo short by manufacturer CLSC', 7.00, 56.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1785, 'Cargo short', 'X-Large', 'CLSC', 'A Cargo short by manufacturer CLSC', 7.00, 56.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1786, 'Dress pants', 'Kids', 'CLSC', 'A Dress pants by manufacturer CLSC', 79.00, 60.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1787, 'Dress pants', 'Large', 'CLSC', 'A Dress pants by manufacturer CLSC', 79.00, 60.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1788, 'Dress pants', 'Medium', 'CLSC', 'A Dress pants by manufacturer CLSC', 79.00, 60.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1789, 'Dress pants', 'Petite', 'CLSC', 'A Dress pants by manufacturer CLSC', 79.00, 60.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1790, 'Dress pants', 'Small', 'CLSC', 'A Dress pants by manufacturer CLSC', 79.00, 60.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1791, 'Dress pants', 'X-Large', 'CLSC', 'A Dress pants by manufacturer CLSC', 79.00, 60.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1792, 'Overalls', 'Kids', 'Diesel', 'A Overalls by manufacturer Diesel', 75.00, 40.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1793, 'Overalls', 'Large', 'Diesel', 'A Overalls by manufacturer Diesel', 75.00, 40.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1794, 'Overalls', 'Medium', 'Diesel', 'A Overalls by manufacturer Diesel', 75.00, 40.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1795, 'Overalls', 'Petite', 'Diesel', 'A Overalls by manufacturer Diesel', 75.00, 40.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1796, 'Overalls', 'Small', 'Diesel', 'A Overalls by manufacturer Diesel', 75.00, 40.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1797, 'Overalls', 'X-Large', 'Diesel', 'A Overalls by manufacturer Diesel', 75.00, 40.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1798, 'Flannel shirt', 'Kids', 'Chanel', 'A Flannel shirt by manufacturer Chanel', 51.00, 79.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1799, 'Flannel shirt', 'Large', 'Chanel', 'A Flannel shirt by manufacturer Chanel', 51.00, 79.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1800, 'Flannel shirt', 'Medium', 'Chanel', 'A Flannel shirt by manufacturer Chanel', 51.00, 79.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1801, 'Flannel shirt', 'Petite', 'Chanel', 'A Flannel shirt by manufacturer Chanel', 51.00, 79.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1802, 'Flannel shirt', 'Small', 'Chanel', 'A Flannel shirt by manufacturer Chanel', 51.00, 79.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1803, 'Flannel shirt', 'X-Large', 'Chanel', 'A Flannel shirt by manufacturer Chanel', 51.00, 79.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1804, 'Sweatshirt', 'Kids', 'TinyCottons', 'A Sweatshirt by manufacturer TinyCottons', 73.00, 29.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1805, 'Sweatshirt', 'Large', 'TinyCottons', 'A Sweatshirt by manufacturer TinyCottons', 73.00, 29.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1806, 'Sweatshirt', 'Medium', 'TinyCottons', 'A Sweatshirt by manufacturer TinyCottons', 73.00, 29.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1807, 'Sweatshirt', 'Petite', 'TinyCottons', 'A Sweatshirt by manufacturer TinyCottons', 73.00, 29.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1808, 'Sweatshirt', 'Small', 'TinyCottons', 'A Sweatshirt by manufacturer TinyCottons', 73.00, 29.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1809, 'Sweatshirt', 'X-Large', 'TinyCottons', 'A Sweatshirt by manufacturer TinyCottons', 73.00, 29.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1810, 'Tank top', 'Kids', 'Fred Perry', 'A Tank top by manufacturer Fred Perry', 54.00, 99.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1811, 'Tank top', 'Large', 'Fred Perry', 'A Tank top by manufacturer Fred Perry', 54.00, 99.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1812, 'Tank top', 'Medium', 'Fred Perry', 'A Tank top by manufacturer Fred Perry', 54.00, 99.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1813, 'Tank top', 'Petite', 'Fred Perry', 'A Tank top by manufacturer Fred Perry', 54.00, 99.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1814, 'Tank top', 'Small', 'Fred Perry', 'A Tank top by manufacturer Fred Perry', 54.00, 99.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1815, 'Tank top', 'X-Large', 'Fred Perry', 'A Tank top by manufacturer Fred Perry', 54.00, 99.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1816, 'Rain jacket', 'Kids', 'Izod', 'A Rain jacket by manufacturer Izod', 83.00, 58.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1817, 'Rain jacket', 'Large', 'Izod', 'A Rain jacket by manufacturer Izod', 83.00, 58.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1818, 'Rain jacket', 'Medium', 'Izod', 'A Rain jacket by manufacturer Izod', 83.00, 58.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1819, 'Rain jacket', 'Petite', 'Izod', 'A Rain jacket by manufacturer Izod', 83.00, 58.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1820, 'Rain jacket', 'Small', 'Izod', 'A Rain jacket by manufacturer Izod', 83.00, 58.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1821, 'Rain jacket', 'X-Large', 'Izod', 'A Rain jacket by manufacturer Izod', 83.00, 58.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1822, 'Rain jacket', 'Kids', 'Diesel', 'A Rain jacket by manufacturer Diesel', 84.00, 38.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1823, 'Rain jacket', 'Large', 'Diesel', 'A Rain jacket by manufacturer Diesel', 84.00, 38.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1824, 'Rain jacket', 'Medium', 'Diesel', 'A Rain jacket by manufacturer Diesel', 84.00, 38.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1825, 'Rain jacket', 'Petite', 'Diesel', 'A Rain jacket by manufacturer Diesel', 84.00, 38.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1826, 'Rain jacket', 'Small', 'Diesel', 'A Rain jacket by manufacturer Diesel', 84.00, 38.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1827, 'Rain jacket', 'X-Large', 'Diesel', 'A Rain jacket by manufacturer Diesel', 84.00, 38.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1828, 'Wool hat', 'Kids', 'Polo', 'A Wool hat by manufacturer Polo', 2.00, 24.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1829, 'Wool hat', 'Large', 'Polo', 'A Wool hat by manufacturer Polo', 2.00, 24.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1830, 'Wool hat', 'Medium', 'Polo', 'A Wool hat by manufacturer Polo', 2.00, 24.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1831, 'Wool hat', 'Petite', 'Polo', 'A Wool hat by manufacturer Polo', 2.00, 24.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1832, 'Wool hat', 'Small', 'Polo', 'A Wool hat by manufacturer Polo', 2.00, 24.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1833, 'Wool hat', 'X-Large', 'Polo', 'A Wool hat by manufacturer Polo', 2.00, 24.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1834, 'Bike short', 'Kids', 'Chanel', 'A Bike short by manufacturer Chanel', 12.00, 61.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1835, 'Bike short', 'Large', 'Chanel', 'A Bike short by manufacturer Chanel', 12.00, 61.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1836, 'Bike short', 'Medium', 'Chanel', 'A Bike short by manufacturer Chanel', 12.00, 61.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1837, 'Bike short', 'Petite', 'Chanel', 'A Bike short by manufacturer Chanel', 12.00, 61.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1838, 'Bike short', 'Small', 'Chanel', 'A Bike short by manufacturer Chanel', 12.00, 61.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1839, 'Bike short', 'X-Large', 'Chanel', 'A Bike short by manufacturer Chanel', 12.00, 61.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1840, 'Sport briefs', 'Kids', 'Aeropostale', 'A Sport briefs by manufacturer Aeropostale', 94.00, 32.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1841, 'Sport briefs', 'Large', 'Aeropostale', 'A Sport briefs by manufacturer Aeropostale', 94.00, 32.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1842, 'Sport briefs', 'Medium', 'Aeropostale', 'A Sport briefs by manufacturer Aeropostale', 94.00, 32.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1843, 'Sport briefs', 'Petite', 'Aeropostale', 'A Sport briefs by manufacturer Aeropostale', 94.00, 32.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1844, 'Sport briefs', 'Small', 'Aeropostale', 'A Sport briefs by manufacturer Aeropostale', 94.00, 32.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1845, 'Sport briefs', 'X-Large', 'Aeropostale', 'A Sport briefs by manufacturer Aeropostale', 94.00, 32.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1846, 'Bathrobe', 'Kids', 'Dior', 'A Bathrobe by manufacturer Dior', 18.00, 1.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1847, 'Bathrobe', 'Large', 'Dior', 'A Bathrobe by manufacturer Dior', 18.00, 1.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1848, 'Bathrobe', 'Medium', 'Dior', 'A Bathrobe by manufacturer Dior', 18.00, 1.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1849, 'Bathrobe', 'Petite', 'Dior', 'A Bathrobe by manufacturer Dior', 18.00, 1.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1850, 'Bathrobe', 'Small', 'Dior', 'A Bathrobe by manufacturer Dior', 18.00, 1.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1851, 'Bathrobe', 'X-Large', 'Dior', 'A Bathrobe by manufacturer Dior', 18.00, 1.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1852, 'Yoga skort', 'Kids', 'Nike', 'A Yoga skort by manufacturer Nike', 30.00, 84.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1853, 'Yoga skort', 'Large', 'Nike', 'A Yoga skort by manufacturer Nike', 30.00, 84.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1854, 'Yoga skort', 'Medium', 'Nike', 'A Yoga skort by manufacturer Nike', 30.00, 84.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1855, 'Yoga skort', 'Petite', 'Nike', 'A Yoga skort by manufacturer Nike', 30.00, 84.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1856, 'Yoga skort', 'Small', 'Nike', 'A Yoga skort by manufacturer Nike', 30.00, 84.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1857, 'Yoga skort', 'X-Large', 'Nike', 'A Yoga skort by manufacturer Nike', 30.00, 84.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1858, 'Cargo short', 'Kids', 'Chanel', 'A Cargo short by manufacturer Chanel', 98.00, 5.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1859, 'Cargo short', 'Large', 'Chanel', 'A Cargo short by manufacturer Chanel', 98.00, 5.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1860, 'Cargo short', 'Medium', 'Chanel', 'A Cargo short by manufacturer Chanel', 98.00, 5.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1861, 'Cargo short', 'Petite', 'Chanel', 'A Cargo short by manufacturer Chanel', 98.00, 5.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1862, 'Cargo short', 'Small', 'Chanel', 'A Cargo short by manufacturer Chanel', 98.00, 5.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1863, 'Cargo short', 'X-Large', 'Chanel', 'A Cargo short by manufacturer Chanel', 98.00, 5.00, 1002 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1864, 'Skinny jean', 'Kids', 'Diesel', 'A Skinny jean by manufacturer Diesel', 76.00, 81.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1865, 'Skinny jean', 'Large', 'Diesel', 'A Skinny jean by manufacturer Diesel', 76.00, 81.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1866, 'Skinny jean', 'Medium', 'Diesel', 'A Skinny jean by manufacturer Diesel', 76.00, 81.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1867, 'Skinny jean', 'Petite', 'Diesel', 'A Skinny jean by manufacturer Diesel', 76.00, 81.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1868, 'Skinny jean', 'Small', 'Diesel', 'A Skinny jean by manufacturer Diesel', 76.00, 81.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1869, 'Skinny jean', 'X-Large', 'Diesel', 'A Skinny jean by manufacturer Diesel', 76.00, 81.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1870, 'Ballet skirt', 'Kids', 'Aeropostale', 'A Ballet skirt by manufacturer Aeropostale', 30.00, 30.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1871, 'Ballet skirt', 'Large', 'Aeropostale', 'A Ballet skirt by manufacturer Aeropostale', 30.00, 30.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1872, 'Ballet skirt', 'Medium', 'Aeropostale', 'A Ballet skirt by manufacturer Aeropostale', 30.00, 30.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1873, 'Ballet skirt', 'Petite', 'Aeropostale', 'A Ballet skirt by manufacturer Aeropostale', 30.00, 30.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1874, 'Ballet skirt', 'Small', 'Aeropostale', 'A Ballet skirt by manufacturer Aeropostale', 30.00, 30.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1875, 'Ballet skirt', 'X-Large', 'Aeropostale', 'A Ballet skirt by manufacturer Aeropostale', 30.00, 30.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1876, 'Jeans', 'Kids', 'ZARA', 'A Jeans by manufacturer ZARA', 21.00, 87.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1877, 'Jeans', 'Large', 'ZARA', 'A Jeans by manufacturer ZARA', 21.00, 87.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1878, 'Jeans', 'Medium', 'ZARA', 'A Jeans by manufacturer ZARA', 21.00, 87.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1879, 'Jeans', 'Petite', 'ZARA', 'A Jeans by manufacturer ZARA', 21.00, 87.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1880, 'Jeans', 'Small', 'ZARA', 'A Jeans by manufacturer ZARA', 21.00, 87.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1881, 'Jeans', 'X-Large', 'ZARA', 'A Jeans by manufacturer ZARA', 21.00, 87.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1882, 'Tank top', 'Kids', 'J.Crew', 'A Tank top by manufacturer J.Crew', 56.00, 86.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1883, 'Tank top', 'Large', 'J.Crew', 'A Tank top by manufacturer J.Crew', 56.00, 86.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1884, 'Tank top', 'Medium', 'J.Crew', 'A Tank top by manufacturer J.Crew', 56.00, 86.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1885, 'Tank top', 'Petite', 'J.Crew', 'A Tank top by manufacturer J.Crew', 56.00, 86.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1886, 'Tank top', 'Small', 'J.Crew', 'A Tank top by manufacturer J.Crew', 56.00, 86.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1887, 'Tank top', 'X-Large', 'J.Crew', 'A Tank top by manufacturer J.Crew', 56.00, 86.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1888, 'Vest top', 'Kids', 'Carhartt', 'A Vest top by manufacturer Carhartt', 54.00, 91.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1889, 'Vest top', 'Large', 'Carhartt', 'A Vest top by manufacturer Carhartt', 54.00, 91.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1890, 'Vest top', 'Medium', 'Carhartt', 'A Vest top by manufacturer Carhartt', 54.00, 91.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1891, 'Vest top', 'Petite', 'Carhartt', 'A Vest top by manufacturer Carhartt', 54.00, 91.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1892, 'Vest top', 'Small', 'Carhartt', 'A Vest top by manufacturer Carhartt', 54.00, 91.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1893, 'Vest top', 'X-Large', 'Carhartt', 'A Vest top by manufacturer Carhartt', 54.00, 91.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1894, 'Bathrobe', 'Kids', 'J.Crew', 'A Bathrobe by manufacturer J.Crew', 4.00, 35.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1895, 'Bathrobe', 'Large', 'J.Crew', 'A Bathrobe by manufacturer J.Crew', 4.00, 35.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1896, 'Bathrobe', 'Medium', 'J.Crew', 'A Bathrobe by manufacturer J.Crew', 4.00, 35.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1897, 'Bathrobe', 'Petite', 'J.Crew', 'A Bathrobe by manufacturer J.Crew', 4.00, 35.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1898, 'Bathrobe', 'Small', 'J.Crew', 'A Bathrobe by manufacturer J.Crew', 4.00, 35.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1899, 'Bathrobe', 'X-Large', 'J.Crew', 'A Bathrobe by manufacturer J.Crew', 4.00, 35.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1900, 'Pajama pants', 'Kids', 'J.Crew', 'A Pajama pants by manufacturer J.Crew', 7.00, 43.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1901, 'Pajama pants', 'Large', 'J.Crew', 'A Pajama pants by manufacturer J.Crew', 7.00, 43.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1902, 'Pajama pants', 'Medium', 'J.Crew', 'A Pajama pants by manufacturer J.Crew', 7.00, 43.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1903, 'Pajama pants', 'Petite', 'J.Crew', 'A Pajama pants by manufacturer J.Crew', 7.00, 43.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1904, 'Pajama pants', 'Small', 'J.Crew', 'A Pajama pants by manufacturer J.Crew', 7.00, 43.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1905, 'Pajama pants', 'X-Large', 'J.Crew', 'A Pajama pants by manufacturer J.Crew', 7.00, 43.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1906, 'Ballet skirt', 'Kids', 'Prada', 'A Ballet skirt by manufacturer Prada', 12.00, 85.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1907, 'Ballet skirt', 'Large', 'Prada', 'A Ballet skirt by manufacturer Prada', 12.00, 85.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1908, 'Ballet skirt', 'Medium', 'Prada', 'A Ballet skirt by manufacturer Prada', 12.00, 85.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1909, 'Ballet skirt', 'Petite', 'Prada', 'A Ballet skirt by manufacturer Prada', 12.00, 85.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1910, 'Ballet skirt', 'Small', 'Prada', 'A Ballet skirt by manufacturer Prada', 12.00, 85.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1911, 'Ballet skirt', 'X-Large', 'Prada', 'A Ballet skirt by manufacturer Prada', 12.00, 85.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1912, 'Bathrobe', 'Kids', 'TinyCottons', 'A Bathrobe by manufacturer TinyCottons', 17.00, 43.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1913, 'Bathrobe', 'Large', 'TinyCottons', 'A Bathrobe by manufacturer TinyCottons', 17.00, 43.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1914, 'Bathrobe', 'Medium', 'TinyCottons', 'A Bathrobe by manufacturer TinyCottons', 17.00, 43.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1915, 'Bathrobe', 'Petite', 'TinyCottons', 'A Bathrobe by manufacturer TinyCottons', 17.00, 43.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1916, 'Bathrobe', 'Small', 'TinyCottons', 'A Bathrobe by manufacturer TinyCottons', 17.00, 43.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1917, 'Bathrobe', 'X-Large', 'TinyCottons', 'A Bathrobe by manufacturer TinyCottons', 17.00, 43.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1918, 'Onesy', 'Kids', 'Levi''s', 'A Onesy by manufacturer Levi''s', 43.00, 53.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1919, 'Onesy', 'Large', 'Levi''s', 'A Onesy by manufacturer Levi''s', 43.00, 53.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1920, 'Onesy', 'Medium', 'Levi''s', 'A Onesy by manufacturer Levi''s', 43.00, 53.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1921, 'Onesy', 'Petite', 'Levi''s', 'A Onesy by manufacturer Levi''s', 43.00, 53.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1922, 'Onesy', 'Small', 'Levi''s', 'A Onesy by manufacturer Levi''s', 43.00, 53.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1923, 'Onesy', 'X-Large', 'Levi''s', 'A Onesy by manufacturer Levi''s', 43.00, 53.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1924, 'Dress pants', 'Kids', 'Fred Perry', 'A Dress pants by manufacturer Fred Perry', 64.00, 37.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1925, 'Dress pants', 'Large', 'Fred Perry', 'A Dress pants by manufacturer Fred Perry', 64.00, 37.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1926, 'Dress pants', 'Medium', 'Fred Perry', 'A Dress pants by manufacturer Fred Perry', 64.00, 37.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1927, 'Dress pants', 'Petite', 'Fred Perry', 'A Dress pants by manufacturer Fred Perry', 64.00, 37.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1928, 'Dress pants', 'Small', 'Fred Perry', 'A Dress pants by manufacturer Fred Perry', 64.00, 37.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1929, 'Dress pants', 'X-Large', 'Fred Perry', 'A Dress pants by manufacturer Fred Perry', 64.00, 37.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1930, 'Vest top', 'Kids', 'Aeropostale', 'A Vest top by manufacturer Aeropostale', 72.00, 10.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1931, 'Vest top', 'Large', 'Aeropostale', 'A Vest top by manufacturer Aeropostale', 72.00, 10.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1932, 'Vest top', 'Medium', 'Aeropostale', 'A Vest top by manufacturer Aeropostale', 72.00, 10.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1933, 'Vest top', 'Petite', 'Aeropostale', 'A Vest top by manufacturer Aeropostale', 72.00, 10.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1934, 'Vest top', 'Small', 'Aeropostale', 'A Vest top by manufacturer Aeropostale', 72.00, 10.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1935, 'Vest top', 'X-Large', 'Aeropostale', 'A Vest top by manufacturer Aeropostale', 72.00, 10.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1936, 'Bathing suit', 'Kids', 'Gucci', 'A Bathing suit by manufacturer Gucci', 71.00, 68.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1937, 'Bathing suit', 'Large', 'Gucci', 'A Bathing suit by manufacturer Gucci', 71.00, 68.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1938, 'Bathing suit', 'Medium', 'Gucci', 'A Bathing suit by manufacturer Gucci', 71.00, 68.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1939, 'Bathing suit', 'Petite', 'Gucci', 'A Bathing suit by manufacturer Gucci', 71.00, 68.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1940, 'Bathing suit', 'Small', 'Gucci', 'A Bathing suit by manufacturer Gucci', 71.00, 68.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1941, 'Bathing suit', 'X-Large', 'Gucci', 'A Bathing suit by manufacturer Gucci', 71.00, 68.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1942, 'Dress socks', 'Kids', 'Izod', 'A Dress socks by manufacturer Izod', 93.00, 89.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1943, 'Dress socks', 'Large', 'Izod', 'A Dress socks by manufacturer Izod', 93.00, 89.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1944, 'Dress socks', 'Medium', 'Izod', 'A Dress socks by manufacturer Izod', 93.00, 89.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1945, 'Dress socks', 'Petite', 'Izod', 'A Dress socks by manufacturer Izod', 93.00, 89.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1946, 'Dress socks', 'Small', 'Izod', 'A Dress socks by manufacturer Izod', 93.00, 89.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1947, 'Dress socks', 'X-Large', 'Izod', 'A Dress socks by manufacturer Izod', 93.00, 89.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1948, 'Beach sling', 'Kids', 'Acrylick', 'A Beach sling by manufacturer Acrylick', 30.00, 6.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1949, 'Beach sling', 'Large', 'Acrylick', 'A Beach sling by manufacturer Acrylick', 30.00, 6.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1950, 'Beach sling', 'Medium', 'Acrylick', 'A Beach sling by manufacturer Acrylick', 30.00, 6.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1951, 'Beach sling', 'Petite', 'Acrylick', 'A Beach sling by manufacturer Acrylick', 30.00, 6.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1952, 'Beach sling', 'Small', 'Acrylick', 'A Beach sling by manufacturer Acrylick', 30.00, 6.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1953, 'Beach sling', 'X-Large', 'Acrylick', 'A Beach sling by manufacturer Acrylick', 30.00, 6.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1954, 'Beach sling', 'Kids', 'Nununu', 'A Beach sling by manufacturer Nununu', 79.00, 6.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1955, 'Beach sling', 'Large', 'Nununu', 'A Beach sling by manufacturer Nununu', 79.00, 6.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1956, 'Beach sling', 'Medium', 'Nununu', 'A Beach sling by manufacturer Nununu', 79.00, 6.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1957, 'Beach sling', 'Petite', 'Nununu', 'A Beach sling by manufacturer Nununu', 79.00, 6.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1958, 'Beach sling', 'Small', 'Nununu', 'A Beach sling by manufacturer Nununu', 79.00, 6.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1959, 'Beach sling', 'X-Large', 'Nununu', 'A Beach sling by manufacturer Nununu', 79.00, 6.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1960, 'Yoga skort', 'Kids', 'Armani', 'A Yoga skort by manufacturer Armani', 51.00, 35.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1961, 'Yoga skort', 'Large', 'Armani', 'A Yoga skort by manufacturer Armani', 51.00, 35.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1962, 'Yoga skort', 'Medium', 'Armani', 'A Yoga skort by manufacturer Armani', 51.00, 35.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1963, 'Yoga skort', 'Petite', 'Armani', 'A Yoga skort by manufacturer Armani', 51.00, 35.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1964, 'Yoga skort', 'Small', 'Armani', 'A Yoga skort by manufacturer Armani', 51.00, 35.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1965, 'Yoga skort', 'X-Large', 'Armani', 'A Yoga skort by manufacturer Armani', 51.00, 35.00, 1004 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1966, 'Yoga skort', 'Kids', 'Adidas', 'A Yoga skort by manufacturer Adidas', 19.00, 54.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1967, 'Yoga skort', 'Large', 'Adidas', 'A Yoga skort by manufacturer Adidas', 19.00, 54.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1968, 'Yoga skort', 'Medium', 'Adidas', 'A Yoga skort by manufacturer Adidas', 19.00, 54.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1969, 'Yoga skort', 'Petite', 'Adidas', 'A Yoga skort by manufacturer Adidas', 19.00, 54.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1970, 'Yoga skort', 'Small', 'Adidas', 'A Yoga skort by manufacturer Adidas', 19.00, 54.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1971, 'Yoga skort', 'X-Large', 'Adidas', 'A Yoga skort by manufacturer Adidas', 19.00, 54.00, 1000 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1972, 'Rain jacket', 'Kids', 'Izod', 'A Rain jacket by manufacturer Izod', 91.00, 59.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1973, 'Rain jacket', 'Large', 'Izod', 'A Rain jacket by manufacturer Izod', 91.00, 59.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1974, 'Rain jacket', 'Medium', 'Izod', 'A Rain jacket by manufacturer Izod', 91.00, 59.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1975, 'Rain jacket', 'Petite', 'Izod', 'A Rain jacket by manufacturer Izod', 91.00, 59.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1976, 'Rain jacket', 'Small', 'Izod', 'A Rain jacket by manufacturer Izod', 91.00, 59.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1977, 'Rain jacket', 'X-Large', 'Izod', 'A Rain jacket by manufacturer Izod', 91.00, 59.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1978, 'Sport briefs', 'Kids', 'Nununu', 'A Sport briefs by manufacturer Nununu', 9.00, 58.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1979, 'Sport briefs', 'Large', 'Nununu', 'A Sport briefs by manufacturer Nununu', 9.00, 58.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1980, 'Sport briefs', 'Medium', 'Nununu', 'A Sport briefs by manufacturer Nununu', 9.00, 58.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1981, 'Sport briefs', 'Petite', 'Nununu', 'A Sport briefs by manufacturer Nununu', 9.00, 58.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1982, 'Sport briefs', 'Small', 'Nununu', 'A Sport briefs by manufacturer Nununu', 9.00, 58.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1983, 'Sport briefs', 'X-Large', 'Nununu', 'A Sport briefs by manufacturer Nununu', 9.00, 58.00, 1001 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1984, 'Bike short', 'Kids', 'Hugo Boss', 'A Bike short by manufacturer Hugo Boss', 33.00, 3.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1985, 'Bike short', 'Large', 'Hugo Boss', 'A Bike short by manufacturer Hugo Boss', 33.00, 3.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1986, 'Bike short', 'Medium', 'Hugo Boss', 'A Bike short by manufacturer Hugo Boss', 33.00, 3.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1987, 'Bike short', 'Petite', 'Hugo Boss', 'A Bike short by manufacturer Hugo Boss', 33.00, 3.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1988, 'Bike short', 'Small', 'Hugo Boss', 'A Bike short by manufacturer Hugo Boss', 33.00, 3.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1989, 'Bike short', 'X-Large', 'Hugo Boss', 'A Bike short by manufacturer Hugo Boss', 33.00, 3.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1990, 'Dress socks', 'Kids', 'Izod', 'A Dress socks by manufacturer Izod', 66.00, 75.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1991, 'Dress socks', 'Large', 'Izod', 'A Dress socks by manufacturer Izod', 66.00, 75.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1992, 'Dress socks', 'Medium', 'Izod', 'A Dress socks by manufacturer Izod', 66.00, 75.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1993, 'Dress socks', 'Petite', 'Izod', 'A Dress socks by manufacturer Izod', 66.00, 75.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1994, 'Dress socks', 'Small', 'Izod', 'A Dress socks by manufacturer Izod', 66.00, 75.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1995, 'Dress socks', 'X-Large', 'Izod', 'A Dress socks by manufacturer Izod', 66.00, 75.00, 1003 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1996, 'Beach sling', 'Kids', 'Bellerose', 'A Beach sling by manufacturer Bellerose', 43.00, 55.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1997, 'Beach sling', 'Large', 'Bellerose', 'A Beach sling by manufacturer Bellerose', 43.00, 55.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1998, 'Beach sling', 'Medium', 'Bellerose', 'A Beach sling by manufacturer Bellerose', 43.00, 55.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 1999, 'Beach sling', 'Petite', 'Bellerose', 'A Beach sling by manufacturer Bellerose', 43.00, 55.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 2000, 'Beach sling', 'Small', 'Bellerose', 'A Beach sling by manufacturer Bellerose', 43.00, 55.00, 1005 )
go
INSERT INTO Product ( "productCode", "productName", "productSize", "productVendor", "productDescription", "buyPrice", "MSRP", "departmentCode") VALUES ( 2001, 'Beach sling', 'X-Large', 'Bellerose', 'A Beach sling by manufacturer Bellerose', 43.00, 55.00, 1005 )
go

/* Data for Inventory table */

INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1103, 464 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1079, 64 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1412, 860 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1900, 165 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1173, 1979 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1949, 113 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1115, 1511 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1040, 1658 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1281, 1382 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1780, 280 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1872, 756 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1272, 1566 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1072, 1672 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1075, 1476 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1654, 574 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1343, 1515 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1443, 544 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1411, 1960 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1109, 690 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1537, 49 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1876, 904 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1905, 1719 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1985, 1252 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1895, 1565 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1268, 1470 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1513, 1709 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1481, 298 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1174, 1514 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1616, 1521 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1250, 466 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1587, 1004 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1635, 980 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1356, 627 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1209, 1151 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1437, 1023 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1818, 866 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1452, 29 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1195, 1476 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1740, 1276 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1565, 1312 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1930, 366 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1820, 3 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1783, 988 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1070, 396 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1529, 539 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1992, 841 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1145, 227 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1243, 1851 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1852, 186 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1485, 1575 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1821, 1544 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1959, 730 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1116, 626 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1280, 138 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1065, 1316 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1929, 656 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1914, 124 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1859, 1125 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1465, 1961 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1745, 1574 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1111, 1929 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1546, 541 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1534, 243 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1543, 1886 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1544, 516 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1169, 417 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1275, 107 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1756, 1802 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1041, 1407 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1670, 1872 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1819, 1662 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1506, 223 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1101, 943 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1978, 1410 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1332, 1474 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1607, 409 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1107, 782 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1100, 953 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1221, 866 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1346, 861 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1816, 259 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1353, 783 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1917, 1243 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1427, 1196 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1721, 1125 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1135, 90 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1055, 1693 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1477, 1539 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1095, 334 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1862, 110 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1850, 1174 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1155, 264 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1013, 796 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1879, 1238 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1057, 607 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1641, 1247 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1455, 1310 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1383, 1150 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1019, 1748 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1244, 1708 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1466, 1074 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1774, 1348 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1972, 1669 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1382, 1276 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1778, 272 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1907, 714 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1430, 741 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1668, 878 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1591, 494 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1049, 127 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1060, 1860 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1329, 1113 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1059, 1460 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1617, 672 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1542, 705 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1915, 1092 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1162, 886 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1323, 192 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1188, 579 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1979, 769 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1990, 262 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1958, 624 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1954, 797 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1066, 1350 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1367, 820 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1027, 1261 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1787, 1438 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1032, 1922 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1527, 664 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1213, 1307 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1627, 1715 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1830, 331 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1077, 944 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1697, 107 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1220, 227 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1765, 194 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1526, 593 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1083, 1500 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1159, 55 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1164, 1236 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1863, 880 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1061, 716 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1769, 1510 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1618, 1560 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1809, 1511 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1690, 843 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1371, 1438 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1687, 784 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1456, 1463 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1524, 1134 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1498, 1104 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1984, 64 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1896, 708 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1293, 1215 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1508, 941 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1279, 1070 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1814, 911 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1739, 1462 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1500, 84 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1771, 493 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1491, 1739 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1044, 1436 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1731, 352 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1071, 1435 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1705, 1943 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1433, 113 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1810, 187 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1866, 677 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1791, 597 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1312, 205 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1395, 701 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1968, 1096 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1324, 1434 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1199, 14 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1924, 1446 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1251, 212 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1640, 263 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1000, 709 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1236, 1852 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1663, 1112 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1725, 1635 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1911, 512 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1257, 637 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1700, 1165 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1662, 1677 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1035, 781 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1548, 133 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1977, 78 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1553, 370 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1441, 1104 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1629, 336 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1884, 1398 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1523, 534 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1315, 1321 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1502, 1872 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1042, 924 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1237, 805 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1742, 1313 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1003, 940 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1480, 132 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1259, 826 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1505, 288 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1856, 536 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1987, 584 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1746, 486 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1880, 643 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1779, 1682 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1490, 504 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1203, 1381 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1792, 792 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1800, 182 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1417, 438 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1788, 1561 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1556, 596 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1238, 771 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1875, 395 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1303, 325 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1916, 97 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1022, 1653 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1582, 1237 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1619, 1418 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1351, 637 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1150, 1122 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1196, 677 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1728, 437 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1786, 1014 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1365, 180 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1610, 333 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1827, 1954 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1822, 157 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1633, 1365 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1730, 174 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1260, 1237 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1517, 913 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1698, 227 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1737, 1041 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1734, 448 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1098, 534 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1707, 1667 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1363, 484 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1801, 707 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1463, 433 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1179, 932 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1694, 1371 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1712, 1072 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1183, 1561 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1408, 42 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1193, 156 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1946, 1429 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1026, 1390 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1585, 1994 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1029, 133 )
go
INSERT INTO "Inventory" ( "storeId", "productId", "quantity" ) VALUES ( 9012, 1449, 1205 )
go

commit work
go
