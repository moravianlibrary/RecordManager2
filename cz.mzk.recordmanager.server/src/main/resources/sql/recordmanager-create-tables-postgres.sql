CREATE TABLE recordmanager_key (
  name                 VARCHAR(128),
  val                  DECIMAL(10),
  PRIMARY KEY (name)
);

CREATE TABLE library (
  id                   DECIMAL(10),
  name                 VARCHAR(128),
  url                  VARCHAR(128),
  catalog_url          VARCHAR(128),
  city                 VARCHAR(60),
  PRIMARY KEY (id)
);

CREATE TABLE contact_person (
  id                   DECIMAL(10),
  library_id           DECIMAL(10),
  name                 VARCHAR(128),
  email                VARCHAR(128),
  phone                VARCHAR(32),
  PRIMARY KEY (id),
  FOREIGN KEY (library_id) REFERENCES library(id)
);

CREATE TABLE oai_harvest_conf (
  id                   DECIMAL(10),
  library_id           DECIMAL(10),
  url                  VARCHAR(128),
  set_spec             VARCHAR(128),
  metadata_prefix      VARCHAR(128),
  contact_person_id    DECIMAL(10),
  PRIMARY KEY (id),
  FOREIGN KEY (library_id)        REFERENCES library(id),
  FOREIGN KEY (contact_person_id) REFERENCES contact_person(id)
);

CREATE TABLE format (
  format               VARCHAR(12) PRIMARY KEY,
  description          VARCHAR(255)
);

CREATE TABLE harvested_record (
  id                   DECIMAL(10),
  oai_harvest_conf_id  DECIMAL(10),
  oai_record_id        VARCHAR(128),
  deleted              TIMESTAMP,
  format               VARCHAR(12) NOT NULL,
  isbn                 VARCHAR(32), -- issn
  issn                 VARCHAR(16),
  title                VARCHAR(255),
  raw_record           BYTEA,
  PRIMARY KEY (id),
  FOREIGN KEY (oai_harvest_conf_id)        REFERENCES oai_harvest_conf(id)
);

CREATE UNIQUE INDEX harvested_record_sec_key ON harvested_record(oai_harvest_conf_id, oai_record_id);
