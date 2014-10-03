-- liveFlightFeed DDL
--ALTER TABLE `dba`.`liveFlightFeed` DROP CONSTRAINT `PRIMARY`;
--DROP TABLE `dba`.`liveFlightFeed`;

CREATE TABLE `dba`.`liveFlightFeed`
(
  `airline_iata`       VARCHAR(4) NOT NULL,
  `airlineName`        VARCHAR(45),
  `departTime`         VARCHAR(20),
  `arriveTime`         VARCHAR(20),
  `actualArriveTime`   VARCHAR(20),
  `actualDepartTime`   VARCHAR(20),
  `flightDate`         VARCHAR(20) NOT NULL,
  `fromAirport`        VARCHAR(4),
  `toAirport`          VARCHAR(4),
  `flightNo`           VARCHAR(45) NOT NULL,
  `gate`               VARCHAR(45),
  `terminal`           VARCHAR(45),
  status               VARCHAR(45)
)
;

ALTER TABLE `dba`.`liveFlightFeed`
  ADD CONSTRAINT `PRIMARY`
    PRIMARY KEY (`airline_iata`,`flightNo`,`flightDate`)
;

INSERT INTO `dba`.`liveFlightFeed`
(airline_iata,
airlineName,
departTime,
arriveTime,
actualArriveTime,
actualDepartTime,
flightDate,
fromAirport,
toAirport,
flightNo,
gate,
terminal,
status)
VALUES
('AA', 'American Airlines', 'PT11H00M00S', 'PT14H01M00S', 'PT17H01M00S', 'PT14H01M00S', '2014-11-26T00:00.0', 'JFK', 'SFO', '0017', 'C70', 'Terminal 2', 'DELAYED'),
('AA', 'American Airlines', 'PT12H00M00S', 'PT15H01M00S', 'PT12H01M00S', 'PT12H01M00S', '2014-11-26T00:00.0', 'JFK', 'SFO', '0124', 'C66', 'Terminal 2', 'ON TIME');


-- airport_maps DDL
--ALTER TABLE `dba`.`airport_maps` DROP CONSTRAINT `PRIMARY`;
--ALTER TABLE `dba`.`airports` DROP CONSTRAINT `PRIMARY`;
--DROP TABLE `dba`.`airport_maps`;
--DROP TABLE `dba`.`airports`;

CREATE TABLE `dba`.`airport_maps`
(
  `iata`             VARCHAR(4) NOT NULL,
  `image_title`      VARCHAR(45) NOT NULL,
  `image_subtitle`   VARCHAR(45) NOT NULL,
  `image_name`       VARCHAR(45),
  `sequence`         INTEGER NOT NULL
)
;

ALTER TABLE `dba`.`airport_maps`
  ADD CONSTRAINT `PRIMARY`
    PRIMARY KEY (`iata`,`image_title`,`image_subtitle`)
;

INSERT INTO `dba`.`airport_maps`
(iata,
image_title,
image_subtitle,
image_name,
sequence)
VALUES
('JFK',	'John F. Kennedy International Airport',	'Airport Overview',	'jfk_airport_360_wl',	1),
('JFK',	'John F. Kennedy International Airport',	'Terminal 1',	'jfk_terminal_1_540_nl',	2),
('JFK',	'John F. Kennedy International Airport',	'Terminal 2',	'jfk_terminal_2_540_nl',	3),
('JFK',	'John F. Kennedy International Airport',	'Terminal 4',	'jfk_terminal_4_540_nl',	4),
('JFK',	'John F. Kennedy International Airport',	'Terminal 5',	'jfk_terminal_5_540_nl',	5),
('JFK',	'John F. Kennedy International Airport',	'Terminal 6',	'jfk_terminal_7_540_nl',	6),
('JFK',	'John F. Kennedy International Airport',	'Terminal7',	'jfk_terminal_8_540_nl',	7),
('LAS',	'McCarran International Airport (LAS)',	'Airport Overview',	'las_terminal',	1),
('LAS',	'McCarran International Airport (LAS)',	'Terminal 1: Concourse A, Concourse B',	'las_concourse_a_b_c_540_nl',	2),
('LAS',	'McCarran International Airport (LAS)',	'Terminal 1: Concourse D',	'las_concourse_d_540_nl',	3),
('LAS',	'McCarran International Airport (LAS)',	'Terminal 3',	'las_terminal_3_540_nl',	4),
('SFO',	'San Francisco International Airport',	'Airport Overview',	'sfo_airport_540_nl',	1),
('SFO',	'San Francisco International Airport',	'International Terminal', 'sfo_international_terminal_75x75',	2),
('SFO',	'San Francisco International Airport',	'Terminal 1',	'sfo_terminal_1_540_nl',	3),
('SFO',	'San Francisco International Airport',	'Terminal 2',	'sfo_terminal_2_540_nl',	4),
('SFO',	'San Francisco International Airport',	'Terminal 3',	'sfo_terminal_3_540_nl',	5);






