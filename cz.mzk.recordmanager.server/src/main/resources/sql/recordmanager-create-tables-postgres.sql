CREATE TABLE recordmanager_key (
  name                 VARCHAR(128) PRIMARY KEY,
  val                  DECIMAL(10)
);

CREATE TABLE library (
  id                   DECIMAL(10) PRIMARY KEY,
  name                 VARCHAR(128),
  url                  VARCHAR(128),
  catalog_url          VARCHAR(128),
  city                 VARCHAR(60)
);

CREATE TABLE contact_person (
  id                   DECIMAL(10) PRIMARY KEY,
  library_id           DECIMAL(10),
  name                 VARCHAR(128),
  email                VARCHAR(128),
  phone                VARCHAR(32),
  FOREIGN KEY (library_id) REFERENCES library(id)
);

CREATE TABLE oai_harvest_conf (
  id                   DECIMAL(10) PRIMARY KEY,
  library_id           DECIMAL(10),
  url                  VARCHAR(128),
  set_spec             VARCHAR(128),
  metadata_prefix      VARCHAR(128),
  granularity          VARCHAR(30),
  contact_person_id    DECIMAL(10),
  id_prefix            VARCHAR(10),
  FOREIGN KEY (library_id)        REFERENCES library(id),
  FOREIGN KEY (contact_person_id) REFERENCES contact_person(id)
);

CREATE TABLE format (
  format               VARCHAR(12) PRIMARY KEY,
  description          VARCHAR(255)
);

CREATE SEQUENCE dedup_record_seq_id MINVALUE 1;
CREATE TABLE dedup_record (
  id                   DECIMAL(10) DEFAULT NEXTVAL('"dedup_record_seq_id"')  PRIMARY KEY,
  updated              TIMESTAMP
);

CREATE TABLE harvested_record (
  id                   DECIMAL(10) PRIMARY KEY,
  oai_harvest_conf_id  DECIMAL(10),
  record_id            VARCHAR(128),
  harvested            TIMESTAMP,
  updated              TIMESTAMP,
  deleted              TIMESTAMP,
  format               VARCHAR(12) NOT NULL,
  isbn                 VARCHAR(32),
  title                VARCHAR(255),
  publication_year     DECIMAL(4),
  physical_format      VARCHAR(255),
  dedup_record_id      DECIMAL(10),
  raw_record           BYTEA,
  UNIQUE (oai_harvest_conf_id, record_id),
  FOREIGN KEY (oai_harvest_conf_id) REFERENCES oai_harvest_conf(id),
  FOREIGN KEY (format)              REFERENCES format(format)
);

CREATE TABLE isbn (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  isbn                 DECIMAL(13),
  order_in_record      DECIMAL(4),
  note                 VARCHAR(100),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id)
);

CREATE TABLE title (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  title                VARCHAR(255),
  order_in_record      DECIMAL(4),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id)
);

CREATE TABLE authority_record (
  id                   DECIMAL(10) PRIMARY KEY,
  oai_harvest_conf_id  DECIMAL(10),
  oai_record_id        VARCHAR(128),
  authority_type       VARCHAR(128),
  harvested            TIMESTAMP,
  updated              TIMESTAMP,
  deleted              TIMESTAMP,
  format               VARCHAR(12) NOT NULL,
  raw_record           BYTEA,
  FOREIGN KEY (oai_harvest_conf_id) REFERENCES oai_harvest_conf(id),
  FOREIGN KEY (format)              REFERENCES format(format)
);
