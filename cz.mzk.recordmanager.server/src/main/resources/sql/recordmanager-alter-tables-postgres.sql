-- 19. 2. 2015 - xrosecky
ALTER TABLE dedup_record ADD updated TIMESTAMP;

DROP VIEW dedup_record_last_update;

CREATE VIEW dedup_record_last_update AS
SELECT
  dr.id dedup_record_id,
  MAX(CASE WHEN dr.updated > hr.harvested THEN dr.updated ELSE hr.harvested END) last_update
FROM
  dedup_record dr JOIN 
  record_link rl ON rl.dedup_record_id = dr.id JOIN
  harvested_record hr ON hr.id = rl.harvested_record_id
GROUP BY
  dr.id
;

CREATE VIEW dedup_record_orphaned AS
SELECT
  dr.id dedup_record_id,
  dr.updated AS orphaned
FROM
  dedup_record dr
WHERE
  NOT EXISTS(SELECT 1 FROM record_link rl WHERE rl.dedup_record_id = dr.id)
;

-- 2. 3. 2015 - xrosecky
ALTER TABLE harvested_record ADD publication_year  DECIMAL(4),
                             ADD physical_format   VARCHAR(255)
;

ALTER TABLE dedup_record ADD publication_year  DECIMAL(4),
                         ADD physical_format   VARCHAR(255)
;

-- 26. 3. 2015 - mertam
ALTER TABLE harvested_record ADD unique_id VARCHAR(100) UNIQUE;

-- 3. 4. 2015 - tomascejpek
ALTER TABLE harvested_record DROP unique_id;
ALTER TABLE harvested_record RENAME oai_record_id TO record_id;

-- 7. 4. 2015 - xrosecky
ALTER TABLE oai_harvest_conf ADD COLUMN id_prefix VARCHAR(10);

-- 10. 4. 2015 - xrosecky
ALTER TABLE harvested_record ADD COLUMN dedup_record_id DECIMAL(10);
UPDATE harvested_record hr 
  SET dedup_record_id = (
    SELECT
      dedup_record_id
    FROM
      record_link rl
    WHERE
      rl.harvested_record_id = hr.id
    LIMIT 1
  )
;
DROP VIEW dedup_record_orphaned;
DROP VIEW dedup_record_last_update;
DROP TABLE record_link;
DROP VIEW harvested_record_view;
ALTER TABLE harvested_record DROP COLUMN id;
DROP INDEX harvested_record_sec_key;
ALTER TABLE harvested_record ADD CONSTRAINT harvested_record_pk PRIMARY KEY (oai_harvest_conf_id, oai_record_id);
ALTER TABLE harvested_record RENAME oai_record_id TO record_id;
CREATE VIEW dedup_record_last_update AS
SELECT
  dr.id dedup_record_id,
  MAX(CASE WHEN dr.updated > hr.harvested THEN dr.updated ELSE hr.harvested END) last_update
FROM
  dedup_record dr JOIN 
  harvested_record hr ON hr.dedup_record_id = dr.id 
GROUP BY
  dr.id
;
CREATE VIEW dedup_record_orphaned AS
SELECT
  dr.id dedup_record_id,
  dr.updated AS orphaned
FROM
  dedup_record dr
WHERE
  NOT EXISTS(SELECT 1 FROM harvested_record hr WHERE hr.dedup_record_id = dr.id)
;
CREATE VIEW harvested_record_view AS
SELECT
  oai_harvest_conf_id,
  record_id,
  deleted,
  format,
  isbn,
  title,
  convert_from(raw_record, 'UTF8') AS raw_record
FROM
  harvested_record
;

-- 4. 5. 2015 - xrosecky
ALTER TABLE harvested_record ADD COLUMN id DECIMAL(10);
CREATE SEQUENCE harvested_record_id_seq START 1;
UPDATE harvested_record SET id = nextval('harvested_record_id_seq');
ALTER TABLE harvested_record DROP CONSTRAINT harvested_record_pk;
ALTER TABLE harvested_record ADD CONSTRAINT harvested_record_pk PRIMARY KEY (id);

-- 6. 5. 2015 - mertam
ALTER TABLE harvested_record DROP COLUMN title;
ALTER TABLE harvested_record DROP COLUMN isbn;
ALTER TABLE harvested_record DROP COLUMN physical_format; 

-- 7. 5. 2015 - tomascejpek
ALTER TABLE harvested_record ADD COLUMN weight DECIMAL(10);
ALTER TABLE oai_harvest_conf ADD COLUMN base_weight DECIMAL(10);

