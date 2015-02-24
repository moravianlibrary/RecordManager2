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
  CONSTRAINT contact_person_library_id_fk FOREIGN KEY (library_id) REFERENCES library(id)
);

CREATE TABLE oai_harvest_conf (
  id                   DECIMAL(10) PRIMARY KEY,
  library_id           DECIMAL(10),
  url                  VARCHAR(128),
  set_spec             VARCHAR(128),
  metadata_prefix      VARCHAR(128),
  granularity          VARCHAR(30),
  contact_person_id    DECIMAL(10),
  CONSTRAINT oai_harvest_conf_library_id_fk        FOREIGN KEY (library_id)        REFERENCES library(id),
  CONSTRAINT oai_harvest_conf_contact_person_id_fk FOREIGN KEY (contact_person_id) REFERENCES contact_person(id)
);

CREATE TABLE format (
  format               VARCHAR(12) PRIMARY KEY,
  description          VARCHAR(255)
);

CREATE TABLE dedup_record (
  id                   DECIMAL(10) PRIMARY KEY,
  isbn                 VARCHAR(32),
  title                VARCHAR(255),
  publication_year     DECIMAL(4),
  physical_format      VARCHAR(255),
  updated              TIMESTAMP
);

CREATE TABLE harvested_record (
  id                   DECIMAL(10) PRIMARY KEY,
  oai_harvest_conf_id  DECIMAL(10),
  oai_record_id        VARCHAR(128),
  harvested            TIMESTAMP,
  updated              TIMESTAMP,
  deleted              TIMESTAMP,
  format               VARCHAR(12) NOT NULL,
  isbn                 VARCHAR(32),
  title                VARCHAR(255),
  publication_year     DECIMAL(4),
  physical_format      VARCHAR(255),
  raw_record           BLOB,
  CONSTRAINT harvested_record_oai_harvest_conf_id_fk FOREIGN KEY (oai_harvest_conf_id) REFERENCES oai_harvest_conf(id),
  CONSTRAINT harvested_record_format_fk              FOREIGN KEY (format)              REFERENCES format(format)
);

CREATE TABLE record_link (
  harvested_record_id  DECIMAL(10),
  dedup_record_id      DECIMAL(10),
  created              TIMESTAMP,
  CONSTRAINT record_link_PK PRIMARY KEY (harvested_record_id, dedup_record_id) 
);
