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
  contact_person_id    DECIMAL(10),
  FOREIGN KEY (library_id)        REFERENCES library(id),
  FOREIGN KEY (contact_person_id) REFERENCES contact_person(id)
);

CREATE TABLE format (
  format               VARCHAR(12) PRIMARY KEY,
  description          VARCHAR(255)
);

CREATE TABLE dedup_record (
  id                   DECIMAL(10) PRIMARY KEY,
  isbn                 VARCHAR(32),
  title                VARCHAR(255)
);

CREATE TABLE harvested_record (
  id                   DECIMAL(10) PRIMARY KEY,
  oai_harvest_conf_id  DECIMAL(10),
  oai_record_id        VARCHAR(128),
  deleted              TIMESTAMP,
  format               VARCHAR(12) NOT NULL,
  isbn                 VARCHAR(32),
  title                VARCHAR(255),
  dedup_record_id      DECIMAL(10),
  raw_record           BLOB,
  FOREIGN KEY (oai_harvest_conf_id) REFERENCES oai_harvest_conf(id),
  FOREIGN KEY (format)              REFERENCES format(format),
  FOREIGN KEY (dedup_record_id)     REFERENCES dedup_record(id)
);