-- 12. 5. 2015 - tomascejpek
ALTER TABLE harvested_record ADD COLUMN cluster_id VARCHAR(20);
ALTER TABLE oai_harvest_conf ADD COLUMN cluster_id_enabled BOOLEAN DEFAULT(FALSE);

-- 4. 6. 2015 mertam
ALTER TABLE harvested_record ADD COLUMN pages DECIMAL(10);

-- 12. 6. 2015 mertam 
ALTER TABLE oai_harvest_conf ADD COLUMN id_prefix VARCHAR(10);

-- 24. 6. 2015 - xrosecky
CREATE TABLE import_conf (
  id                   DECIMAL(10) PRIMARY KEY,
  library_id           DECIMAL(10),
  contact_person_id    DECIMAL(10),
  id_prefix            VARCHAR(10),
  base_weight          DECIMAL(10),
  cluster_id_enabled   BOOLEAN DEFAULT FALSE,
  CONSTRAINT import_conf_library_id_fk        FOREIGN KEY (library_id)        REFERENCES library(id),
  CONSTRAINT import_conf_contact_person_id_fk FOREIGN KEY (contact_person_id) REFERENCES contact_person(id)
);

CREATE TABLE kramerius_conf (
  import_conf_id       DECIMAL(10)  PRIMARY KEY,
  url                  VARCHAR(128),
  model                VARCHAR(128),
  query_rows           DECIMAL(10),
  metadata_stream      VARCHAR(128),
  CONSTRAINT kramerius_conf_import_conf_fk FOREIGN KEY (import_conf_id) REFERENCES import_conf(id)
);

INSERT INTO import_conf(id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled)
  SELECT id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled FROM oai_harvest_conf;

ALTER TABLE oai_harvest_conf ADD COLUMN import_conf_id DECIMAL(10);
UPDATE oai_harvest_conf SET import_conf_id = id;
ALTER TABLE oai_harvest_conf DROP CONSTRAINT oai_harvest_conf_pkey;

ALTER TABLE harvested_record DROP CONSTRAINT harvested_record_oai_harvest_conf_id_fkey;
ALTER TABLE harvested_record RENAME oai_harvest_conf_id TO import_conf_id;
ALTER TABLE harvested_record ADD CONSTRAINT harvested_record_import_conf_id_fk FOREIGN KEY (import_conf_id) REFERENCES import_conf(id);

ALTER TABLE authority_record DROP CONSTRAINT authority_record_oai_harvest_conf_id_fkey;
ALTER TABLE authority_record RENAME oai_harvest_conf_id TO import_conf_id;
ALTER TABLE authority_record ADD CONSTRAINT authority_record_import_conf_id_fk FOREIGN KEY (import_conf_id) REFERENCES import_conf(id);

ALTER TABLE oai_harvest_conf DROP COLUMN library_id;
ALTER TABLE oai_harvest_conf DROP COLUMN id;
ALTER TABLE oai_harvest_conf DROP COLUMN contact_person_id;
ALTER TABLE oai_harvest_conf DROP COLUMN base_weight;
ALTER TABLE oai_harvest_conf DROP COLUMN cluster_id_enabled;

-- 7. 7. 2015 - xrosecky
CREATE TABLE download_import_conf (
  import_conf_id       DECIMAL(10)  PRIMARY KEY,
  url                  VARCHAR(128),
  CONSTRAINT download_conf_import_conf_fk FOREIGN KEY (import_conf_id) REFERENCES import_conf(id)
);

CREATE TABLE antikvariaty (
  id                   DECIMAL(10) PRIMARY KEY,
  updated              TIMESTAMP,
  url                  VARCHAR(500),
  title                VARCHAR(255),
  pub_year             DECIMAL(5)
);

CREATE TABLE antikvariaty_catids (
  id_from_catalogue   VARCHAR(100),
  antikvariaty_id     DECIMAL(10),
  CONSTRAINT antikvariaty_catids_pk PRIMARY KEY (id_from_catalogue, antikvariaty_id),
  CONSTRAINT antikvariaty_catids_fk FOREIGN KEY (antikvariaty_id) REFERENCES antikvariaty(id)
);

-- 17. 7. 2015 - xrosecky
CREATE INDEX harvested_record_dedup_record_id_updated_idx ON harvested_record(dedup_record_id, updated);
DROP INDEX harvested_record_dedup_record_idx;

CREATE OR REPLACE VIEW dedup_record_last_update AS
SELECT
  dr.id dedup_record_id,
  MAX(CASE WHEN dr.updated > hr.updated THEN dr.updated ELSE hr.updated END) last_update
FROM
  dedup_record dr JOIN 
  harvested_record hr ON hr.dedup_record_id = dr.id 
GROUP BY
  dr.id
;
