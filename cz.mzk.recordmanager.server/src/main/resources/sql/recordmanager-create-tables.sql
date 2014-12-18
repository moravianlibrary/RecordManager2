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

CREATE TABLE harvested_record (
  id                   DECIMAL(10),
  oai_harvest_conf_id  DECIMAL(10),
  oai_record_id        VARCHAR(128),
  raw_record           BLOB,
  PRIMARY KEY (id),
  FOREIGN KEY (oai_harvest_conf_id)        REFERENCES oai_harvest_conf(id)
);
