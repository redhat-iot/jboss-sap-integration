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
  ,"quantityInStock" bigint NOT NULL
  ,"buyPrice" numeric(6,2) NOT NULL
  ,"MSRP"  numeric(6,2) NOT NULL
  ,"departmentCode"   long varchar  NOT NULL
  ,PRIMARY KEY ("productCode" ASC)
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


/*Random generated products*/

INSERT INTO Product("productCode","productName","productSize","productVendor","productDescription","quantityInStock","buyPrice","MSRP","departmentCode") VALUES(1050,'Lorem ipsum dolor sit','46','Facilisis Incorporated','accumsan',59,'0.45','73.28',1004),(1051,'Lorem ipsum dolor sit amet,','34','Erat Vivamus Nisi LLC','hendrerit.',62,'0.02','59.04',1005),(1052,'Lorem ipsum dolor','12','Molestie Arcu Sed Company','accumsan convallis, ante',46,'0.45','64.86',1000),(1053,'Lorem ipsum dolor','42','Sed Turpis Foundation','ipsum.',30,'0.13','60.30',1002),(1054,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Curabitur sed','50','Mauris Limited','mauris eu elit. Nulla facilisi. Sed',98,'0.31','75.54',1003),(1055,'Lorem ipsum dolor sit','38','Mauris Molestie Industries','lacus. Etiam bibendum fermentum',88,'0.45','61.10',1003),(1056,'Lorem ipsum dolor','50','Tempus Lorem Institute','tempus scelerisque, lorem ipsum sodales purus, in molestie',61,'0.18','72.67',1003),(1057,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit.','2','Volutpat Nunc Company','lobortis, nisi nibh lacinia orci,',76,'0.49','65.75',1000),(1058,'Lorem ipsum','8','Aliquam Fringilla Consulting','a, facilisis non, bibendum',84,'0.04','95.45',1003),(1059,'Lorem ipsum dolor','18','In Inc.','eu eros. Nam consequat dolor vitae dolor. Donec fringilla. Donec',50,'0.19','84.60',1002),(1060,'Lorem','12','Ut Foundation','eu elit. Nulla facilisi. Sed neque.',97,'0.11','55.92',1003),(1061,'Lorem','22','Hendrerit Consectetuer Cursus LLC','penatibus et magnis dis',81,'0.30','88.69',1004),(1062,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Curabitur','16','Nec Euismod In Consulting','Suspendisse aliquet, sem ut cursus luctus, ipsum leo',60,'0.05','97.15',1002),(1063,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Curabitur','20','Vitae Mauris Corporation','sollicitudin orci sem eget massa. Suspendisse eleifend. Cras sed leo.',65,'0.25','56.80',1000),(1064,'Lorem ipsum dolor sit','20','Quisque Porttitor Eros Limited','odio. Etiam ligula tortor, dictum eu, placerat eget, venenatis',86,'0.34','83.52',1005),(1065,'Lorem ipsum dolor','48','Mi Aliquam Gravida Limited','iaculis odio. Nam interdum enim non',84,'0.20','76.80',1003),(1066,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit.','36','Dis Parturient Institute','amet ornare lectus justo',61,'0.05','94.96',1002),(1067,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Curabitur','10','At Egestas A LLC','amet, risus. Donec nibh enim, gravida sit',91,'0.37','90.00',1003),(1068,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit.','34','Nunc Institute','lectus. Nullam suscipit, est ac facilisis',99,'0.04','73.98',1001),(1069,'Lorem ipsum dolor sit amet,','8','Pede Praesent Eu Incorporated','mi tempor lorem, eget mollis',41,'0.38','52.91',1003),(1070,'Lorem ipsum dolor sit amet, consectetuer adipiscing','32','Ante Iaculis Nec LLP','varius ultrices, mauris',22,'0.21','87.26',1001),(1071,'Lorem ipsum dolor sit amet,','46','Sapien Company','ac metus vitae velit',51,'0.41','67.99',1002),(1072,'Lorem ipsum dolor sit amet,','6','Aliquam Rutrum Associates','eu, odio.',18,'0.43','60.56',1001),(1073,'Lorem ipsum dolor sit amet,','10','Justo Sit LLC','scelerisque',24,'0.29','94.61',1005),(1074,'Lorem ipsum dolor sit amet,','38','Egestas Ligula Inc.','aliquet diam. Sed',72,'0.39','72.18',1004),(1075,'Lorem ipsum dolor sit amet, consectetuer adipiscing','32','Donec Company','quam vel sapien imperdiet ornare. In faucibus.',36,'0.08','81.93',1004),(1076,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Curabitur','22','Rhoncus PC','pharetra nibh. Aliquam ornare,',0,'0.10','97.04',1005),(1077,'Lorem ipsum dolor sit amet,','42','Risus Nulla Eget PC','mollis. Integer tincidunt aliquam arcu. Aliquam',28,'0.40','99.77',1002),(1078,'Lorem ipsum dolor sit amet, consectetuer','50','Morbi Non Company','senectus et netus et',50,'0.38','53.33',1001),(1079,'Lorem ipsum dolor sit amet,','8','Aliquet Ltd','a, magna. Lorem ipsum dolor sit amet,',83,'0.26','74.22',1004),(1080,'Lorem ipsum dolor sit amet, consectetuer adipiscing','16','Purus In Inc.','a, magna. Lorem ipsum',0,'0.42','84.38',1003),(1081,'Lorem ipsum dolor sit','46','Nisi Aenean Eget Company','nunc sit amet metus. Aliquam erat',59,'0.29','53.41',1000),(1082,'Lorem ipsum dolor sit amet, consectetuer adipiscing','42','Gravida Praesent Eu LLC','sodales at, velit. Pellentesque ultricies dignissim',15,'0.46','91.11',1002),(1083,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit.','4','Mattis Velit Consulting','Donec vitae erat vel pede blandit congue. In',97,'0.15','82.37',1002),(1084,'Lorem ipsum dolor sit amet, consectetuer','38','Fringilla LLC','neque. Nullam nisl. Maecenas malesuada fringilla est. Mauris eu turpis.',92,'0.33','98.29',1005),(1085,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Curabitur sed','40','Vel LLP','ac mattis velit justo nec ante. Maecenas mi felis,',94,'0.41','65.22',1002),(1086,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit.','34','Sed Congue Elit Corporation','vel arcu. Curabitur ut odio vel est tempor',29,'0.48','76.20',1001),(1087,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Curabitur sed','4','Neque Venenatis Lacus Incorporated','Duis dignissim tempor arcu.',86,'0.03','88.75',1000),(1088,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Curabitur','8','Velit Limited','nisi sem semper erat, in consectetuer',7,'0.33','92.05',1000),(1089,'Lorem ipsum dolor','50','Ut Incorporated','fringilla. Donec feugiat metus sit amet ante. Vivamus non',61,'0.19','63.06',1000),(1090,'Lorem ipsum','14','Tristique Pellentesque Tellus Incorporated','Fusce diam nunc, ullamcorper eu, euismod ac, fermentum',18,'0.15','68.28',1005),(1091,'Lorem','18','Risus Institute','Morbi metus. Vivamus euismod urna. Nullam lobortis',75,'0.25','64.97',1005),(1092,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit.','42','Ridiculus Mus Aenean Inc.','felis ullamcorper viverra.',54,'0.28','71.52',1000),(1093,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Curabitur','38','Malesuada Ut Incorporated','iaculis enim, sit amet ornare lectus justo eu arcu.',18,'0.06','97.91',1003),(1094,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit.','4','Donec Tempor LLC','lorem semper auctor. Mauris vel turpis. Aliquam',75,'0.29','63.29',1005),(1095,'Lorem ipsum dolor','18','Pellentesque Sed Associates','quam, elementum at, egestas a, scelerisque sed, sapien.',65,'0.16','79.24',1004),(1096,'Lorem ipsum dolor sit amet,','46','Aliquam Gravida Mauris Incorporated','erat neque non quam. Pellentesque habitant morbi tristique senectus',98,'0.45','88.65',1001),(1097,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Curabitur sed','48','Arcu Vivamus Sit Company','felis. Donec tempor,',59,'0.43','64.32',1001),(1098,'Lorem ipsum','36','Imperdiet Nec Inc.','mi',5,'0.01','53.17',1001),(1099,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit.','46','Quisque Industries','tellus id nunc interdum',78,'0.04','62.21',1005),(1100,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Curabitur','30','Cursus PC','montes, nascetur ridiculus mus.',72,'0.08','78.01',1000),(1101,'Lorem','48','Erat Corporation','Phasellus dolor elit, pellentesque a, facilisis non, bibendum sed, est.',38,'0.04','98.10',1000),(1102,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit.','44','Sed PC','velit. Sed malesuada augue ut lacus. Nulla',71,'0.23','89.37',1003),(1103,'Lorem ipsum dolor sit amet, consectetuer adipiscing','46','Dictum Cursus Nunc PC','mauris ipsum porta elit, a feugiat tellus',45,'0.06','59.44',1000),(1104,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Curabitur sed','34','Ut Erat Company','libero at auctor ullamcorper, nisl arcu iaculis enim, sit',29,'0.28','70.79',1001),(1105,'Lorem','42','Tortor Industries','ultricies ligula. Nullam enim. Sed nulla ante,',13,'0.21','60.84',1001),(1106,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Curabitur sed','18','Risus At LLP','in felis. Nulla tempor augue ac',94,'0.35','94.12',1002),(1107,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit.','30','Sit Amet Foundation','ullamcorper. Duis at',97,'0.33','71.15',1005),(1108,'Lorem ipsum dolor sit','38','Et Malesuada Fames Company','Cras eu tellus eu',92,'0.16','56.23',1000),(1109,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Curabitur sed','46','Egestas Rhoncus Proin Inc.','ac risus. Morbi metus. Vivamus euismod',27,'0.37','89.80',1001),(1110,'Lorem ipsum dolor sit amet, consectetuer','6','Facilisis Non Bibendum Incorporated','tellus',64,'0.37','86.29',1000),(1111,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit.','2','Velit Quisque Varius Associates','arcu. Sed eu nibh vulputate mauris sagittis placerat.',85,'0.30','91.37',1001),(1112,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit.','24','Leo Cras Consulting','a tortor. Nunc commodo auctor velit. Aliquam',48,'0.37','72.44',1004),(1113,'Lorem ipsum dolor sit amet,','22','Sed Eu Institute','lobortis risus. In mi pede, nonummy ut, molestie in,',87,'0.05','68.52',1004),(1114,'Lorem ipsum dolor sit','12','Facilisis Suspendisse Commodo Foundation','tincidunt, neque vitae semper egestas, urna justo faucibus',6,'0.19','56.43',1005),(1115,'Lorem ipsum dolor','10','Mauris Aliquam Eu Associates','vulputate, nisi sem',18,'0.26','56.49',1003),(1116,'Lorem ipsum dolor sit amet, consectetuer adipiscing','44','Ornare Facilisis LLP','metus. Aenean sed pede nec ante blandit viverra. Donec',95,'0.10','56.13',1003),(1117,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Curabitur sed','14','Lorem Incorporated','adipiscing.',21,'0.28','70.64',1002),(1118,'Lorem ipsum dolor sit amet, consectetuer','18','Adipiscing Mauris Molestie Incorporated','tempus,',37,'0.04','94.18',1001),(1119,'Lorem ipsum dolor sit amet, consectetuer adipiscing','50','Ullamcorper Nisl LLP','velit.',74,'0.17','65.70',1005),(1120,'Lorem ipsum','10','Non Associates','magnis',65,'0.28','82.98',1002),(1121,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit.','28','Ullamcorper Eu Euismod Company','est ac facilisis facilisis, magna',69,'0.14','85.39',1005),(1122,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Curabitur','10','Lorem Ipsum LLC','at, libero. Morbi',43,'0.45','84.31',1004),(1123,'Lorem ipsum dolor sit amet, consectetuer adipiscing','4','Dictum Eu Eleifend Institute','eget metus. In nec orci. Donec nibh. Quisque',0,'0.17','54.66',1003),(1124,'Lorem ipsum','4','Lectus Nullam Incorporated','sagittis lobortis mauris. Suspendisse aliquet',27,'0.39','81.70',1000),(1125,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit.','44','Sit Amet Corp.','auctor ullamcorper,',70,'0.19','89.53',1005),(1126,'Lorem','26','Non Cursus LLC','lorem fringilla ornare placerat, orci lacus vestibulum lorem, sit amet',35,'0.19','53.47',1002),(1127,'Lorem ipsum','2','Ullamcorper Viverra Maecenas Corporation','consectetuer adipiscing elit. Etiam laoreet, libero et tristique pellentesque,',26,'0.50','85.60',1004),(1128,'Lorem ipsum dolor sit amet, consectetuer adipiscing','34','Velit Eu Sem LLP','lectus pede, ultrices a, auctor non, feugiat nec,',71,'0.18','80.51',1001),(1129,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Curabitur','18','Sed Et Industries','tempor arcu. Vestibulum ut eros non enim',62,'0.47','99.75',1005),(1130,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Curabitur sed','28','A Neque Consulting','amet diam eu dolor egestas rhoncus. Proin nisl',17,'0.07','99.17',1004),(1131,'Lorem ipsum dolor','16','Consectetuer Cursus Corp.','tristique pellentesque, tellus sem mollis',72,'0.44','69.02',1000),(1132,'Lorem ipsum dolor sit amet,','40','Feugiat Placerat Company','Aliquam rutrum lorem ac risus. Morbi metus.',62,'0.05','98.82',1000),(1133,'Lorem ipsum dolor sit amet,','26','Convallis Incorporated','neque sed dictum eleifend, nunc risus varius',44,'0.22','51.45',1003),(1134,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit.','42','Dis Parturient Limited','a',66,'0.07','91.36',1001),(1135,'Lorem ipsum dolor','22','Lorem Tristique Aliquet PC','ac urna. Ut tincidunt vehicula risus. Nulla eget metus',75,'0.29','51.97',1004),(1136,'Lorem ipsum dolor sit amet, consectetuer','44','Parturient Montes Institute','posuere',97,'0.19','78.47',1002),(1137,'Lorem ipsum dolor','24','Diam Limited','vitae semper egestas, urna justo',6,'0.31','83.85',1000),(1138,'Lorem ipsum dolor sit','18','Sagittis Felis LLP','molestie orci tincidunt adipiscing. Mauris molestie',74,'0.46','72.57',1002),(1139,'Lorem ipsum dolor','38','Posuere Industries','Ut sagittis lobortis',86,'0.40','67.61',1002),(1140,'Lorem ipsum dolor sit amet, consectetuer adipiscing','10','Sollicitudin Orci Sem Ltd','enim. Sed nulla ante, iaculis nec,',3,'0.24','85.64',1000),(1141,'Lorem','8','Mollis Consulting','gravida. Praesent eu nulla at sem molestie sodales.',20,'0.09','93.41',1004),(1142,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Curabitur','24','Sagittis Foundation','amet, dapibus id, blandit at, nisi. Cum sociis natoque',33,'0.39','73.55',1001),(1143,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Curabitur','46','Duis Associates','interdum. Curabitur dictum. Phasellus in felis. Nulla tempor augue ac',48,'0.21','97.86',1000),(1144,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Curabitur sed','48','Sed Hendrerit PC','Duis elementum, dui quis accumsan convallis,',90,'0.16','87.81',1001),(1145,'Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Curabitur sed','18','Augue Inc.','mauris ipsum porta elit, a feugiat tellus lorem eu',97,'0.43','68.06',1004),(1146,'Lorem ipsum','16','Urna Nullam Inc.','fringilla mi lacinia',68,'0.10','89.93',1001),(1147,'Lorem ipsum','32','Lorem Corporation','neque. Morbi quis',72,'0.26','71.58',1001),(1148,'Lorem ipsum dolor','38','Curae; Donec Limited','nonummy. Fusce fermentum fermentum arcu. Vestibulum ante ipsum',40,'0.08','62.23',1004),(1149,'Lorem ipsum dolor sit amet, consectetuer','30','Quisque Varius Limited','bibendum sed, est.',35,'0.38','61.50',1002),('1000','Dress Shirt','Small','Apt 9','Black dress shirt, button down',10,25,49,'1003'),('1001','Dress Shirt','Small','Apt 9','Red dress shirt, button down',10,25,49,'1003'),('1002','Dress Shirt','Small','Apt 9','Blue dress shirt, button down',10,25,49,'1003'),('1003','Dress Shirt','Small','Apt 9','White dress shirt, button down',10,25,49,'1003'),('1004','Dress Shirt','Small','Apt 9','Yellow dress shirt, button down',10,25,49,'1003'),('1005','Dress Shirt','Small','Apt 9','Green dress shirt, button down',10,25,49,'1003'),('1030','Dress Shirt','Medium','Apt 9','Black dress shirt, button down',10,25,49,'1003'),('1031','Dress Shirt','Medium','Apt 9','Red dress shirt, button down',10,25,49,'1003'),('1032','Dress Shirt','Medium','Apt 9','Blue dress shirt, button down',10,25,49,'1003'),('1033','Dress Shirt','Medium','Apt 9','White dress shirt, button down',10,25,49,'1003'),('1034','Dress Shirt','Medium','Apt 9','Yellow dress shirt, button down',10,25,49,'1003'),('1035','Dress Shirt','Medium','Apt 9','Green dress shirt, button down',10,25,49,'1003'),('1006','Dress Shirt','Large','Apt 9','Black dress shirt, button down',10,25,49,'1003'),('1007','Dress Shirt','Large','Apt 9','Red dress shirt, button down',10,25,49,'1003'),('1008','Dress Shirt','Large','Apt 9','Blue dress shirt, button down',10,25,49,'1003'),('1009','Dress Shirt','Large','Apt 9','White dress shirt, button down',10,25,49,'1003'),('1010','Dress Shirt','Large','Apt 9','Yellow dress shirt, button down',10,25,49,'1003'),('1011','Dress Shirt','Large','Apt 9','Green dress shirt, button down',10,25,49,'1003'),('1012','Dress Shirt','Small','Apt 9','Black dress shirt, button down',10,25,49,'1003'),('1013','Dress Shirt','Small','Apt 9','Red dress shirt, button down',10,25,49,'1003'),('1014','Dress Shirt','Small','Apt 9','Blue dress shirt, button down',10,25,49,'1003'),('1015','Dress Shirt','Small','Apt 9','White dress shirt, button down',10,25,49,'1003'),('1016','Dress Shirt','Small','Apt 9','Yellow dress shirt, button down',10,25,49,'1003'),('1017','Dress Shirt','Small','Apt 9','Green dress shirt, button down',10,25,49,'1003'),('1018','Dress Shirt','Medium','Apt 9','Black dress shirt, button down',10,25,49,'1003'),('1019','Dress Shirt','Medium','Apt 9','Red dress shirt, button down',10,25,49,'1003'),('1020','Dress Shirt','Medium','Apt 9','Blue dress shirt, button down',10,25,49,'1003'),('1021','Dress Shirt','Medium','Apt 9','White dress shirt, button down',10,25,49,'1003'),('1022','Dress Shirt','Medium','Apt 9','Yellow dress shirt, button down',10,25,49,'1003'),('1023','Dress Shirt','Medium','Apt 9','Green dress shirt, button down',10,25,49,'1003'),('1024','Dress Shirt','Large','Apt 9','Black dress shirt, button down',10,25,49,'1003'),('1025','Dress Shirt','Large','Apt 9','Red dress shirt, button down',10,25,49,'1003'),('1026','Dress Shirt','Large','Apt 9','Blue dress shirt, button down',10,25,49,'1003'),('1027','Dress Shirt','Large','Apt 9','White dress shirt, button down',10,25,49,'1003'),('1028','Dress Shirt','Large','Apt 9','Yellow dress shirt, button down',10,25,49,'1003'),('1029','Dress Shirt','Large','Apt 9','Green dress shirt, button down',10,25,49,'1003')

go

commit work
go

