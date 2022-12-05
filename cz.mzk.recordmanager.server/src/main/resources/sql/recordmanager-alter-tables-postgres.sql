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

-- 31. 7. 2015 - mertam
CREATE TABLE oclc (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  oclc                 VARCHAR(20),
  CONSTRAINT oclc_fk  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id)
);

CREATE TABLE language (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  lang                 VARCHAR(5),
  CONSTRAINT language_fk  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id)
);


ALTER TABLE import_conf ADD COLUMN filtering_enabled boolean;

--3. 8. 2015 - mertam; set default filtering values
UPDATE import_conf SET filtering_enabled = false;

CREATE INDEX oclc_harvested_record_idx ON oclc(harvested_record_id); 
CREATE INDEX language_harvested_record_idx ON language(harvested_record_id); 

-- 12. 8. 2015 - mertam; changes in auth_record table
ALTER TABLE authority_record RENAME COLUMN oai_harvest_conf_id TO import_conf_id;
ALTER TABLE authority_record RENAME COLUMN authority_type TO authority_code;
ALTER TABLE authority_record ADD CONSTRAINT authority_code_unique UNIQUE(authority_code);
CREATE INDEX authority_code_idx ON authority_record(authority_code);

-- 14. 8. 2015 mertam; 
ALTER TABLE import_conf ADD COLUMN interception_enabled BOOLEAN DEFAULT FALSE;

-- 20. 8. 2015 tomascejpek
ALTER TABLE import_conf ADD COLUMN is_library BOOLEAN DEFAULT FALSE;

-- 2. 9. 2015 xrosecky
ALTER TABLE language DROP CONSTRAINT language_pkey;
ALTER TABLE language DROP COLUMN id;
DELETE FROM language WHERE harvested_record_id IS NULL;
ALTER TABLE language ADD CONSTRAINT language_pk PRIMARY KEY (harvested_record_id, lang);

-- 6. 9. 2015 mertam 
UPDATE import_conf set id_prefix = 'unmz' where id = 320;
update oai_harvest_conf set set_spec = 'CPK' where import_conf_id = 307;

--14.9. 2015 mjtecka
CREATE TABLE fulltext_monography
(
  id numeric(10,0) NOT NULL,
  harvested_record_id numeric(10,0),
  uuid_page character varying(42),
  is_private boolean, 
  order_in_monography numeric(10,0),
  page character varying(20),
  fulltext bytea,
  CONSTRAINT fulltext_monography_pkey PRIMARY KEY (id),
  CONSTRAINT fulltext_monography_harvested_record_id_fk FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
); 

-- 16. 9. 2015 mertam
CREATE TABLE skat_keys (
  skat_record_id      DECIMAL(10),
  sigla               VARCHAR(20),
  local_record_id     VARCHAR(128),
  manually_merged     BOOLEAN DEFAULT FALSE,
  CONSTRAINT skat_keys_pk PRIMARY KEY(skat_record_id,sigla,local_record_id)
);

CREATE TABLE sigla (
  import_conf_id       DECIMAL(10),
  sigla                VARCHAR(20),
  CONSTRAINT sigla_pk PRIMARY KEY(import_conf_id,sigla)
);

-- 17.9.2015 mertam
alter table sigla add CONSTRAINT sigla_import_conf_fk FOREIGN KEY (import_conf_id) REFERENCES import_conf(id);

--18.9.2015 mertam 
ALTER TABLE harvested_record ADD COLUMN raw_001_id VARCHAR(128); 

-- 21.9.2015 mertam 
ALTER TABLE isbn DROP CONSTRAINT isbn_harvested_record_id_fkey;
ALTER TABLE isbn ADD FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE;
ALTER TABLE issn DROP CONSTRAINT issn_harvested_record_id_fkey;
ALTER TABLE issn ADD FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE;
ALTER TABLE cnb DROP CONSTRAINT cnb_harvested_record_id_fkey;
ALTER TABLE cnb ADD FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE;
ALTER TABLE title DROP CONSTRAINT title_harvested_record_id_fkey;
ALTER TABLE title ADD FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE;
ALTER TABLE oclc DROP CONSTRAINT oclc_fk;
ALTER TABLE oclc ADD FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE;
ALTER TABLE language DROP CONSTRAINT language_fk;
ALTER TABLE language ADD FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE;
ALTER TABLE harvested_record_format_link DROP CONSTRAINT format_link_hr_id_fk;
ALTER TABLE harvested_record_format_link ADD FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE;

-- 21.10.2015 tomascejpek
ALTER TABLE format ALTER COLUMN format TYPE VARCHAR(15);

-- 26.10.2015 tomascejpek
UPDATE import_conf SET id_prefix = 'openlib' WHERE id = 327;
UPDATE import_conf SET interception_enabled = true WHERE id = 327;
UPDATE library SET name = 'OPENLIB' WHERE id = 127;

-- 5. 11. 2015 mjtecka
ALTER TABLE kramerius_conf ADD COLUMN auth_token varchar(128);

-- 5. 11. 2015 mjtecka
ALTER TABLE kramerius_conf ADD COLUMN download_private_fulltexts boolean DEFAULT FALSE;

-- 9. 11. 2015 xrosecky
ALTER TABLE kramerius_conf ADD COLUMN fulltext_harvest_type VARCHAR(128) DEFAULT 'fedora';

-- 13.11.2015 mjtecka
ALTER TABLE fulltext_monography ALTER COLUMN uuid_page TYPE VARCHAR(50);

-- 23.11.2015 tomascejpek
CREATE TABLE cosmotron_996 (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  import_conf_id       DECIMAL(10),
  record_id            VARCHAR(128),
  harvested            TIMESTAMP,
  updated              TIMESTAMP,
  deleted              TIMESTAMP,
  raw_record           BYTEA,
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);

-- 23. 11. 2015 xrosecky
ALTER TABLE import_conf ADD COLUMN harvest_frequency CHAR(1) DEFAULT 'U';

-- 1. 12. 2015 tomascejpek
UPDATE oai_harvest_conf SET url='http://opac.moderniknihovna.cz/cgi-bin/koha/oai.pl' WHERE import_conf_id=306;

-- 3. 12. 2015 tomascejpek
UPDATE oai_harvest_conf SET set_spec='CPK1' WHERE import_conf_id=328;

-- 4.12. 2015 mjtecka
ALTER TABLE kramerius_conf ADD COLUMN url_solr character varying(128);

-- 14. 12. 2015 tomascejpek
UPDATE import_conf SET id_prefix='vktatest' WHERE id=329;

-- 17. 12. 2015 tomascejpek
UPDATE import_conf SET harvest_frequency='D' WHERE id IN (300,301,304,306,307,311,312,313,314,315,316,320,323,324);

-- 17. 12. 2015 mertam
ALTER TABLE harvested_record ADD COLUMN dedup_keys_hash CHAR(40);
ALTER TABLE harvested_record ADD COLUMN next_dedup_flag BOOLEAN DEFAULT TRUE;
ALTER TABLE harvested_record ADD COLUMN oai_timestamp TIMESTAMP;

-- 4. 1. 2016 tomascejpek
UPDATE oai_harvest_conf SET url='http://web2.mlp.cz/cgi/oaie' WHERE import_conf_id=302;
UPDATE oai_harvest_conf SET metadata_prefix='marc21e' WHERE import_conf_id=302;

-- 4. 1. 2016 xrosecky 
CREATE TABLE obalkyknih_toc (
  id                   DECIMAL(10) PRIMARY KEY,
  book_id              DECIMAL(10),
  nbn                  VARCHAR(32),
  oclc                 VARCHAR(32),
  ean                  VARCHAR(32),
  isbn                 VARCHAR(32),
  toc                  VARCHAR(1048576)
);

CREATE INDEX obalkyknih_toc_book_idx ON obalkyknih_toc(book_id);
CREATE INDEX obalkyknih_toc_oclc_idx ON obalkyknih_toc(oclc);
CREATE INDEX obalkyknih_toc_ean_idx ON obalkyknih_toc(ean);
CREATE INDEX obalkyknih_toc_isbn_idx ON obalkyknih_toc(isbn);
CREATE INDEX obalkyknih_toc_nbn_idx ON obalkyknih_toc(nbn);

ALTER TABLE obalkyknih_toc ALTER COLUMN isbn TYPE DECIMAL(13) USING isbn::numeric(13,0);

-- 25. 1. 2016 xrosecky
ALTER TABLE oai_harvest_conf ADD COLUMN extract_id_regex VARCHAR(128);

-- 27. 1. 2016 tomascejpek
UPDATE oai_harvest_conf SET extract_id_regex='[^:]+:(.*)' WHERE import_conf_id in (319,321,325,326);
UPDATE oai_harvest_conf SET extract_id_regex='[^:]+:[^:]+:MZK04-(.*)' WHERE import_conf_id=320;

-- 2. 3. 2016 tomascejpek
UPDATE import_conf SET library_id=130 WHERE id=99003;

-- 3. 3. 2016 tomascejpek
UPDATE oai_harvest_conf SET extract_id_regex='UOG505:(.*)' WHERE import_conf_id=306;

-- 7. 3. 2016 tomascejpek
UPDATE harvested_record SET record_id = substring(record_id FROM 'UOG505:(.*)') where import_conf_id=306 and record_id ~ 'UOG505:';
CREATE TABLE inspiration (
  id					DECIMAL(10) PRIMARY KEY,
  harvested_record_id	DECIMAL(10),
  name					VARCHAR(32),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);
CREATE INDEX inspiration_harvested_record_idx ON inspiration(harvested_record_id);
UPDATE import_conf SET id_prefix = 'mzk' WHERE id=324;

-- 16. 3. 2016 mjtecka
ALTER TABLE fulltext_monography ALTER COLUMN page TYPE VARCHAR(50);

ALTER TABLE fulltext_monography RENAME TO fulltext_kramerius;
ALTER TABLE fulltext_kramerius RENAME COLUMN order_in_monography TO order_in_document;

DROP INDEX fulltext_monography_harvested_record_idx;
CREATE INDEX fulltext_kramerius_harvested_record_idx ON fulltext_kramerius(harvested_record_id);

ALTER TABLE kramerius_conf DROP COLUMN model;

-- 18. 3. 2016 xrosecky
UPDATE import_conf SET harvest_frequency = 'D'
WHERE harvest_frequency = 'U' AND id IN (SELECT import_conf_id FROM oai_harvest_conf WHERE url IS NOT NULL AND url NOT LIKE '%i2.ws.oai.cls' /* Cosmotron */);

-- 22. 3. 2016 tomascejpek
ALTER TABLE sigla ADD COLUMN id DECIMAL(10) UNIQUE;

-- 22. 3. 2016 xrosecky
ALTER TABLE oai_harvest_conf ADD COLUMN harvest_job_name VARCHAR(128);

-- 24. 3. 2016 tomascejpek
UPDATE import_conf SET filtering_enabled=true WHERE id=316;

-- 01. 4. 2016 tomascejpek
UPDATE oai_harvest_conf SET url='http://katalog.svkos.cz/OAI' WHERE import_conf_id=335;
UPDATE oai_harvest_conf SET set_spec='MZK-CPK' WHERE import_conf_id=335;

-- 20. 4. 2016 tomascejpek
UPDATE oai_harvest_conf SET url='http://aleph.knihovna-pardubice.cz/OAI' WHERE import_conf_id=333;
UPDATE oai_harvest_conf SET set_spec='PAG_OAI_CPK_MARC21' WHERE import_conf_id=333;

-- 9. 5. 2016 xrosecky
ALTER TABLE import_conf ADD COLUMN mapping_script VARCHAR(256);

-- 18. 5. 2016 tomascejpek
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (1304, 130, 200, 'sfxknav', 8, false, false, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (1304,NULL,NULL,'marc21',NULL);

-- 19. 5. 2016 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city) VALUES (400, 'AUTHORITY', 'nkp.cz', 'aleph.nkp.cz', NULL);
UPDATE import_conf SET library_id=400 WHERE id=400;
UPDATE import_conf SET is_library=false WHERE id=400;
UPDATE import_conf SET mapping_script='AuthorityMarc.groovy' WHERE id=400;
INSERT INTO harvested_record_format(id, name) VALUES (28, 'PERSON');

-- 24. 5. 2016 tomascejpek
ALTER TABLE title ADD COLUMN similarity_enabled BOOLEAN DEFAULT(FALSE);

-- 10. 6. 2016 tomascejpek
UPDATE import_conf SET library_id=104 WHERE id in (321,325,326);
UPDATE import_conf SET is_library=true WHERE id in (325,326);

-- 14. 6. 2016 tomascejpek
UPDATE import_conf SET filtering_enabled=true WHERE id=400;

-- 22. 6. 2016 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city) VALUES (140, 'MKUO', 'knihovna-uo.cz', 'vufind.knihovna-uo.cz/vufind/', 'Ústí nad Orlicí');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (340, 140, 200, 'mkuo', 8, false, true, false, true, 'D');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (340,'http://katalog-usti.knihovna-uo.cz/cgi-bin/koha/oai.pl','CPK','marc21',NULL,'UOG001:(.*)');

-- 22. 6. 2016 xrosecky
ALTER TABLE import_conf ADD COLUMN generate_dedup_keys BOOLEAN DEFAULT(TRUE);

-- 28. 6. 2016 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city) VALUES (141, 'OSOBNOSTI', 'osobnostiregionu.cz', 'http://hledani.osobnostiregionu.cz/', NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency,mapping_script) VALUES (341, 141, 200, 'osobnosti', 0, false, false, false, false, 'U', 'AuthorityMarc.groovy');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (341,NULL,NULL,'marc21',NULL);

-- 30. 6. 2016 tomascejpek
UPDATE oai_harvest_conf SET metadata_prefix='marccpk' WHERE import_conf_id=340;

-- 12. 7. 2016 tomascejpek
UPDATE import_conf SET harvest_frequency='D' WHERE id=334;
UPDATE oai_harvest_conf SET url='http://109.73.209.153/clavius/l.dll', set_spec='NKP' WHERE import_conf_id=334;
UPDATE import_conf SET harvest_frequency='D' WHERE id=338;
UPDATE oai_harvest_conf SET url='http://katalogold.iir.cz:81/l.dll', set_spec='CPK' WHERE import_conf_id=338;

-- 13. 7. 2016 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city) VALUES (142, 'BMC', 'nlk.cz', 'www.medvik.cz/bmc/', NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (342, 142, 200, 'bmc', 8, false, false, false, false, 'D');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (342,'http://oai.medvik.cz/bmc/oai',NULL,'xml-marc',NULL);

-- 19. 7. 2016 tomascejpek
UPDATE oai_harvest_conf SET set_spec='CPK' WHERE import_conf_id=334;

-- 22. 7. 2016 tomascejpek
UPDATE import_conf SET harvest_frequency='U' WHERE id in (329,323);

-- 16. 8. 2016 tomascejpek
UPDATE import_conf SET harvest_frequency='D' WHERE id=336;
UPDATE oai_harvest_conf SET url='http://svk7.svkkl.cz/i2/i2.ws.oai.cls', set_spec='CPK1', metadata_prefix='oai_marcxml_cpk', harvest_job_name='cosmotronHarvestJob' WHERE import_conf_id=336;

-- 16. 8. 2016 tomascejpek
CREATE TABLE ismn (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  ismn                 DECIMAL(13),
  order_in_record      DECIMAL(4),
  note                 VARCHAR(300),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);
COMMENT ON TABLE ismn IS 'dedup_keys: table contatining ISMNs';
CREATE INDEX ismn_harvested_record_idx ON ismn(harvested_record_id);

-- 18. 8. 2016 tomascejpek
ALTER TABLE download_import_conf ADD COLUMN import_job_name VARCHAR(128);
ALTER TABLE download_import_conf ADD COLUMN format VARCHAR(128);
ALTER TABLE download_import_conf ADD COLUMN extract_id_regex VARCHAR(128);

-- 19. 8. 2016 tomascejpek
INSERT INTO 
  import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency)
SELECT 500, 110, 200, 'antik', null, false, false, false, false, 'U' 
WHERE 
  NOT EXISTS (SELECT id FROM import_conf WHERE id=500)
;
INSERT INTO 
  download_import_conf (import_conf_id,url)
SELECT 500,'http://muj-antikvariat.cz/oai-all.xml'
WHERE 
  NOT EXISTS (SELECT import_conf_id FROM download_import_conf WHERE import_conf_id=500)
;
UPDATE download_import_conf SET import_job_name='antikvariatyImportRecordsJob' WHERE import_conf_id=500;
DELETE FROM oai_harvest_conf WHERE import_conf_id=341;
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format) VALUES (341,'http://www.osobnostiregionu.cz/export_online.php','downloadAndImportRecordsJob','osobnosti');

-- 24. 8. 2016 tomascejpek
UPDATE harvested_record_format SET name='OTHER_PERSON' WHERE id=28;

-- 24. 8. 2016 tomascejpek
ALTER TABLE kramerius_conf ADD COLUMN harvest_job_name VARCHAR(128);
DROP VIEW IF EXISTS oai_harvest_job_stat CASCADE;
CREATE OR REPLACE VIEW oai_harvest_job_stat AS
SELECT
  bje.job_execution_id,
  (array_agg(ic.id))[1]  import_conf_id,
  l.name library_name,
  ohc.url url,
  ohc.set_spec,
  bje.start_time,
  bje.end_time,
  bje.status,
  from_param.date_val from_param,
  to_param.date_val to_param,
  (SELECT sum(read_count) FROM batch_step_execution bse WHERE bse.job_execution_id = bje.job_execution_id) no_of_records
FROM batch_job_instance bji
  JOIN batch_job_execution bje ON bje.job_instance_id = bji.job_instance_id
  JOIN batch_job_execution_params conf_id_param ON conf_id_param.job_execution_id = bje.job_execution_id AND conf_id_param.key_name = 'configurationId'
  LEFT JOIN batch_job_execution_params to_param ON to_param.job_execution_id = bje.job_execution_id AND to_param.key_name = 'to'
  LEFT JOIN batch_job_execution_params from_param ON from_param.job_execution_id = bje.job_execution_id AND from_param.key_name = 'from'
  LEFT JOIN oai_harvest_conf ohc ON ohc.import_conf_id = conf_id_param.long_val
  LEFT JOIN kramerius_conf kc ON kc.import_conf_id = conf_id_param.long_val
  JOIN import_conf ic ON ic.id = ohc.import_conf_id OR ic.id = kc.import_conf_id
  JOIN library l ON l.id = ic.library_id
WHERE bji.job_name IN ('oaiHarvestJob', 'oaiReharvestJob', 'oaiPartitionedHarvestJob', 'cosmotronHarvestJob', 'krameriusHarvestJob', 'krameriusHarvestNoSortingJob')
GROUP BY bje.job_execution_id,l.name,ohc.url,ohc.set_spec,from_param.date_val,to_param.date_val
;
CREATE OR REPLACE VIEW oai_harvest_summary AS
WITH last_harvest_date AS (
  SELECT
    import_conf_id,
    COALESCE(MAX(CASE WHEN status = 'COMPLETED' THEN to_param END), MAX(CASE WHEN status = 'COMPLETED' THEN end_time END)) last_successful_harvest_date,
    COALESCE(MAX(CASE WHEN status = 'FAILED' THEN to_param END), MAX(CASE WHEN status = 'FAILED' THEN end_time END)) last_failed_harvest_date,
    COALESCE(MIN(end_time), MIN(to_param)) first_harvest_date,
    COUNT(1) no_of_harvests
  FROM oai_harvest_job_stat
  GROUP BY import_conf_id
)
SELECT l.name, ic.id_prefix, ohc.url, ohc.set_spec, lhd.last_successful_harvest_date, lhd.last_failed_harvest_date, lhd.first_harvest_date, lhd.no_of_harvests
FROM last_harvest_date lhd
  JOIN import_conf ic ON ic.id = lhd.import_conf_id
  LEFT JOIN oai_harvest_conf ohc ON ohc.import_conf_id = ic.id
  LEFT JOIN kramerius_conf kc ON kc.import_conf_id = ic.id
  JOIN library l ON l.id = ic.library_id
;
CREATE OR REPLACE VIEW oai_last_failed_harvests AS
SELECT name, url, set_spec, last_failed_harvest_date
FROM oai_harvest_summary
WHERE last_failed_harvest_date > last_successful_harvest_date OR (last_failed_harvest_date IS NOT NULL AND last_successful_harvest_date IS NULL)
;
CREATE OR REPLACE VIEW solr_index_summary AS
SELECT params1.string_val solr_url, COALESCE(MAX(params2.date_val), MAX(bje.end_time)) last_index_time
FROM batch_job_instance bji
  JOIN batch_job_execution bje ON bje.job_instance_id = bji.job_instance_id
  JOIN batch_job_execution_params params1 ON params1.job_execution_id = bje.job_execution_id AND params1.key_name = 'solrUrl'
  LEFT JOIN batch_job_execution_params params2 ON params2.job_execution_id = bje.job_execution_id AND params2.key_name = 'to'
WHERE bji.job_name IN ('indexRecordsToSolrJob', 'indexAllRecordsToSolrJob', 'indexHarvestedRecordsToSolrJob')
  AND bje.status = 'COMPLETED'
GROUP BY params1.string_val
;

-- 25. 8. 2016 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city) VALUES (143, 'KFBZ', 'kfbz.cz', 'katalog.kfbz.cz/', 'Zlín');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (343, 143, 200, 'kfbz', 12, false, false, false, true, 'D');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (343,'http://katalog.kfbz.cz/api/oai','0','marc21',NULL);

-- 5. 9. 2016 tomascejpek
ALTER TABLE inspiration ALTER COLUMN name TYPE VARCHAR(128);

-- 6. 9. 2016 tomascejpek
UPDATE import_conf SET id_prefix='nkp' WHERE id in (321,325,326);
UPDATE oai_harvest_conf SET extract_id_regex='s/[^:]+:(.*)/SLK01-$1/' WHERE import_conf_id=321;
UPDATE oai_harvest_conf SET extract_id_regex='s/[^:]+:(.*)/KKL01-$1/' WHERE import_conf_id=325;
UPDATE oai_harvest_conf SET extract_id_regex='s/[^:]+:(.*)/STT01-$1/' WHERE import_conf_id=326;
UPDATE oai_harvest_conf SET url='http://ipac.kvkli.cz/i2/i2.ws.oai.cls' WHERE import_conf_id=308;

-- 6. 10. 2016 tomascejpek
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (345, 102, 200, 'mkpe', 8, false, false, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (345, 'http://web2.mlp.cz/cgi/oai', 'ebook', 'marc21', null, null, null);

-- 7. 10. 2016 tomascejpek
INSERT INTO harvested_record_format(id, name) VALUES (29, 'LEGISLATIVE_GOVERNMENT_ORDERS');
INSERT INTO harvested_record_format(id, name) VALUES (30, 'LEGISLATIVE_REGULATIONS');
INSERT INTO harvested_record_format(id, name) VALUES (31, 'LEGISLATIVE_COMMUNICATION');
INSERT INTO harvested_record_format(id, name) VALUES (32, 'LEGISLATIVE_LAWS');
INSERT INTO harvested_record_format(id, name) VALUES (33, 'LEGISLATIVE_LAWS_TEXT');
INSERT INTO harvested_record_format(id, name) VALUES (34, 'LEGISLATIVE_FINDING');
INSERT INTO harvested_record_format(id, name) VALUES (35, 'LEGISLATIVE_CONSTITUTIONAL_LAWS');
INSERT INTO harvested_record_format(id, name) VALUES (36, 'LEGISLATIVE_DECISIONS');

-- 10. 10. 2016 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city) VALUES (144, 'ZAKONY', 'zakonyprolidi.cz', '', NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (344, 144, 200, 'zakony', 8, false, true, false, false, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (344,NULL,NULL,'marc21',NULL);

-- 12. 10. 2016 tomascejpek
INSERT INTO harvested_record_format(id, name) VALUES (37, 'LEGISLATIVE_DECREES');
INSERT INTO harvested_record_format(id, name) VALUES (38, 'LEGISLATIVE_EDICTS');
INSERT INTO harvested_record_format(id, name) VALUES (39, 'LEGISLATIVE_RESOLUTIONS');
INSERT INTO harvested_record_format(id, name) VALUES (40, 'LEGISLATIVE_MEASURES');
INSERT INTO harvested_record_format(id, name) VALUES (41, 'LEGISLATIVE_DIRECTIVES');
INSERT INTO harvested_record_format(id, name) VALUES (42, 'LEGISLATIVE_TREATIES');
INSERT INTO harvested_record_format(id, name) VALUES (43, 'LEGISLATIVE_EDITORIAL');
INSERT INTO harvested_record_format(id, name) VALUES (44, 'LEGISLATIVE_RULES');
INSERT INTO harvested_record_format(id, name) VALUES (45, 'LEGISLATIVE_ORDERS');
INSERT INTO harvested_record_format(id, name) VALUES (46, 'LEGISLATIVE_PROCEDURES');
INSERT INTO harvested_record_format(id, name) VALUES (47, 'LEGISLATIVE_STATUTES');
INSERT INTO harvested_record_format(id, name) VALUES (48, 'LEGISLATIVE_CONVENTIONS');
INSERT INTO harvested_record_format(id, name) VALUES (49, 'LEGISLATIVE_PRINCIPLES');
INSERT INTO harvested_record_format(id, name) VALUES (50, 'LEGISLATIVE_AGREEMENTS');
INSERT INTO harvested_record_format(id, name) VALUES (51, 'LEGISLATIVE_GUIDELINES');
INSERT INTO harvested_record_format(id, name) VALUES (52, 'LEGISLATIVE_ORDINANCES');
INSERT INTO harvested_record_format(id, name) VALUES (53, 'LEGISLATIVE_SENATE_MEASURES');
INSERT INTO harvested_record_format(id, name) VALUES (54, 'LEGISLATIVE_CONDITIONS');
INSERT INTO harvested_record_format(id, name) VALUES (55, 'LEGISLATIVE_OTHERS');
INSERT INTO harvested_record_format(id, name) VALUES (56, 'LEGISLATIVE_NOTICE');
INSERT INTO harvested_record_format(id, name) VALUES (57, 'LEGISLATIVE_CODE');

-- 14. 11. 2016 tomascejpek
DELETE FROM sigla WHERE id IN (4,26,27,31);
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (33, 340, 'UOG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (34, 340, 'UOG009');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (35, 340, 'UOG010');

-- 06. 12. 2016 tomascejpek
CREATE OR REPLACE VIEW dedup_record_orphaned AS
SELECT
  dr.id dedup_record_id,
  dr.updated AS orphaned
FROM
  dedup_record dr
WHERE
  NOT EXISTS(SELECT 1 FROM harvested_record hr WHERE hr.dedup_record_id = dr.id and deleted is null)
;

-- 06. 12. 2016 tomascejpek
UPDATE import_conf SET interception_enabled='true' WHERE id=312;

-- 09. 12. 2016 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city) VALUES (146, 'MKPR', 'http://knihovnaprerov.cz/', 'katalog.knihovnaprerov.cz/', 'Přerov');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (346, 146, 200, 'mkpr', 12, false, false, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (346,'http://katalog.knihovnaprerov.cz/l.dll','CPK','marc21',NULL);

-- 09. 12. 2016 tomascejpek
UPDATE import_conf SET base_weight=9 WHERE id=346;

-- 12. 12. 2016 tomascejpek
DELETE FROM oai_harvest_conf WHERE import_conf_id in (319,321,325,326);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (319,'local:/data/imports/aleph.ANL','importOaiRecordsJob',null,'[^:]+:(.*)');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (321,'local:/data/imports/aleph.SLK','importOaiRecordsJob',null,'s/[^:]+:(.*)/SLK01-$1/');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (325,'local:/data/imports/aleph.KKL','importOaiRecordsJob',null,'s/[^:]+:(.*)/KKL01-$1/');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (326,'local:/data/imports/aleph.STT','importOaiRecordsJob',null,'s/[^:]+:(.*)/STT01-$1/');

-- 05. 01. 2017 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city) VALUES (147, 'UPV', 'http://upv.cz/', 'https://isdv.upv.cz/webapp/pts.frm', NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (347, 147, 200, 'upv', 8, false, false, false, false, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (347,NULL,NULL,'marc21',NULL);

-- 05. 01. 2017 tomascejpek
INSERT INTO harvested_record_format(id, name) VALUES (58, 'PATENTS');

-- 10. 01. 2017 tomascejpek
ALTER TABLE harvested_record ADD COLUMN source_info VARCHAR(255);
CREATE INDEX harvested_record_source_info_idx ON harvested_record(source_info);

-- 10. 01. 2017 tomascejpek
CREATE TABLE ean (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  ean                  DECIMAL(13),
  order_in_record      DECIMAL(4),
  note                 VARCHAR(300),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);
COMMENT ON TABLE ean IS 'dedup_keys: table contatining EANs';
CREATE INDEX ean_harvested_record_idx ON ean(harvested_record_id);

-- 11. 01. 2017 tomascejpek
CREATE TABLE short_title (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  short_title          VARCHAR(255),
  order_in_record      DECIMAL(4),
  similarity_enabled   BOOLEAN DEFAULT FALSE,
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);
CREATE INDEX short_title_harvested_record_idx ON short_title(harvested_record_id);

-- 16. 01. 2017 tomascejpek
INSERT INTO harvested_record_format(id, name) VALUES (59, 'COMPUTER_CARRIERS');

-- 16. 01. 2017 tomascejpek
INSERT INTO harvested_record_format(id, name) VALUES (60, 'OTHER_OTHER');

-- 16. 01. 2017 tomascejpek
UPDATE harvested_record_format SET name='OTHER_COMPUTER_CARRIER' WHERE id=59;

-- 25. 01. 2017 tomascejpek
UPDATE import_conf SET base_weight=13 WHERE id in (308,314,328,335,336);
UPDATE import_conf SET base_weight=10 WHERE id=313;

-- 27. 01. 2017 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city) VALUES (148, 'NPMK', 'http://npmk.cz/', 'http://katalog.npmk.cz/', NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (348, 148, 200, 'npmk', 11, false, false, false, false, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (348,'http://katalog.npmk.cz/api/oai','4','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (149, 'EUHB', 'https://www.lib.cas.cz/', '', NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (349, 149, 200, 'euhb', 9, false, false, false, false, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (349,'http://aleph.lib.cas.cz/OAI','EUHB','marc21',NULL);

-- 27. 01. 2017 tomascejpek
INSERT INTO harvested_record_format(id, name) VALUES (61, 'PATENTS_UTILITY_MODELS');
INSERT INTO harvested_record_format(id, name) VALUES (62, 'PATENTS_PATENT_APPLICATIONS');
INSERT INTO harvested_record_format(id, name) VALUES (63, 'PATENTS_PATENTS');

-- 27. 01. 2017 tomascejpek
UPDATE import_conf SET generate_dedup_keys=false WHERE id=347;

-- 24. 02. 2017 tomascejpek
UPDATE import_conf SET id_prefix='muzibib' WHERE id=349;
UPDATE library SET name='MUZIBIB' WHERE id=149;
UPDATE import_conf SET generate_dedup_keys=false WHERE id=331;

-- 23. 03. 2017 tomascejpek
UPDATE oai_harvest_conf SET set_spec='cpk' WHERE import_conf_id=302;

-- 24. 03. 2017 tomascejpek
UPDATE import_conf SET filtering_enabled=true WHERE id=314;

-- 10. 05. 2017 tomascejpek
CREATE TABLE tezaurus_record (
  id                   DECIMAL(10) PRIMARY KEY,
  import_conf_id       DECIMAL(10),
  record_id            VARCHAR(128),
  source_field         VARCHAR(15),
  name                 VARCHAR(255),
  raw_record           BYTEA,
  FOREIGN KEY (import_conf_id) REFERENCES import_conf(id)
);
CREATE INDEX tezaurus_id_idx ON tezaurus_record(import_conf_id,record_id);
CREATE INDEX tezaurus_name_idx ON tezaurus_record(import_conf_id,source_field,name);
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys) VALUES (352,101,200,'mesh',0,false,false,false,false,'U',null,false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (352,NULL,NULL,'marc21',NULL);

-- 13. 06. 2017 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city) VALUES (150, 'MKCHODOV', 'https://www.knihovnachodov.cz/', 'https://www.knihovnachodov.cz/Katalog/', 'Chodov');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (350, 150, 200, 'mkchodov', 11, false, false, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (350,'https://www.knihovnachodov.cz/Tritius/oai-provider','CPK_124','marc21',NULL);

-- 21. 06. 2017 tomascejpek
UPDATE import_conf SET filtering_enabled=true WHERE id=327;

-- 17. 07. 2017 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city) VALUES (153, 'OKPB', 'http://www.okpb.cz', 'http://www.okpb.cz/clavius/', 'Opava');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (353, 153, 200, 'okpb', 12, false, false, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (353,'http://www.okpb.cz/clavius/l.dll','CPK','marc21',NULL);

-- 19. 07. 2017 tomascejpek
UPDATE library SET city='Bibliography' WHERE id in (119,131,142,148,149);
UPDATE library SET city=NULL WHERE id=116;

-- 28. 07. 2017 tomascejpek
ALTER TABLE import_conf ADD COLUMN mapping_dedup_script VARCHAR(256);
UPDATE import_conf SET mapping_dedup_script='AuthorityMergedBaseMarc.groovy' WHERE id in (341,400);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (151, 'LIBRARY', 'nkp.cz', '', NULL);
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,generate_dedup_keys,mapping_script,mapping_dedup_script) VALUES (351, 151, 200, 'library', 0, false, true, false, false, 'U', false, 'AdresarKnihovenBaseMarc.groovy', 'AdresarKnihovenBaseMarc.groovy');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (351,'local:/data/imports/aleph.ADR','importOaiRecordsJob',null,'[^:]+:(.*)');
INSERT INTO sigla (id,import_conf_id,sigla) VALUES (36,304,'ABA000');

-- 01. 08. 2017 tomascejpek
DELETE FROM download_import_conf WHERE import_conf_id in (321,325,326);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (321,'http://aleph.nkp.cz/OAI','SLK-CPK','marc21',NULL);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (325,'http://aleph.nkp.cz/OAI','KKL-CPK','marc21',NULL);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (326,'http://aleph.nkp.cz/OAI','STT-CPK','marc21',NULL);

-- 02. 08. 2017 tomascejpek
CREATE TABLE publisher_number (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  publisher_number     VARCHAR(255),
  order_in_record      DECIMAL(4),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);
COMMENT ON TABLE publisher_number IS 'dedup_keys: table contatining publisher numbers';
CREATE INDEX publisher_number_harvested_record_idx ON publisher_number(harvested_record_id);

-- 11. 08. 2017 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (37, 343, 'ZLG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (38, 346, 'PRG001');

-- 07. 08. tomascejpek
DROP VIEW IF EXISTS oai_harvest_summary CASCADE;
ALTER TABLE import_conf ALTER COLUMN id_prefix TYPE VARCHAR(15);
CREATE OR REPLACE VIEW oai_harvest_summary AS
WITH last_harvest_date AS (
  SELECT
    import_conf_id,
    COALESCE(MAX(CASE WHEN status = 'COMPLETED' THEN to_param END), MAX(CASE WHEN status = 'COMPLETED' THEN end_time END)) last_successful_harvest_date,
    COALESCE(MAX(CASE WHEN status = 'FAILED' THEN to_param END), MAX(CASE WHEN status = 'FAILED' THEN end_time END)) last_failed_harvest_date,
    COALESCE(MIN(end_time), MIN(to_param)) first_harvest_date,
    COUNT(1) no_of_harvests
  FROM oai_harvest_job_stat
  GROUP BY import_conf_id
)
SELECT ic.id, l.name, ic.id_prefix, ohc.url, ohc.set_spec, lhd.last_successful_harvest_date, lhd.last_failed_harvest_date, lhd.first_harvest_date, lhd.no_of_harvests
FROM last_harvest_date lhd
  JOIN import_conf ic ON ic.id = lhd.import_conf_id
  LEFT JOIN oai_harvest_conf ohc ON ohc.import_conf_id = ic.id
  LEFT JOIN kramerius_conf kc ON kc.import_conf_id = ic.id
  JOIN library l ON l.id = ic.library_id
;
CREATE OR REPLACE VIEW oai_last_failed_harvests AS
SELECT name, url, set_spec, last_failed_harvest_date
FROM oai_harvest_summary
WHERE last_failed_harvest_date > last_successful_harvest_date OR (last_failed_harvest_date IS NOT NULL AND last_successful_harvest_date IS NULL);

-- 15. 08. 2017 tomascejpek
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1305,104,200,'sfxjibnkp',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1306,115,200,'sfxjibvkol',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1307,128,200,'sfxjibcbvk',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1308,143,200,'sfxjibkfbz',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1309,108,200,'sfxjibkvkl',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1310,102,200,'sfxjibmkp',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1311,135,200,'sfxjibsvkos',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1312,113,200,'sfxjibsvkhk',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1313,130,200,'sfxjibknav',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1314,100,200,'sfxjibmzk',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1305,'http://sfx.jib.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-NKP.xml','downloadAndImportRecordsJob','sfx',null);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1306,'http://sfx.jib.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-VKOL.xml','downloadAndImportRecordsJob','sfx',null);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1307,'http://sfx.jib.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-JVKCB.xml','downloadAndImportRecordsJob','sfx',null);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1308,'http://sfx.jib.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-KKFB.xml','downloadAndImportRecordsJob','sfx',null);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1309,'http://sfx.jib.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-KVKLI.xml','downloadAndImportRecordsJob','sfx',null);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1310,'http://sfx.jib.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-MKP.xml','downloadAndImportRecordsJob','sfx',null);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1311,'http://sfx.jib.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-MSVK.xml','downloadAndImportRecordsJob','sfx',null);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1312,'http://sfx.jib.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-SVKHK.xml','downloadAndImportRecordsJob','sfx',null);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1313,null,'downloadAndImportRecordsJob','sfx',null);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1314,'http://sfx.jib.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-MZK.xml','downloadAndImportRecordsJob','sfx',null);

-- 30. 08. 2017 tomascejpek
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1315,100,200,'sfxjibfree',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1316,101,200,'sfxjibnlk',8,false,true,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1318,107,200,'sfxjibntk',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1319,107,200,'sfxjibuochb',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1320,107,200,'sfxjibvscht',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1315,'http://sfx.jib.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-ANY.xml','downloadAndImportRecordsJob','sfx',null);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1315,'http://sfx.jib.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding.xml','downloadAndImportRecordsJob','sfx',null);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1316,null,null,'sfxnlk',null);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1318,null,null,'sfx',null);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1319,null,null,'sfx',null);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1320,null,null,'sfx',null);

-- 08. 09. 2017 tomascejpek
INSERT INTO harvested_record_format(id, name) VALUES (64, 'OTHER_DICTIONARY_ENTRY');
INSERT INTO library (id,name,url,catalog_url,city) VALUES (154,'TDKIV','nkp.cz','aleph.nkp.cz',null);
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (354,154,200,'tdkiv',8,false,false,true,false,'U','DictionaryLocal.groovy',false,'DictionaryMerged.groovy');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (354,'local:/data/imports/aleph.KTD','importOaiRecordsJob',null,'[^:]+:(.*)');

-- 13. 09. 2017 tomascejpek
DELETE FROM sigla WHERE id=21;
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (40, 350, 'SOG504');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (41, 353, 'OPG001');

-- 22. 09. 2017 tomascejpek
UPDATE oai_harvest_conf SET url='https://katalog.kfbz.cz/api/oai' WHERE import_conf_id=343;

-- 12. 10. 2017 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city) VALUES (157, 'AGROVOC', 'http://aims.fao.org', 'http://aims.fao.org', NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (357, 157, 200, 'agrovoc', 0, false, false, false, false, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (357,NULL,NULL,'marc21',NULL);

-- 24. 10. 2017 tomascejpek
UPDATE import_conf SET filtering_enabled=true WHERE id in ('300','301','302','304','307','308','311','312','313','315','319','320','321','324','325','326','328','330','331','332','333','334','335','336','337','338','342','343','345','346','347','348','349','350','353');

-- 31. 10. 2017 tomascejpek
CREATE OR REPLACE VIEW dedup_record_orphaned AS
SELECT
  dr.id dedup_record_id,
  GREATEST(dr.updated, (SELECT MAX(updated) FROM harvested_record hr WHERE hr.dedup_record_id = dr.id)) orphaned
FROM
  dedup_record dr
WHERE
  NOT EXISTS(SELECT 1 FROM harvested_record hr WHERE hr.dedup_record_id = dr.id and deleted is null)
;

-- 06. 11. 2017 tomascejpek
ALTER TABLE import_conf ADD COLUMN item_id VARCHAR(15);
UPDATE import_conf SET item_id='aleph',interception_enabled=true WHERE id in (300,304,307,313,315,321,324,325,326,330,335,337);
UPDATE import_conf SET item_id='tre',interception_enabled=true WHERE id=306;
UPDATE import_conf SET item_id='nlk',interception_enabled=true WHERE id=301;
UPDATE import_conf SET item_id='svkul',interception_enabled=true WHERE id=314;
UPDATE import_conf SET item_id='other',interception_enabled=true WHERE id in (302,308,311,312,328,334,336,338,340,343,346,350,353);

-- 06. 11. 2017 tomascejpek
UPDATE oai_harvest_conf SET url='https://www.knihovnachodov.cz/tritius/oai-provider' WHERE import_conf_id=350;

-- 10. 11. 2017 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city) VALUES (156, 'KJDPB', 'https://www.kjd.pb.cz/', 'http://gw.kjd.pb.cz:8080/', 'Příbram');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (356, 156, 200, 'kjdpb', 12, false, true, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (356,'http://gw.kjd.pb.cz:8080/l.dll','CPK','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (158, 'KNIHBIB', 'https://www.lib.cas.cz/', 'https://www.lib.cas.cz/', 'Bibliography');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (358, 158, 200, 'knihbib', 11, false, true, false, false, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (358,'http://aleph.lib.cas.cz/OAI','KVO','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (159, 'UPOL', 'https://www.knihovna.upol.cz/', 'https://library.upol.cz/i2/i2.entry.cls', 'Olomouc');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (359, 159, 200, 'upol', 11, false, true, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,harvest_job_name) VALUES (359,'http://library.upol.cz/i2/i2.ws.oai.cls','UPOLCPKALL','oai_marcxml_cpk',NULL,'cosmotronHarvestJob');

-- 10. 11. 2017 tomascejpek
ALTER TABLE harvested_record ADD COLUMN upv_application_id VARCHAR(20);
CREATE INDEX harvested_record_upv_appl_dx ON harvested_record(upv_application_id);

-- 13. 11. 2017 tomascejpek
ALTER TABLE harvested_record
ADD COLUMN source_info_t VARCHAR(255),
ADD COLUMN source_info_x VARCHAR(30),
ADD COLUMN source_info_g VARCHAR(255);
CREATE INDEX harvested_record_source_info_t_idx ON harvested_record(source_info_t);
CREATE INDEX harvested_record_source_info_x_idx ON harvested_record(source_info_x);
CREATE INDEX harvested_record_source_info_g_idx ON harvested_record(source_info_g);
DROP INDEX IF EXISTS harvested_record_source_info_idx;
ALTER TABLE harvested_record DROP COLUMN source_info;

-- 14. 11. 2017 tomascejpek
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1321,138,200,'sfxjibirel',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1321,'http://sfx.jib.cz/sfxirel/cgi/public/get_file.cgi?file=institutional_holding-IREL.xml','downloadAndImportRecordsJob','sfx',null);

-- 04. 12. 2017 tomascejpek
UPDATE oai_harvest_conf SET url='http://kutnahora.tritius.cz/tritius/oai-provider' WHERE import_conf_id=334;

-- 06. 12. 2017 tomascejpek
UPDATE oai_harvest_conf SET url='https://kutnahora.tritius.cz/tritius/oai-provider' WHERE import_conf_id=334;
UPDATE oai_harvest_conf SET url='http://katalog.kkvysociny.cz/clavius/l.dll' WHERE import_conf_id=312;

-- 06. 12. 2017 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city) VALUES (160, 'CELITEBIB', 'https://www.lib.cas.cz/', 'https://www.lib.cas.cz/', 'Bibliography');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (360, 160, 200, 'celitebib', 11, false, true, false, false, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (360,'https://aleph.lib.cas.cz/OAI','UCLA','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (161, 'CVGZ', 'https://www.cvgz.cas.cz/', 'https://www.lib.cas.cz/', 'Brno');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (361, 161, 200, 'cvgz', 11, false, true, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (361,'https://aleph.lib.cas.cz/OAI','CVGZ','marc21',NULL);

-- 20. 12. 2017 tomascejpek
UPDATE oai_harvest_conf SET extract_id_regex='s/^(.*)/KKV01-$1/' WHERE import_conf_id=332;

-- 26. 01. 2017 tomascejpek
DELETE FROM download_import_conf WHERE import_conf_id=319;
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (319,'http://aleph.nkp.cz/OAI','ANL','marc21',NULL);

-- 29. 01. 2018 tomascejpek
ALTER TABLE cosmotron_996 DROP CONSTRAINT cosmotron_996_harvested_record_id_fkey;
ALTER TABLE cosmotron_996 ADD COLUMN parent_record_id VARCHAR(128);
ALTER TABLE cosmotron_996 ADD CONSTRAINT cosmotron_996_uniqueid UNIQUE (record_id,import_conf_id);
DROP INDEX IF EXISTS cosmotron_996_harvested_record_idx;
ALTER TABLE cosmotron_996 DROP COLUMN harvested_record_id;
UPDATE oai_harvest_conf set extract_id_regex='s/[^:]+:[^:]+:([^\\/]+)\\/([^\\/]+)/$1_$2/' WHERE import_conf_id in (308,328,336);
CREATE INDEX cosmotron_996_conf_id_parent_id_idx ON cosmotron_996(import_conf_id,parent_record_id);
CREATE OR REPLACE VIEW cosmotron_periodicals_last_update AS
  SELECT
    hr.id harvested_record_id,
    hr.import_conf_id,
    hr.record_id,
    GREATEST(hr.updated, (SELECT MAX(updated) FROM cosmotron_996 c996 WHERE c996.import_conf_id = hr.import_conf_id AND c996.parent_record_id = hr.record_id)) last_update
  FROM
    harvested_record hr
  WHERE
    EXISTS(SELECT 1 FROM cosmotron_996 c996 WHERE c996.import_conf_id = hr.import_conf_id AND c996.parent_record_id = hr.record_id)
;

-- 30. 01. 2018 tomascejpek
UPDATE oai_harvest_conf SET url='https://katalog1.kjd.pb.cz/l.dll' WHERE import_conf_id=356;
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (42, 356, 'PBG001');
UPDATE import_conf SET item_id='other',interception_enabled=true WHERE id=356;

-- 31. 01. 2018 tomascejpek
DROP INDEX cosmotron_996_conf_id_parent_id_idx;
CREATE INDEX cosmotron_996_conf_id_parent_id_idx ON cosmotron_996 (parent_record_id,import_conf_id);

-- 27. 02. 2018 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city) VALUES (163, 'PKJAK', 'http://npmk.cz/', 'http://katalog.npmk.cz/', 'Praha');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (363, 163, 200, 'pkjak', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (363,'https://katalog.npmk.cz/api/oai','5','marc21',NULL,'oai:(.*)');

-- 05. 03. 2018 tomascejpek
INSERT INTO harvested_record_format(id, name) VALUES (65, 'BLIND_AUDIO');
INSERT INTO harvested_record_format(id, name) VALUES (66, 'BLIND_BRAILLE');

-- 12. 03. 2018 tomascejpek
UPDATE import_conf SET interception_enabled=true WHERE id=360;

-- 13. 03. 2018 tomascejpek
UPDATE import_conf SET mapping_script='AdresarKnihovenLocal.groovy' WHERE id=351;

-- 10. 04. 2018 tomascejpek
DROP VIEW IF EXISTS oai_harvest_job_stat CASCADE;
CREATE OR REPLACE VIEW oai_harvest_job_stat AS
  SELECT
    bje.job_execution_id,
    (array_agg(ic.id))[1]  import_conf_id,
    l.name library_name,
    ohc.url url,
    ohc.set_spec,
    bje.start_time,
    bje.end_time,
    bje.status,
    from_param.date_val from_param,
    to_param.date_val to_param,
    (SELECT sum(read_count) FROM batch_step_execution bse WHERE bse.job_execution_id = bje.job_execution_id) no_of_records
  FROM batch_job_instance bji
    JOIN batch_job_execution bje ON bje.job_instance_id = bji.job_instance_id
    JOIN batch_job_execution_params conf_id_param ON conf_id_param.job_execution_id = bje.job_execution_id AND conf_id_param.key_name = 'configurationId'
    LEFT JOIN batch_job_execution_params to_param ON to_param.job_execution_id = bje.job_execution_id AND to_param.key_name = 'to'
    LEFT JOIN batch_job_execution_params from_param ON from_param.job_execution_id = bje.job_execution_id AND from_param.key_name = 'from'
    LEFT JOIN oai_harvest_conf ohc ON ohc.import_conf_id = conf_id_param.long_val
    LEFT JOIN kramerius_conf kc ON kc.import_conf_id = conf_id_param.long_val
    JOIN import_conf ic ON ic.id = ohc.import_conf_id OR ic.id = kc.import_conf_id
    JOIN library l ON l.id = ic.library_id
  WHERE bji.job_name IN ('oaiHarvestJob', 'oaiReharvestJob', 'oaiPartitionedHarvestJob', 'cosmotronHarvestJob', 'krameriusHarvestJob', 'krameriusHarvestNoSortingJob', 'oaiHarvestOneByOneJob')
  GROUP BY bje.job_execution_id,l.name,ohc.url,ohc.set_spec,from_param.date_val,to_param.date_val
;
CREATE OR REPLACE VIEW oai_harvest_summary AS
  WITH last_harvest_date AS (
      SELECT
        import_conf_id,
        COALESCE(MAX(CASE WHEN status = 'COMPLETED' THEN to_param END), MAX(CASE WHEN status = 'COMPLETED' THEN end_time END)) last_successful_harvest_date,
        COALESCE(MAX(CASE WHEN status = 'FAILED' THEN to_param END), MAX(CASE WHEN status = 'FAILED' THEN end_time END)) last_failed_harvest_date,
        COALESCE(MIN(end_time), MIN(to_param)) first_harvest_date,
        COUNT(1) no_of_harvests
      FROM oai_harvest_job_stat
      GROUP BY import_conf_id
  )
  SELECT ic.id, l.name, ic.id_prefix, ohc.url, ohc.set_spec, lhd.last_successful_harvest_date, lhd.last_failed_harvest_date, lhd.first_harvest_date, lhd.no_of_harvests
  FROM last_harvest_date lhd
    JOIN import_conf ic ON ic.id = lhd.import_conf_id
    LEFT JOIN oai_harvest_conf ohc ON ohc.import_conf_id = ic.id
    LEFT JOIN kramerius_conf kc ON kc.import_conf_id = ic.id
    JOIN library l ON l.id = ic.library_id
;
CREATE OR REPLACE VIEW oai_last_failed_harvests AS
  SELECT name, url, set_spec, last_failed_harvest_date
  FROM oai_harvest_summary
  WHERE last_failed_harvest_date > last_successful_harvest_date OR (last_failed_harvest_date IS NOT NULL AND last_successful_harvest_date IS NULL)
;
UPDATE oai_harvest_conf SET harvest_job_name='oaiHarvestOneByOneJob' WHERE import_conf_id in (301,342);

-- 10. 04. 2018 tomascejpek
UPDATE import_conf SET base_weight=11 WHERE id=349;
INSERT INTO library (id, name, url, catalog_url, city) VALUES (165, 'DIVABIB', 'http://www.idu.cz/cs/bibliograficke-oddeleni', 'http://vis.idu.cz/Biblio.aspx', 'Bibliography');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (365, 165, 200, 'divabib', 11, false, true, false, false, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (365,'http://vis.idu.cz:8080/biblio/api/oai','4','marc21',NULL);

-- 12. 04. 2018 tomascejpek
UPDATE import_conf SET interception_enabled=true WHERE id=342;

-- 19. 04. 2018 tomas.cejpek
UPDATE oai_harvest_conf SET url='https://ipac.svkkl.cz/i2/i2.ws.oai.cls' WHERE import_conf_id=336;

-- 24. 04. 2018 tomascejpek
UPDATE oai_harvest_conf SET url='https://aleph.mzk.cz/OAI' WHERE import_conf_id=300;
UPDATE oai_harvest_conf SET url='https://web2.mlp.cz/cgi/oaie' WHERE import_conf_id=302;
UPDATE oai_harvest_conf SET url='https://aleph.nkp.cz/OAI' WHERE import_conf_id=304;
UPDATE oai_harvest_conf SET url='https://opac.moderniknihovna.cz/cgi-bin/koha/oai.pl' WHERE import_conf_id=306;
UPDATE oai_harvest_conf SET url='https://aleph.techlib.cz/OAI' WHERE import_conf_id=307;
UPDATE oai_harvest_conf SET url='https://ipac.kvkli.cz/i2/i2.ws.oai.cls' WHERE import_conf_id=308;
UPDATE oai_harvest_conf SET url='https://www.vkta.cz/Clavius/l.dll' WHERE import_conf_id=311;
UPDATE oai_harvest_conf SET url='https://katalog.kkvysociny.cz/clavius/l.dll' WHERE import_conf_id=312;
UPDATE oai_harvest_conf SET url='https://aleph.svkhk.cz/OAI' WHERE import_conf_id=313;
UPDATE oai_harvest_conf SET url='https://aleph.vkol.cz/OAI' WHERE import_conf_id=315;
UPDATE oai_harvest_conf SET url='https://aleph.nkp.cz/OAI' WHERE import_conf_id=316;
UPDATE oai_harvest_conf SET url='https://aleph.nkp.cz/OAI' WHERE import_conf_id=319;
UPDATE oai_harvest_conf SET url='https://aleph.mzk.cz/OAI' WHERE import_conf_id=320;
UPDATE oai_harvest_conf SET url='https://aleph.nkp.cz/OAI' WHERE import_conf_id=321;
UPDATE oai_harvest_conf SET url='https://aleph.mzk.cz/OAI' WHERE import_conf_id=324;
UPDATE oai_harvest_conf SET url='https://aleph.nkp.cz/OAI' WHERE import_conf_id=325;
UPDATE oai_harvest_conf SET url='https://aleph.nkp.cz/OAI' WHERE import_conf_id=326;
UPDATE oai_harvest_conf SET url='https://katalog.cbvk.cz/i2/i2.ws.oai.cls' WHERE import_conf_id=328;
UPDATE oai_harvest_conf SET url='https://aleph.lib.cas.cz/OAI' WHERE import_conf_id=330;
UPDATE oai_harvest_conf SET url='https://katalog.svkos.cz/OAI' WHERE import_conf_id=335;
UPDATE oai_harvest_conf SET url='https://web2.mlp.cz/cgi/oai' WHERE import_conf_id=345;
UPDATE oai_harvest_conf SET url='https://katalog.knihovnaprerov.cz/l.dll' WHERE import_conf_id=346;
UPDATE oai_harvest_conf SET url='https://katalog.npmk.cz/api/oai' WHERE import_conf_id=348;
UPDATE oai_harvest_conf SET url='https://www.okpb.cz/clavius/l.dll' WHERE import_conf_id=353;
UPDATE oai_harvest_conf SET url='https://aleph.lib.cas.cz/OAI' WHERE import_conf_id=358;
UPDATE oai_harvest_conf SET url='https://aleph.lib.cas.cz/OAI' WHERE import_conf_id=360;
UPDATE oai_harvest_conf SET url='https://aleph.nkp.cz/OAI' WHERE import_conf_id=400;

-- 17. 05. 2018 tomascejpek
UPDATE import_conf SET interception_enabled=TRUE WHERE id=332;

-- 17. 05. 2018 tomascejpek
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (362, 136, 200, 'klskcla', 11, false, true, true, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (362,'https://svk7.svkkl.cz/i2/i2.ws.oai.cls','KLSKCLA','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (164, 'SVITAVY', 'http://www.booksy.cz/', 'http://www.booksy.cz:8080/Carmen/', 'Svitavy');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (364, 164, 200, 'svitavy', 11, false, true, true, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (364,NULL,NULL,'marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (166, 'HISTOGRAFBIB', 'https://biblio.hiu.cas.cz/', 'https://biblio.hiu.cas.cz/search', 'Bibliography');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (366, 166, 200, 'archbib', 11, false, true, true, false, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (366,'https://biblio.hiu.cas.cz/api/oai','cpk-archiv','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (367, 166, 200, 'czhistbib', 11, false, true, true, false, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (367,'https://biblio.hiu.cas.cz/api/oai','cpk-huav','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (168, 'MKFM', 'https://www.knihovnafm.cz/', 'http://katalog.mkmistek.cz:8080/Carmen/', 'Frýdek-Místek');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (368, 168, 200, 'mkfm', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (368,NULL,NULL,'marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (169, 'RKKA', 'http://rkka.cz/', 'http://rkka.cz:8085/opac?dom=RKKA&fn=*SearchForm', 'Karvina');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (369, 169, 200, 'rkka', 11, false, true, true, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,harvest_job_name) VALUES (369,'http://rkka.cz:8085/rkka2cpk/oai',NULL,'xml-marc',NULL,'oaiHarvestOneByOneJob');
UPDATE oai_harvest_conf SET url='https://library.upol.cz/i2/i2.ws.oai.cls' WHERE import_conf_id=359;
UPDATE oai_harvest_conf SET url='https://aleph.svkpk.cz/OAI',set_spec='PNA01-CPK-MARC21' WHERE import_conf_id=337;
UPDATE oai_harvest_conf SET url='https://aleph.lib.cas.cz/OAI' WHERE import_conf_id=349;

-- 25. 06. 2018 tomascejpek
UPDATE kramerius_conf SET harvest_job_name='krameriusHarvestJob' WHERE import_conf_id=99001;

-- 16. 07. 2018 tomascejpek
ALTER TABLE harvested_record ADD COLUMN sigla VARCHAR(10);
CREATE INDEX harvested_record_sigla_idx ON harvested_record(sigla);

-- 23. 07. 2018 tomascejpek
UPDATE import_conf SET filtering_enabled=TRUE WHERE id in (339,99001);

-- 30. 07. 2018 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (39, 337, 'PNA001');

-- 31. 07. 2018 tomascejpek
DROP TABLE authority_record;

-- 06. 08. 2018 tomascejpek
CREATE TABLE authority (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  authority_id         VARCHAR(20),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);
COMMENT ON TABLE authority IS 'table contatining authority ids';
CREATE INDEX authority_harvested_record_idx ON authority(harvested_record_id);
CREATE INDEX authority_idx ON authority(authority_id);

-- 10. 08. 2018 tomascejpek
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1322,137,200,'sfxjibsvkpk',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1322,'http://sfx.jib.cz/sfxirel/cgi/public/get_file.cgi?file=institutional_holding-SVKPL.xml','downloadAndImportRecordsJob','sfx',null);

-- 20. 08. 2018 tomascejpek
DELETE FROM oai_harvest_conf WHERE import_conf_id=332;
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (332,'local:/data/imports/kkkv_upd','importRecordsJob','xml','s/^(.*)/KKV01-$1/');

-- 29. 08. 2018 tomascejpek
UPDATE import_conf SET item_id='aleph' WHERE id=332;
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (43, 332, 'KVG001');

-- 30. 08. 2018 tomascejpek
ALTER TABLE kramerius_conf ADD COLUMN collection VARCHAR(128);

-- 30. 08. 2018 tomascejpek
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99003,130,200,'kram-knav',8,false,true,false,true,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection) VALUES (99003,'https://cdk.lib.cas.cz/search/api/v5.0','https://cdk.lib.cas.cz/solr-select-only/k4',20,'DC',null,'solr',true,'krameriusHarvestJob','vc:c4bb27af-3a51-4ac2-95c7-fd393b489e26');

-- 18. 09. 2018 tomascejpek
UPDATE download_import_conf SET url='http://sfx.jib.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-SVKPL.xml' WHERE import_conf_id=1322;

-- 02. 10. 2018 tomascejpek
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1323,104,200,'sfxjibmus',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1323,'http://sfx.jib.cz/sfxmus3/cgi/public/get_file.cgi?file=institutional_holding.xml','downloadAndImportRecordsJob','sfx',null);
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1324,104,200,'sfxjibkiv',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1324,'http://sfx.jib.cz/sfxkiv3/cgi/public/get_file.cgi?file=institutional_holding.xml','downloadAndImportRecordsJob','sfx',null);
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1325,107,200,'sfxjibtech',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1325,'http://sfx.techlib.cz/sfxlcl41/cgi/public/get_file.cgi?file=institutional_holding-NTK.xml','downloadAndImportRecordsJob','sfx',null);

-- 25. 10. 2018 tomascejpek
CREATE TABLE obalkyknih_annotation (
  id                   DECIMAL(10) PRIMARY KEY,
  book_id              DECIMAL(10),
  cnb                  VARCHAR(32),
  oclc                 VARCHAR(32),
  isbn                 DECIMAL(13),
  updated              TIMESTAMP,
  last_harvest         TIMESTAMP,
  annotation           VARCHAR(1048576)
);
COMMENT ON TABLE obalkyknih_annotation IS 'downloaded annotations from obalkyknih.cz';
CREATE INDEX obalkyknih_annotation_oclc_idx ON obalkyknih_annotation(oclc);
CREATE INDEX obalkyknih_annotation_isbn_idx ON obalkyknih_annotation(isbn);
CREATE INDEX obalkyknih_annotation_cnb_idx ON obalkyknih_annotation(cnb);
CREATE INDEX obalkyknih_annotation_bookid_idx ON obalkyknih_annotation(book_id);
CREATE INDEX isbn_idx ON isbn(isbn);
CREATE INDEX cnb_idx ON cnb(cnb);
CREATE INDEX oclc_idx ON oclc(oclc);

-- 19. 12. 2018 tomascejpek
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99010,101,200,'kram-nlk',8,false,true,false,true,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection) VALUES (99010,'https://cdk.lib.cas.cz/search/api/v5.0','https://cdk.lib.cas.cz/solr-select-only/k4',50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob','"vc:3c06120c-ffc0-4b96-b8df-80bc12e030d9"');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99011,114,200,'kram-svkul',8,false,true,false,true,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection) VALUES (99011,'https://cdk.lib.cas.cz/search/api/v5.0','https://cdk.lib.cas.cz/solr-select-only/k4',50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob','"vc:cd324f70-c034-46f1-9674-e0df4f93de86"');

-- 02. 01. 2019 tomascejpek
ALTER TABLE harvested_record ADD COLUMN last_harvest TIMESTAMP;
CREATE INDEX harvested_record_last_harvest_idx ON harvested_record(last_harvest);

-- 08. 01. 2019 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city) VALUES (170, 'KNEP', 'http://www.knihovna.brandysnl.cz/', 'https://carmen.knihovna.brandysnl.cz/', 'Brandýs nad Labem');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (370, 170, 200, 'knep', 11, false, true, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (370,'https://clavius.knihovna.brandysnl.cz/l.dll','CPK','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (171, 'MKBREC', 'https://www.mkkl.cz/', 'https://katalog.mkkl.cz/katalog/', 'Břeclav');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (371, 171, 200, 'mkbrec', 11, false, true, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (371,'https://breclav.knihovny.net/clavius/l.dll','CPK','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (172, 'CMUZ', 'http://muzeumcaslav.cz/knihovna/', 'http://caslav.knihovny.net/clavius/', 'Čáslav');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (372, 172, 200, 'cmuz', 11, false, true, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (372,'http://caslav.knihovny.net/clavius/l.dll','CPK','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (173, 'MKCK', 'http://www.knih-ck.cz/', 'http://db.knih-ck.cz:8090/Carmen/', 'Český Krumlov');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (373, 173, 200, 'mkck', 11, false, true, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (373,'http://db.knih-ck.cz/clavius/l.dll','CPK','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (174, 'MKHOD', 'https://www.knihovnahod.cz/', 'https://hodonin.knihovny.net/katalog/', 'Hodonín');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (374, 174, 200, 'mkhod', 11, false, true, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (374,'https://hodonin.knihovny.net/katalog/l.dll','CPK','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (175, 'MKHNM', 'https://www.knihovnahradec.cz/', 'https://katalog.knihovnahradec.cz/katalog/', 'Hradec nad Moravicí');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (375, 175, 200, 'mkhnm', 11, false, true, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (375,'https://katalog.knihovnahradec.cz/katalog/l.dll','CPK','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (176, 'MKKL', 'https://www.mkkl.cz/', 'https://katalog.mkkl.cz/Carmen/', 'Kladno');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (376, 176, 200, 'mkkl', 11, false, true, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (376,'https://katalog.mkkl.cz/katalog/l.dll','CPK','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (177, 'MKHK', 'http://www.knihovnahk.cz/', 'https://katalog.kmhk.cz/Carmen/', 'Hradec Králové');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (377, 177, 200, 'mkhk', 11, false, true, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (377,'https://katalog.kmhk.cz/clavius/l.dll','CPK','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (178, 'MKMIL', 'https://www.knihmil.cz/', 'http://katalog.knihmil.cz/lanius/', 'Milevsko');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (378, 178, 200, 'mkmil', 11, false, true, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (378,'http://katalog.knihmil.cz/LANius/l.dll','CPK','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (179, 'MKOR', 'https://www.knihovna-orlova.cz/', 'https://orlova.knihovny.net/clavius/', 'Orlová Lutyně');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (379, 179, 200, 'mkor', 11, false, true, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (379,'https://orlova.knihovny.net/clavius/l.dll','CPK','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (180, 'MKPEL', 'http://www.knih-pe.cz/', 'http://online.knih-pe.cz/', 'Pelhřimov');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (380, 180, 200, 'mkpel', 11, false, true, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (380,'http://online.knih-pe.cz/l.dll','CPK','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (181, 'MKPISEK', 'http://www.knih-pi.cz/', 'http://katalog.knih-pi.cz:8000/', 'Písek');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (381, 181, 200, 'mkpisek', 11, false, true, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (381,'http://katalog.knih-pi.cz:8000/l.dll','CPK','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (182, 'MKSTER', 'https://www.mkzsternberk.cz/', 'http://sternberk.knihovny.net/katalog/', 'Šternberk');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (382, 182, 200, 'mkster', 11, false, true, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (382,'http://sternberk.knihovny.net/katalog/l.dll','CPK','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (183, 'MKTRI', 'https://www.knihovnatrinec.cz/', 'https://katalog.knihovnatrinec.cz/clavius/', 'Třinec');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (383, 183, 200, 'mktri', 11, false, true, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (383,'https://katalog.knihovnatrinec.cz/clavius/l.dll','CPK','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (184, 'MKZN', 'https://www.knihovnazn.cz/', 'https://baze.knihovnazn.cz/katalog/', 'Znojmo');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (384, 184, 200, 'mkzn', 11, false, true, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (384,'https://baze.knihovnazn.cz/katalog/l.dll','CPK','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (185, 'VFU', 'https://sis.vfu.cz/', 'https://katalog.vfu.cz/Carmen/', 'Brno');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (385, 185, 200, 'vfu', 11, false, true, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (385,'http://195.113.198.88:8080/katalog/l.dll','CPK','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (186, 'MKML', 'https://knihovnaml.cz/', 'https://carmen.knihovnaml.cz/Carmen/', 'Mariánské Lázně');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (386, 186, 200, 'mkml', 11, false, true, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (386,'https://clavius.knihovnaml.cz/l.dll','CPK','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (187, 'KNIR', 'https://knir.cz/', 'https://knir.cz/opacsql/', 'Rožnov pod Radhoštěm');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (387, 187, 200, 'knir', 11, false, true, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (387,'http://knir.cz/opacsql/api/oai','cpk','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (188, 'MKOSTROV', 'https://katalog.mkostrov.cz/', 'https://mkostrov.cz/', 'Ostrov');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (388, 188, 200, 'mkostrov', 11, false, true, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (388,'https://katalog.mkostrov.cz/tritius/oai-provider','CPK','marc21',NULL);

-- 14. 01. 2019 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city) VALUES (189, 'MKBER', 'https://www.knihovnaberoun.cz/', 'https://beroun.knihovny.net/Clavius/', 'Beroun');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (389, 189, 200, 'mkber', 11, false, true, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (389,'https://beroun.knihovny.net/Clavius/l.dll','CPK','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (190, 'MKMOST', 'http://www.knihovnamost.cz/', 'https://most.tritius.cz/', 'Most');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (390, 190, 200, 'mkmost', 11, false, true, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (390,'https://most.tritius.cz/tritius/oai-provider','CPK','marc21',NULL);

-- 16. 01. 2019 tomascejpek
UPDATE import_conf SET interception_enabled=true, item_id='other' WHERE id>=370 AND id<=390;
UPDATE import_conf SET interception_enabled=true, item_id='aleph' WHERE id=333;

-- 22. 01. 2019 tomascejpek
UPDATE oai_harvest_conf SET url='https://tritius.knihovnachodov.cz/tritius/oai-provider' WHERE import_conf_id=350;

-- 22. 01. 2019 tomascejpek
UPDATE download_import_conf SET url='http://sfx.techlib.cz/sfxlcl41/cgi/public/get_file.cgi?file=institutional_holding-NTK.xml' WHERE import_conf_id=1318;
UPDATE download_import_conf SET import_job_name='downloadAndImportRecordsJob' WHERE import_conf_id=1318;

-- 29. 01. 2019 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (45, 371, 'BVG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (48, 374, 'HOG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (58, 384, 'ZNG001');

-- 30. 01. 2019 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (44, 370, 'ABG503');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (51, 377, 'HKG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (52, 378, 'PIG501');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (57, 383, 'FMG502');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (60, 386, 'CHG501');

-- 31. 01. 2019 tomascejpek
ALTER TABLE library ADD COLUMN region VARCHAR(15);
UPDATE library SET region='PR' WHERE id in (101,102,104,105,107,121,123,125,126,130,138,163);
UPDATE library SET region='JM' WHERE id in (100,103,161,171,174,184,185);
UPDATE library SET region='SC' WHERE id in (134,136,156,170,172,176,189);
UPDATE library SET region='KV' WHERE id in (132,150,186,188);
UPDATE library SET region='MS' WHERE id in (135,153,168,169,175,179,183);
UPDATE library SET region='VY' WHERE id in (112,180);
UPDATE library SET region='KH' WHERE id in (113,177);
UPDATE library SET region='LI' WHERE id in (108);
UPDATE library SET region='JC' WHERE id in (111,128,173,178,181);
UPDATE library SET region='OL' WHERE id in (115,146,159,182);
UPDATE library SET region='PA' WHERE id in (106,133,140);
UPDATE library SET region='PL' WHERE id in (137);
UPDATE library SET region='ZL' WHERE id in (143,187);
UPDATE library SET region='US' WHERE id in (114,190);
UPDATE library SET region='bibliography' WHERE id in (119,131,142,148,149,155,158,160,165,166);

-- 04. 02. 2019 tomascejpek
ALTER TABLE obalkyknih_toc ADD updated TIMESTAMP;
ALTER TABLE obalkyknih_toc ADD last_harvest TIMESTAMP;

-- 06. 02. 2019 tomascejpek
CREATE INDEX ean_idx ON ean(ean);

-- 06. 02. 2019 tomascejpek
DELETE FROM oai_harvest_conf WHERE import_conf_id=344;
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (344,null,null,null,null);

-- 06. 02. 2019 tomascejpek
CREATE OR REPLACE VIEW import_job_stat AS
  SELECT
    bje.job_execution_id,
    (array_agg(conf_id_param.long_val))[1] import_conf_id,
    bje.start_time,
    bje.end_time,
    bje.status,
    from_param.date_val from_param,
    to_param.date_val to_param
  FROM batch_job_instance bji
    JOIN batch_job_execution bje ON bje.job_instance_id = bji.job_instance_id
    JOIN batch_job_execution_params conf_id_param ON conf_id_param.job_execution_id = bje.job_execution_id AND conf_id_param.key_name = 'configurationId'
    LEFT JOIN batch_job_execution_params to_param ON to_param.job_execution_id = bje.job_execution_id AND to_param.key_name = 'to'
    LEFT JOIN batch_job_execution_params from_param ON from_param.job_execution_id = bje.job_execution_id AND from_param.key_name = 'from'
    JOIN download_import_conf dic ON dic.import_conf_id = conf_id_param.long_val
  WHERE bji.job_name IN ('importRecordsJob', 'multiImportRecordsJob', 'importOaiRecordsJob', 'downloadAndImportRecordsJob', 'zakonyProLidiHarvestJob')
  GROUP BY bje.job_execution_id,from_param.date_val,to_param.date_val
;
CREATE OR REPLACE VIEW import_summary AS
  WITH last_harvest_date AS (
      SELECT
        import_conf_id,
        COALESCE(MAX(CASE WHEN status = 'COMPLETED' THEN to_param END), MAX(CASE WHEN status = 'COMPLETED' THEN end_time END)) last_successful_harvest_date,
        COALESCE(MAX(CASE WHEN status = 'FAILED' THEN to_param END), MAX(CASE WHEN status = 'FAILED' THEN end_time END)) last_failed_harvest_date,
        COALESCE(MIN(end_time), MIN(to_param)) first_harvest_date
      FROM import_job_stat
      GROUP BY import_conf_id
  )
  SELECT ic.id, l.name, ic.id_prefix, dic.url, dic.import_job_name, lhd.last_successful_harvest_date, lhd.last_failed_harvest_date
  FROM last_harvest_date lhd
    JOIN import_conf ic ON ic.id = lhd.import_conf_id
    LEFT JOIN download_import_conf dic ON dic.import_conf_id = ic.id
    JOIN library l ON l.id = ic.library_id
;

-- 11. 02. 2019 tomascejpek
ALTER TABLE import_conf ADD COLUMN metaproxy_enabled BOOLEAN DEFAULT FALSE;
UPDATE import_conf SET metaproxy_enabled=TRUE WHERE id IN (300,301,304,307,308,313,314,315,316,321,325,326,328,330,335,336,337,338,343);

-- 01. 03. 2019 tomascejpek
UPDATE oai_harvest_conf SET set_spec = '3' WHERE import_conf_id = 342;
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (393, 142, 200, 'bmc', 8, false, true, true, false, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,harvest_job_name) VALUES (393,'http://oai.medvik.cz/bmc/oai','79','xml-marc',NULL,'oaiHarvestOneByOneJob');

-- 04. 03. 2019 tomascejpek
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99012,115,200,'kram-vkol',8,false,true,false,true,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection) VALUES (99012,'https://cdk.lib.cas.cz/search/api/v5.0','https://cdk.lib.cas.cz/solr-select-only/k4',50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob','"vc:b7b1b67a-25d1-4055-905d-45fedfc6a2b5"');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99013,128,200,'kram-cbvk',8,false,true,false,true,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection) VALUES (99013,'https://cdk.lib.cas.cz/search/api/v5.0','https://cdk.lib.cas.cz/solr-select-only/k4',50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob','"vc:4e6b7ee5-3374-4cde-9289-e1f6a2a335b2"');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99014,113,200,'kram-svkhk',8,false,true,false,true,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection) VALUES (99014,'https://cdk.lib.cas.cz/search/api/v5.0','https://cdk.lib.cas.cz/solr-select-only/k4',50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob','"vc:d34ba74b-026a-4c60-aee7-9250a307952c"');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99015,102,200,'kram-mkp',8,false,true,false,true,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection) VALUES (99015,'https://cdk.lib.cas.cz/search/api/v5.0','https://cdk.lib.cas.cz/solr-select-only/k4',50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob','"vc:d4b466de-5435-4b76-bff7-2838bbae747b"');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99016,107,200,'kram-ntk',8,false,true,false,true,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection) VALUES (99016,'https://cdk.lib.cas.cz/search/api/v5.0','https://cdk.lib.cas.cz/solr-select-only/k4',50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob','"vc:f750b424-bda4-4113-849a-5e9dbbfb5846"');

-- 05. 03. 2019 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (192, 'KKDVY', 'https://www.kkdvyskov.cz/', 'https://www.library.sk/arl-vy/', 'Vyškov', 'JM');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (392, 192, 200, 'kkdvy', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (392,'https://www.library.sk/arl-vy/cs/oai/','CPK','oai_marcxml_cpk',NULL,'s/[^:]+:[^:]+:[^:]+:(.+)/VyUsCat_$1/');

-- 05. 03. 2019 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (191, 'UZEI', 'https://www.uzei.cz/', 'https://aleph.uzei.cz/', 'Praha', 'PR');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (391, 191, 200, 'uzei', 11, false, true, true, true, 'U', 'aleph');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format) VALUES (391,null,'importRecordsJob','xml');

-- 08. 03. 2019 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (47, 373, 'CKG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (49, 375, 'OPG503');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (54, 380, 'PEG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (55, 381, 'PIG001');

-- 14. 03. 2019 tomascejpek
UPDATE oai_harvest_conf SET url='https://ipac.kvkli.cz/arl-li/cs/oai/',set_spec='CPK',extract_id_regex='s/[^:]+:[^:]+:[^:]+:(.+)/LiUsCat_$1/' WHERE import_conf_id=308;
UPDATE oai_harvest_conf SET url='https://katalog.svkul.cz/l.dll' WHERE import_conf_id=314;

-- 25. 03. 2019 tomascejpek
UPDATE library SET url='https://www.knihovnabreclav.cz/',catalog_url='https://breclav.knihovny.net/Carmen' WHERE id=171;
UPDATE oai_harvest_conf SET url='https://aleph.knihovna-pardubice.cz/OAI',set_spec='PAG_OAI_CPK_MARC21' WHERE import_conf_id=333;

-- 18. 04. 2019 tomascejpek
UPDATE oai_harvest_conf SET set_spec='CPK_1' WHERE import_conf_id=350;

-- 08. 03. 2019 tomacejpek
ALTER TABLE import_conf ADD COLUMN ziskej_enabled BOOLEAN DEFAULT FALSE;
UPDATE import_conf SET ziskej_enabled=TRUE WHERE id IN (300,301,302,304,306,307,308,311,312,313,314,315,316,319,321,
  324,325,326,328,330,331,332,333,334,335,336,337,338,340,342,343,346,348,349,350,353,356,358,359,360,361,362,363,364,
  365,366,367,368,369,370,371,372,373,374,375,376,377,378,379,380,381,382,383,384,385,386,387,388,389,390,391,392,393);

-- 26. 04. 2019 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (50, 376, 'KLG002');

-- 09. 05. 2019 tomascejpek
UPDATE kramerius_conf SET query_rows=50,metadata_stream='BIBLIO_MODS',collection='"vc:c4bb27af-3a51-4ac2-95c7-fd393b489e26"' WHERE import_conf_id=99003;

-- 20. 05. 2019 tomascejpek
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1326,177,200,'sfxjibmkhk',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1326,'https://sfx.knihovny.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-KMHK.xml','downloadAndImportRecordsJob','sfx',null);

-- 29. 05. 2019 tomascejpek
UPDATE download_import_conf SET url='https://sfx.knihovny.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-NKP.xml' WHERE import_conf_id=1305;
UPDATE download_import_conf SET url='https://sfx.knihovny.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-VKOL.xml' WHERE import_conf_id=1306;
UPDATE download_import_conf SET url='https://sfx.knihovny.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-JVKCB.xml' WHERE import_conf_id=1307;
UPDATE download_import_conf SET url='https://sfx.knihovny.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-KKFB.xml' WHERE import_conf_id=1308;
UPDATE download_import_conf SET url='https://sfx.knihovny.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-KVKLI.xml' WHERE import_conf_id=1309;
UPDATE download_import_conf SET url='https://sfx.knihovny.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-MKP.xml' WHERE import_conf_id=1310;
UPDATE download_import_conf SET url='https://sfx.knihovny.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-MSVK.xml' WHERE import_conf_id=1311;
UPDATE download_import_conf SET url='https://sfx.knihovny.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-SVKHK.xml' WHERE import_conf_id=1312;
UPDATE download_import_conf SET url='https://sfx.knihovny.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-MZK.xml' WHERE import_conf_id=1314;
UPDATE download_import_conf SET url='https://sfx.knihovny.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding.xml' WHERE import_conf_id=1315;
UPDATE download_import_conf SET url='https://sfx.knihovny.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-SVKPL.xml' WHERE import_conf_id=1322;

-- 13. 06. 2019 tomascejpek
UPDATE oai_harvest_conf SET harvest_job_name='cosmotronHarvestJob' WHERE import_conf_id=392;
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (66, 392, 'VYG001');

-- 03. 07. 2019 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (197, 'MKTRUT', 'https://www.mktrutnov.cz/', 'https://trutnov.tritius.cz/', 'Trutnov', 'KH');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, ziskej_enabled) VALUES (397, 197, 200, 'mktrut', 11, false, true, true, true, 'U', 'other', true);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (397,'https://trutnov.tritius.cz/tritius/oai-provider','CPK_1','marc21',NULL);

-- 03. 07. 2019 tomascejpek
UPDATE oai_harvest_conf SET set_spec='CPK_1' WHERE import_conf_id=388;

-- 03. 07. 2019 tomascejpek
UPDATE download_import_conf SET extract_id_regex='s/^(.*)/UZP01-$1/',url='local:/data/imports/uzp01_upd' WHERE import_conf_id=391;

-- 19. 09. 2019 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (196, 'CZPB', 'https://kas.uzei.cz/', 'https://kas.uzei.cz/', 'Bibliography', 'bibliography');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, ziskej_enabled) VALUES (396, 196, 200, 'czpb', 11, false, true, true, false, 'U', true);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format) VALUES (396,'local:/data/imports/uzp02_upd','importRecordsJob','xml');

-- 03. 07. 2019 tomascejpek
UPDATE library SET catalog_url='https://katalog.kjm.cz' WHERE id=103;
UPDATE import_conf SET base_weight=11,filtering_enabled=TRUE,interception_enabled=TRUE,item_id='other',ziskej_enabled=TRUE WHERE id=303;
UPDATE oai_harvest_conf SET url='https://katalog.kjm.cz/oai',set_spec='KJMCPKDATE',metadata_prefix='oai_marcxml_cpk',extract_id_regex='s/[^:]+:[^:]+:([^\\/]+)\\/([^\\/]+)/$1_$2/',harvest_job_name='cosmotronHarvestJob' WHERE import_conf_id=303;

-- 15. 08. 2019 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (65, 333, 'PAG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (62, 388, 'KVG501');

-- 09. 09. 2019 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (198, 'KNIHKM', 'https://www.knihkm.cz/', 'https://kromeriz.tritius.cz/', 'Kroměříž', 'ZL');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, ziskej_enabled) VALUES (398, 198, 200, 'knihkm', 11, false, true, true, true, 'U', 'other', true);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (398,'https://kromeriz.tritius.cz/tritius/oai-provider','CPK_1','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (199, 'MKCHOM', 'https://www.chomutovskaknihovna.cz/', 'https://chomutovskaknihovna.tritius.cz/', 'Chomutov', 'US');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, ziskej_enabled) VALUES (399, 199, 200, 'mkchom', 11, false, true, true, true, 'U', 'other', true);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (399,'https://chomutovskaknihovna.tritius.cz/tritius/oai-provider','CPK_1','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (201, 'MKKOLIN', 'https://knihovnakolin.cz/', 'https://tritius.knihovnakolin.cz/', 'Kolin', 'SC');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, ziskej_enabled) VALUES (401, 201, 200, 'mkkolin', 11, false, true, true, true, 'U', 'other', true);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (401,'https://tritius.knihovnakolin.cz/tritius/oai-provider','CPK_1','marc21',NULL);
UPDATE library SET catalog_url='https://fmi.tritius.cz/' WHERE id=168;
UPDATE import_conf SET item_id='other' WHERE id=368;
UPDATE oai_harvest_conf SET url='https://fmi.tritius.cz/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=368;
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (202, 'MKPLZEN', 'https://knihovna.plzen.eu/', 'https://tritius.plzen.eu/', 'Plzeň', 'PL');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, ziskej_enabled) VALUES (402, 202, 200, 'mkplzen', 11, false, true, true, true, 'U', 'other', true);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (402,'https://tritius.plzen.eu/tritius/oai-provider','CPK_1','marc21',NULL);
UPDATE library SET name='MKSVIT',catalog_url='https://booksy.tritius.cz/',region='PA' WHERE id=164;
UPDATE import_conf SET id_prefix='mksvit',item_id='other' WHERE id=364;
UPDATE oai_harvest_conf SET url='https://booksy.tritius.cz/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=364;

-- 09. 09. 2019 tomascejpek
UPDATE import_conf SET item_id='other' WHERE id=369;
UPDATE oai_harvest_conf SET url='https://opac.rkka.cz/rkka2cpk/oai',set_spec='12' WHERE import_conf_id=369;
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, ziskej_enabled) VALUES (403, 169, 200, 'rkka', 11, false, true, true, true, 'U', 'other', true);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,harvest_job_name) VALUES (403,'https://opac.rkka.cz/rkka2cpk/oai','1','xml-marc',NULL,'oaiHarvestOneByOneJob');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, ziskej_enabled) VALUES (404, 169, 200, 'rkka', 11, false, true, true, true, 'U', 'other', true);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,harvest_job_name) VALUES (404,'https://opac.rkka.cz/rkka2cpk/oai','3','xml-marc',NULL,'oaiHarvestOneByOneJob');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, ziskej_enabled) VALUES (405, 169, 200, 'rkka', 11, false, true, true, true, 'U', 'other', true);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,harvest_job_name) VALUES (405,'https://opac.rkka.cz/rkka2cpk/oai','51','xml-marc',NULL,'oaiHarvestOneByOneJob');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, ziskej_enabled) VALUES (406, 169, 200, 'rkka', 11, false, true, true, true, 'U', 'other', true);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,harvest_job_name) VALUES (406,'https://opac.rkka.cz/rkka2cpk/oai','63','xml-marc',NULL,'oaiHarvestOneByOneJob');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, ziskej_enabled) VALUES (407, 169, 200, 'rkka', 11, false, true, true, true, 'U', 'other', true);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,harvest_job_name) VALUES (407,'https://opac.rkka.cz/rkka2cpk/oai','108','xml-marc',NULL,'oaiHarvestOneByOneJob');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, ziskej_enabled) VALUES (408, 169, 200, 'rkka', 11, false, true, true, true, 'U', 'other', true);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,harvest_job_name) VALUES (408,'https://opac.rkka.cz/rkka2cpk/oai','1277','xml-marc',NULL,'oaiHarvestOneByOneJob');

-- 09. 09. 2019 tomascejpek
UPDATE oai_harvest_conf SET set_spec='CPK_1' WHERE import_conf_id=334;

-- 27. 09. 2019 tomascejpek
UPDATE oai_harvest_conf SET set_spec='KJMALL' WHERE import_conf_id=303;

-- 27. 09. 2019 tomascejpek
UPDATE oai_harvest_conf SET url='https://tritius.knihovnatrinec.cz/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=383;

-- 27. 09. 2019 tomascejpek
UPDATE oai_harvest_conf SET url='https://pribram.tritius.cz/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=356;

-- 02. 10. 2019 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (209, 'MKCL', 'http://www.knihovna-cl.cz/', 'https://tritius.knihovna-cl.cz/', 'Česká Lípa', 'LI');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, ziskej_enabled) VALUES (409, 209, 200, 'mkcl', 11, false, true, true, true, 'U', 'other', true);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (409,'https://tritius.knihovna-cl.cz/tritius/oai-provider','CPK_1','marc21',NULL);

-- 14. 10. 2019 tomascejpek
UPDATE oai_harvest_conf SET set_spec='default' WHERE import_conf_id=343;

-- 31. 10. 2019 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (210, 'MKSEM', 'https://www.knihovnasemily.cz', 'https://semily.tritius.cz/', 'Semily', 'LI');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, ziskej_enabled) VALUES (410, 210, 200, 'mksem', 11, false, true, true, true, 'U', 'other', true);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (410,'https://semily.tritius.cz/tritius/oai-provider','CPK_1','marc21',NULL);

-- 04. 11. 2019 tomascejpek
UPDATE oai_harvest_conf SET url='https://katalog.svkul.cz/tritius/oai-provider',set_spec='CPK_2' WHERE import_conf_id=314;

-- 15. 11. 2019 tomascejpek
UPDATE oai_harvest_conf SET set_spec='CPK_1' WHERE import_conf_id=390;

-- 15. 11. 2019 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (64, 390, 'MOG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (71, 397, 'TUG001');

-- 29. 11. 2019 tomascejpek
UPDATE oai_harvest_conf set extract_id_regex='s/[^:]+:[^:]+:([^\\/]+)\\/([^\\/]+)/$1_$2/' WHERE import_conf_id=359;

-- 29. 11. 2019 tomascejpek
UPDATE oai_harvest_conf SET url='https://brandysnl.tritius.cz/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=370;

-- 12. 12. 2019 tomascejpek
ALTER TABLE harvested_record ADD COLUMN publisher VARCHAR(100);
ALTER TABLE harvested_record ADD COLUMN edition VARCHAR(10);
ALTER TABLE harvested_record ADD COLUMN disadvantaged BOOLEAN DEFAULT(TRUE);
CREATE INDEX harvested_record_disadvantaged_idx ON harvested_record(disadvantaged);
CREATE TABLE anp_title (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  anp_title            VARCHAR(255),
  similarity_enabled   BOOLEAN DEFAULT FALSE,
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);
CREATE INDEX anp_title_harvested_record_idx ON anp_title(harvested_record_id);

-- 17. 12. 2019 tomascejpek
UPDATE oai_harvest_conf SET url='https://tritius.knihovnaprerov.cz/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=346;

-- 17. 12. 2019 tomascejpek
UPDATE oai_harvest_conf SET url='https://tritius.kkvysociny.cz/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=312;

-- 17. 12. 2019 tomascejpek
CREATE SEQUENCE biblio_linker_seq_id MINVALUE 1;
CREATE TABLE biblio_linker (
  id                   DECIMAL(10) DEFAULT NEXTVAL('"biblio_linker_seq_id"')  PRIMARY KEY,
  updated              TIMESTAMP
);
ALTER TABLE harvested_record
  ADD COLUMN biblio_linker_id DECIMAL(10),
  ADD COLUMN biblio_linker_similar BOOLEAN DEFAULT FALSE,
  ADD COLUMN next_biblio_linker_flag BOOLEAN DEFAULT TRUE,
  ADD COLUMN next_biblio_linker_similar_flag BOOLEAN DEFAULT TRUE,
  ADD COLUMN biblio_linker_keys_hash CHAR(40),
  ADD COLUMN bl_disadvantaged BOOLEAN DEFAULT TRUE,
  ADD COLUMN bl_author VARCHAR(200),
  ADD COLUMN bl_publisher VARCHAR(200),
  ADD COLUMN bl_series VARCHAR(200);
ALTER TABLE harvested_record ADD CONSTRAINT harvested_record_biblio_linker_fk FOREIGN KEY (biblio_linker_id) REFERENCES biblio_linker(id);
CREATE INDEX hr_biblilinker_dedup_record_id_idx ON harvested_record(biblio_linker_id,dedup_record_id);
CREATE INDEX hr_next_biblio_linker_flag_ids ON harvested_record(next_biblio_linker_flag);
CREATE INDEX hr_next_biblio_linker_similar_flag_ids ON harvested_record(next_biblio_linker_similar_flag);
CREATE SEQUENCE biblio_linker_similar_seq_id MINVALUE 1;
CREATE TABLE biblio_linker_similar (
  id                   DECIMAL(10) DEFAULT NEXTVAL('"biblio_linker_similar_seq_id"') PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  harvested_record_similar_id DECIMAL(10),
  url_id               TEXT,
  type                 VARCHAR(20),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);
CREATE INDEX bls_harvested_record_id_idx ON biblio_linker_similar(harvested_record_id);
CREATE TABLE bl_title (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  title                VARCHAR(255),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);
CREATE INDEX bl_title_harvested_record_idx ON bl_title(harvested_record_id);
CREATE TABLE bl_common_title (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  title                VARCHAR(255),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);
CREATE INDEX bl_common_title_harvested_record_idx ON bl_common_title(harvested_record_id);
CREATE TABLE bl_entity (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  entity               VARCHAR(200),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);
CREATE INDEX bl_entity_harvested_record_idx ON bl_entity(harvested_record_id);
CREATE TABLE bl_topic_key (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  topic_key            VARCHAR(20),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);
CREATE INDEX bl_topic_key_harvested_record_idx ON bl_topic_key(harvested_record_id);
CREATE TABLE bl_language (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  lang                 VARCHAR(5),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);
CREATE INDEX bl_language_harvested_record_idx ON bl_language(harvested_record_id);
ALTER TABLE import_conf ADD COLUMN generate_biblio_linker_keys BOOLEAN DEFAULT TRUE;
UPDATE import_conf SET generate_biblio_linker_keys=FALSE
  WHERE id IN (344,354,1300,1301,1302,1304,1305,1306,1307,1308,1309,1310,1311,1312,1313,1314,1315,1316,1318,1319,1320,1321,1322,1323,1324,1325,1326);

-- 21. 01. 2020 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (212, 'MKHOL', 'https://knihovna.holesov.info/', 'https://tritius.holesov.info/', 'Holešov', 'ZL');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, ziskej_enabled) VALUES (412, 212, 200, 'mkhol', 11, false, true, true, true, 'U', 'other', true);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (412,'https://tritius.holesov.info/tritius/oai-provider','CPK_1','marc21',NULL);

-- 21. 01. 2020 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (211, 'MKJAR', 'http://www.knihovnajaromer.cz/', 'https://jaromer.tritius.cz/', 'Jaroměř', 'KH');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, ziskej_enabled) VALUES (411, 211, 200, 'mkjar', 11, false, true, true, true, 'U', 'other', true);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (411,'https://jaromer.tritius.cz/tritius/oai-provider','CPK_1','marc21',NULL);

-- 21. 01. 2020 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (67, 391, 'ABA009');

-- 24. 01. 2020 tomascejpek
UPDATE oai_harvest_conf SET url='https://chodov.tritius.cz/tritius/oai-provider' WHERE import_conf_id=350;

-- 11. 02. 2020 tomascejpek
UPDATE oai_harvest_conf SET url='https://tritius.knihovnachodov.cz/tritius/oai-provider' WHERE import_conf_id=350;

-- 03. 03. 2020 tomascejpek
UPDATE oai_harvest_conf SET url='https://tritius.booksy.cz/tritius/oai-provider' WHERE import_conf_id=364;

-- 09. 03. 2020 tomascejpek
UPDATE oai_harvest_conf SET url='https://katalog-usti.knihovna-uo.cz/cgi-bin/koha/oai.pl' WHERE import_conf_id=340;

-- 09. 03. 2020 tomascejpek
UPDATE oai_harvest_conf SET url='https://db.knih-ck.cz/clavius/l.dll' WHERE import_conf_id=373;

-- 09. 03. 2020 tomascejpek
UPDATE oai_harvest_conf SET url='https://katalog.knihmil.cz/LANius/l.dll' WHERE import_conf_id=378;

-- 10. 03. 2020 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (76, 364, 'SVG001');

-- 11. 03. 2020 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (78, 369, 'KAG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (79, 403, 'KAG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (80, 404, 'KAG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (81, 405, 'KAG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (82, 406, 'KAG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (83, 407, 'KAG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (84, 408, 'KAG001');

-- 17. 03. 2020 tomascejpek
UPDATE oai_harvest_conf SET url='https://web2.mlp.cz/cgi/oaie' WHERE import_conf_id=345;

-- 26. 03. 2020 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (213, 'BOOKPORT', 'https://www.bookport.cz/', '', null, null);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (413, 213, 200, 'bookport', 11, false, true, true, false, 'U');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name, format) VALUES (413,'https://www.bookport.cz/marc21.xml','importRecordsJob','xml');

-- 17. 04. 2020 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (214, 'MUNIPRESS', 'https://www.press.muni.cz/', '', null, 'ebook');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (414, 214, 200, 'munipress', 11, false, true, true, false, 'U');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name, format) VALUES (414,null,null,null);

-- 21. 04. 2020 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (215, 'MKKLAT', 'http://www.knih-kt.cz/', 'https://klatovy.tritius.cz/', 'Klatovy', 'PL');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, ziskej_enabled) VALUES (415, 215, 200, 'mkklat', 11, false, true, true, true, 'U', 'other', true);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (415,'https://klatovy.tritius.cz/tritius/oai-provider','CPK_1','marc21',NULL);

-- 21. 04. 2020 tomascejpek
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99017,191,200,'kram-uzei',8,false,true,false,true,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection) VALUES (99017,'https://cdk.lib.cas.cz/search/api/v5.0','https://cdk.lib.cas.cz/solr-select-only/k4',50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob','"vc:91a19b3d-8271-4889-8652-6c9d5864bd1b"');

-- 22. 04. 2020 tomascejpek
ALTER TABLE antikvariaty ADD COLUMN last_harvest TIMESTAMP, ADD COLUMN updated_original TIMESTAMP;
ALTER TABLE antikvariaty_catids DROP CONSTRAINT antikvariaty_catids_fk;
ALTER TABLE antikvariaty_catids ADD CONSTRAINT antikvariaty_catids_fk FOREIGN KEY (antikvariaty_id) REFERENCES antikvariaty(id) ON DELETE CASCADE;
CREATE INDEX antik_catids_ids ON antikvariaty_catids(antikvariaty_id);
CREATE OR REPLACE VIEW antikvariaty_url_view AS
SELECT
  hr.dedup_record_id,
  a.url,
  a.updated,
  a.last_harvest
FROM harvested_record hr
  INNER JOIN antikvariaty_catids ac on hr.cluster_id = ac.id_from_catalogue
  INNER JOIN antikvariaty a on ac.antikvariaty_id = a.id
ORDER BY hr.weight DESC;
UPDATE download_import_conf SET url='https://muj-antikvariat.cz/assets/obalkyknih.xml' WHERE import_conf_id=500;

-- 28. 04. 2020 tomascejpek
UPDATE library SET region='ebook' WHERE id in (127,213);

-- 07. 05. 2020 tomascejpek
ALTER TABLE cosmotron_996 ADD COLUMN last_harvest TIMESTAMP;
CREATE INDEX cosmotron_996_last_harvest_idx ON cosmotron_996(last_harvest);

-- 11. 05. 2020 tomascejpek
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99004,104,200,'kram-nkp', 8,false,true,false,true,'U',null,true,null,null);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (99004,'http://kramerius5.nkp.cz/oaiprovider','monograph','oai_dc',NULL);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection) VALUES (99004,'https://kramerius5.nkp.cz/search/api/v5.0',null,50,'DC',null,'fedora',true,null,null);

-- 12. 05. 2020 tomascejpek
CREATE TABLE kram_availability (
  id                SERIAL,
  import_conf_id    DECIMAL(10) NOT NULL,
  uuid              VARCHAR(100) NOT NULL,
  availability      VARCHAR(20) NOT NULL,
  dnnt              BOOLEAN DEFAULT FALSE,
  updated           TIMESTAMP NOT NULL,
  last_harvest      TIMESTAMP NOT NULL,
  CONSTRAINT kram_availability_pk PRIMARY KEY(id),
  FOREIGN KEY (import_conf_id) REFERENCES import_conf(id)
);
CREATE INDEX kram_availability_conf_uuid_idx ON kram_availability(import_conf_id, uuid);
CREATE TABLE uuid (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  uuid                 VARCHAR(100),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);
CREATE INDEX uuid_harvested_record_idx ON uuid(harvested_record_id);
CREATE VIEW kram_availability_view AS
SELECT
  hr.dedup_record_id,
  ka.import_conf_id,
  ka.uuid,
  ka.updated,
  ka.last_harvest
FROM harvested_record hr
  INNER JOIN uuid on uuid.harvested_record_id = hr.id
  INNER JOIN kram_availability ka on ka.uuid = uuid.uuid
;
ALTER TABLE kramerius_conf ADD COLUMN availability_source_url VARCHAR(128);
ALTER TABLE kramerius_conf ADD COLUMN availability_dest_url VARCHAR(128);
CREATE OR REPLACE VIEW kram_availability_job_stat AS
SELECT
  bje.job_execution_id,
  (array_agg(conf_id_param.long_val))[1] import_conf_id,
  bje.start_time,
  bje.end_time,
  bje.status
FROM batch_job_instance bji
  JOIN batch_job_execution bje ON bje.job_instance_id = bji.job_instance_id
  JOIN batch_job_execution_params conf_id_param ON conf_id_param.job_execution_id = bje.job_execution_id AND conf_id_param.key_name = 'configurationId'
  JOIN kramerius_conf kc ON kc.import_conf_id = conf_id_param.long_val
WHERE bji.job_name IN ('harvestKramAvailabilityJob')
GROUP BY bje.job_execution_id
;
CREATE OR REPLACE VIEW kram_availability_summary AS
WITH last_harvest_date AS (
  SELECT
    import_conf_id,
    MAX(CASE WHEN status = 'COMPLETED' THEN start_time END) last_successful_harvest_date,
    MAX(CASE WHEN status = 'FAILED' THEN start_time END) last_failed_harvest_date
  FROM kram_availability_job_stat
  GROUP BY import_conf_id
)
SELECT ic.id,
       l.name,
       ic.id_prefix,
       CASE WHEN kc.availability_source_url IS NOT NULL THEN kc.availability_source_url ELSE kc.url END url,
       lhd.last_successful_harvest_date,
       lhd.last_failed_harvest_date
FROM last_harvest_date lhd
  JOIN import_conf ic ON ic.id = lhd.import_conf_id
  LEFT JOIN kramerius_conf kc ON kc.import_conf_id = ic.id
  JOIN library l ON l.id = ic.library_id
  ORDER BY lhd.last_successful_harvest_date DESC NULLS LAST
;
UPDATE kramerius_conf SET availability_dest_url='http://www.digitalniknihovna.cz/mzk/uuid/' WHERE import_conf_id=99001;
UPDATE kramerius_conf SET availability_dest_url='http://kramerius5.nkp.cz/uuid/' WHERE import_conf_id=99004;
UPDATE kramerius_conf SET availability_source_url='https://kramerius.lib.cas.cz/search/api/v5.0',availability_dest_url='https://kramerius.lib.cas.cz/search/handle/' WHERE import_conf_id=99003;
UPDATE kramerius_conf SET availability_source_url='https://kramerius.medvik.cz/search/api/v5.0',availability_dest_url='https://kramerius.medvik.cz/search/handle/' WHERE import_conf_id=99010;
UPDATE kramerius_conf SET availability_source_url='https://kramerius.svkul.cz/search/api/v5.0',availability_dest_url='https://kramerius.svkul.cz/search/handle/' WHERE import_conf_id=99011;
UPDATE kramerius_conf SET availability_source_url='http://kramerius.kr-olomoucky.cz/search/api/v5.0',availability_dest_url='https://kramerius.kr-olomoucky.cz/search/handle/' WHERE import_conf_id=99012;
UPDATE kramerius_conf SET availability_source_url='https://kramerius.cbvk.cz/search/api/v5.0',availability_dest_url='https://kramerius.cbvk.cz/search/handle/' WHERE import_conf_id=99013;
UPDATE kramerius_conf SET availability_source_url='https://kramerius4.svkhk.cz/search/api/v5.0',availability_dest_url='https://kramerius.svkhk.cz/search/handle/' WHERE import_conf_id=99014;
UPDATE kramerius_conf SET availability_source_url='https://kramerius4.mlp.cz/search/api/v5.0',availability_dest_url='https://digitalniknihovna.mlp.cz/' WHERE import_conf_id=99015;
UPDATE kramerius_conf SET availability_source_url='https://kramerius.techlib.cz/search/api/v5.0',availability_dest_url='https://kramerius.techlib.cz/kramerius-web-client/view/' WHERE import_conf_id=99016;
UPDATE kramerius_conf SET availability_source_url='https://kramerius.uzei.cz/search/api/v5.0',availability_dest_url='http://dk.uzei.cz/uzei/uuid/' WHERE import_conf_id=99017;

-- 12. 05. 2020 tomascejpek
UPDATE oai_harvest_conf SET url='https://kmhk.tritius.cz/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=377;

-- 22. 05. 2020 tomascejpek
ALTER TABLE kramerius_conf ADD COLUMN availability_harvest_frequency CHAR(1) DEFAULT 'U';

-- 01. 06. 2020 tomascejpek
ALTER TABLE import_conf ADD COLUMN indexed BOOLEAN DEFAULT TRUE;

-- 08. 06. 2020 tomascejpek
ALTER TABLE kramerius_conf ADD COLUMN dnnt_dest_url VARCHAR(128);

-- 08. 06. 2020 tomascejpek
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99018,106,200,'kram-tre',8,false,true,false,true,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99018,'https://k5.digiknihovna.cz/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'https://k5.digiknihovna.cz/search/handle/');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99019,135,200,'kram-svkos',8,false,true,false,true,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99019,'https://camea2.svkos.cz/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'https://camea2.svkos.cz/search/handle/');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99020,132,200,'kram-kkkv',8,false,true,false,true,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99020,'http://k4.kr-karlovarsky.cz/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'http://k4.kr-karlovarsky.cz/search/handle/');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99021,108,200,'kram-kvkl',8,false,true,false,true,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99021,'http://kramerius.kvkli.cz/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'http://kramerius.kvkli.cz/search/handle/');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99022,137,200,'kram-svkpk',8,false,true,false,true,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99022,'https://k5.svkpk.cz/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'https://k5.svkpk.cz/search/handle/');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99023,143,200,'kram-kfbz',8,false,true,false,true,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99023,'http://kramerius.kfbz.cz/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'http://kramerius.kfbz.cz/uuid/');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99024,136,200,'kram-svkkl',8,false,true,false,true,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99024,'http://kramerius.svkkl.cz/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'http://kramerius.svkkl.cz/search/handle/');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99025,112,200,'kram-kkvy',8,false,true,false,true,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99025,'http://kramerius.kkvysociny.cz/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'http://kramerius.kkvysociny.cz/uuid/');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99026,133,200,'kram-kkpc',8,false,true,false,true,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99026,'http://kramerius.knihovna-pardubice.cz:8089/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'http://kramerius.knihovna-pardubice.cz:8089/search/handle/');

-- 08. 06. 2020 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (85, 409, 'CLG001');

-- 08. 06. 2020 tomascejpek
DELETE FROM sigla WHERE id=54;

-- 08. 06. 2020 tomascejpek
UPDATE kramerius_conf SET dnnt_dest_url='https://ndk.cz/uuid/' WHERE import_conf_id=99004;

-- 17. 06. 2020 tomascejpek
CREATE TABLE ziskej_library (
  id                SERIAL,
  sigla             VARCHAR(10) NOT NULL,
  updated           TIMESTAMP NOT NULL,
  last_harvest      TIMESTAMP NOT NULL,
  CONSTRAINT ziskej_libraries_pk PRIMARY KEY(id)
);

-- 19. 06. 2020 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (99027, 'KRAM-CUNI', '', '', '','kramerius');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99027,99027,200,'kram-cuni',8,false,true,false,false,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99027,'https://kramerius.cuni.cz/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'http://kramerius.cuni.cz/uk/uuid/');
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (99028, 'KRAM-CUNIFSV', '', '', '','kramerius');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99028,99028,200,'kram-cunifsv',8,false,true,false,false,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99028,'http://kramerius.fsv.cuni.cz/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'http://kramerius.cuni.cz/fsv/uuid/');
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (99029, 'KRAM-CUNILF1', '', '', '','kramerius');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99029,99029,200,'kram-cunilf1',8,false,true,false,false,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99029,'http://kramerius.lf1.cuni.cz/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'http://kramerius.cuni.cz/lf1/uuid/');
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (99030, 'KRAM-DIFMOE', '', '', '','kramerius');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99030,99030,200,'kram-difmoe',8,false,true,false,false,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99030,'https://kramerius.difmoe.eu/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'https://www.difmoe.eu/d/uuid/');
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (99031, 'KRAM-DSMO', '', '', '','kramerius');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99031,99031,200,'kram-dsmo',8,false,true,false,false,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99031,'https://kramerius.army.cz/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'https://kramerius.army.cz/search/handle/');
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (99032, 'KRAM-LMDA', '', '', '','kramerius');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99032,99032,200,'kram-lmda',8,false,true,false,false,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99032,'http://lesnickaprace4.cust.ignum.cz:8080/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'http://lmda.silvarium.cz/uuid/');
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (99033, 'KRAM-MENDELU', '', '', '','kramerius');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99033,99033,200,'kram-mendelu',8,false,true,false,false,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99033,'http://kramerius4.mendelu.cz/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'http://kramerius4.mendelu.cz/search/handle/');
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (99034, 'KRAM-MJH', '', '', '','kramerius');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99034,99034,200,'kram-mjh',8,false,true,false,false,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99034,'http://kramerius.mjh.cz/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'http://kramerius.mjh.cz/uuid/');
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (99035, 'KRAM-MVCHK', '', '', '','kramerius');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99035,99035,200,'kram-mvchk',8,false,true,false,false,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99035,'http://k4.muzeumhk.cz/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'http://k4.muzeumhk.cz/search/handle/');
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (99036, 'KRAM-NACR', '', '', '','kramerius');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99036,99036,200,'kram-nacr',8,false,true,false,false,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99036,'https://kramerius.nacr.cz/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'https://kramerius.nacr.cz/search/handle/');
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (99037, 'KRAM-NFA', '', '', '','kramerius');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99037,99037,200,'kram-nfa',8,false,true,false,false,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99037,'https://library.nfa.cz/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'https://library.nfa.cz/search/handle/');
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (99038, 'KRAM-NM', '', '', '','kramerius');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99038,99038,200,'kram-nm',8,false,true,false,false,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99038,'https://kramerius.nm.cz/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'https://kramerius.nm.cz/uuid/');
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (99039, 'KRAM-PKJAK', '', '', '','kramerius');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99039,99039,200,'kram-pkjak',8,false,true,false,false,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99039,'https://kramerius.npmk.cz/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'https://kramerius.npmk.cz/uuid/');
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (99040, 'KRAM-UPM', '', '', '','kramerius');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99040,99040,200,'kram-upm',8,false,true,false,false,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99040,'http://kramerius.upm.cz/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'http://kramerius.upm.cz/uuid/');
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (99041, 'KRAM-VSE', '', '', '','kramerius');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99041,99041,200,'kram-vse',8,false,true,false,false,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99041,'https://kramerius.vse.cz/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'https://kramerius.vse.cz/search/handle/');
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (99042, 'KRAM-VSUP', '', '', '','kramerius');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99042,99042,200,'kram-vsup',8,false,true,false,false,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99042,'https://kramerius.vsup.cz/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'https://kramerius.vsup.cz/search/handle/');
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (99043, 'KRAM-VUGTK', '', '', '','kramerius');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99043,99043,200,'kram-vugtk',8,false,true,false,false,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99043,'https://kramerius.vugtk.cz/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'https://kramerius.vugtk.cz/search/handle/');
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (99044, 'KRAM-ZCM', '', '', '','kramerius');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99044,99044,200,'kram-zcm',8,false,true,false,false,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99044,'http://kramerius.zcm.cz/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'http://kramerius.zcm.cz/search/handle/');
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (99045, 'KRAM-ZMP', '', '', '','kramerius');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99045,99045,200,'kram-zmp',8,false,true,false,false,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99045,'http://kramerius4.jewishmuseum.cz/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'http://kramerius4.jewishmuseum.cz/search/handle/');

-- 20. 07. 2020 tomascejpek
CREATE OR REPLACE VIEW reharvest_summary AS
WITH reharvest_job_stat AS (
    SELECT
        bje.job_execution_id,
        (array_agg(ic.id))[1]  import_conf_id,
        bje.start_time,
        bje.status
    FROM batch_job_instance bji
        JOIN batch_job_execution bje ON bje.job_instance_id = bji.job_instance_id
        JOIN batch_job_execution_params conf_id_param ON conf_id_param.job_execution_id = bje.job_execution_id AND conf_id_param.key_name = 'configurationId'
        LEFT JOIN batch_job_execution_params reharvest_param ON reharvest_param.job_execution_id = bje.job_execution_id AND reharvest_param.key_name = 'reharvest'
        LEFT JOIN oai_harvest_conf ohc ON ohc.import_conf_id = conf_id_param.long_val
        LEFT JOIN kramerius_conf kc ON kc.import_conf_id = conf_id_param.long_val
        JOIN import_conf ic ON ic.id = ohc.import_conf_id OR ic.id = kc.import_conf_id
    WHERE reharvest_param.string_val='true' and bji.job_name IN ('oaiHarvestJob', 'cosmotronHarvestJob', 'krameriusHarvestJob', 'oaiHarvestOneByOneJob', 'importRecordJob', 'multiImportRecordsJob', 'importOaiRecordsJob')
    GROUP BY bje.job_execution_id
), last_reharvest_date AS (
    SELECT
        import_conf_id,
        COALESCE(MAX(CASE WHEN status = 'COMPLETED' THEN start_time END)) last_successful_harvest_date
    FROM reharvest_job_stat
    GROUP BY import_conf_id
)
SELECT ic.id, l.name, ic.id_prefix, COALESCE(ohc.url, kc.url), ohc.set_spec, lhd.last_successful_harvest_date
FROM last_reharvest_date lhd
        JOIN import_conf ic ON ic.id = lhd.import_conf_id
        LEFT JOIN oai_harvest_conf ohc ON ohc.import_conf_id = ic.id
        LEFT JOIN kramerius_conf kc ON kc.import_conf_id = ic.id
        JOIN library l ON l.id = ic.library_id
WHERE lhd.last_successful_harvest_date IS NOT NULL
ORDER BY lhd.last_successful_harvest_date DESC
;

-- 28. 07. 2020 tomascejpek
ALTER TABLE kram_availability ADD level DECIMAL(10);

-- 03. 08. 2020 tomascejpek
UPDATE oai_harvest_conf SET url='https://mkkl.tritius.cz/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=376;

-- 04. 08. 2020 tomascejpek
UPDATE import_conf SET ziskej_enabled=FALSE;
UPDATE import_conf SET ziskej_enabled=TRUE WHERE id IN (300,301,302,304,307,311,312,314,315,324,330,335,343,356,332,370,383,388);

-- 07. 08. 2020 tomascejpek
UPDATE oai_harvest_conf SET url='https://koha.knihovnatabor.cz/cgi-bin/koha/oai.pl',set_spec='CPK',metadata_prefix='marccpk',extract_id_regex='TAG001:(.*)' WHERE import_conf_id=311;

-- 10. 08. 2020 tomascejpek
UPDATE oai_harvest_conf SET url='https://pisek.knihovny.net/l.dll' WHERE import_conf_id=381;

-- 10. 08. 2020 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (72, 398, 'KMG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (87, 411, 'NAG502');

-- 08. 09. 2020 tomascejpek
UPDATE import_conf SET item_id='other' WHERE id=314;
UPDATE import_conf SET item_id='dawinci' WHERE id in (301,369,403,404,405,406,407,408);
UPDATE import_conf SET item_id='koha' WHERE id in (306,340);

-- 01. 10. 2020 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (77, 402, 'PNG001');

-- 08. 10. 2020 tomascejpek
ALTER TABLE download_import_conf ADD COLUMN reharvest BOOLEAN DEFAULT FALSE;
UPDATE download_import_conf SET reharvest=TRUE WHERE import_conf_id IN (341,1305,1306,1307,1308,1309,1310,1311,1312,1314,1315,1318,1322,1326);

-- 26. 10. 2020 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (218, 'MKMT', 'https://www.mkmt.cz/', 'https://katalog.mkmt.cz/', 'Moravská Třebová', 'PA');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (418, 218, 200, 'mkmt', 11, false, true, true, true, 'U', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (418,'https://koha.mkmt.cz/cgi-bin/koha/oai.pl','CPK','marccpk',NULL,'SVG503:(.*)');

-- 26. 10. 2020 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (219, 'MKLIT', 'https://www.knihovna-litvinov.cz/', 'https://opac.knihovna-litvinov.cz/vufind/', 'Litvínov', 'US');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (419, 219, 200, 'mklit', 11, false, true, true, true, 'U', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (419,'https://opac.knihovna-litvinov.cz/cgi-bin/koha/oai.pl','CPK','marccpk',NULL,'MOG501:(.*)');

-- 27. 10. 2020 tomascejpek
UPDATE import_conf SET item_id='koha' WHERE id=311;

-- 01. 12. 2020 tomascejpek
CREATE TABLE fit_project (
  id                   DECIMAL(10) PRIMARY KEY,
  name                 VARCHAR(50) UNIQUE
);
CREATE TABLE fit_knowledge_base (
  id                   DECIMAL(10) PRIMARY KEY,
  data                 TEXT
);
CREATE TABLE fit_project_link (
  id                     SERIAL,
  harvested_record_id    DECIMAL(10),
  fit_project_id         DECIMAL(10),
  fit_knowledge_base_id  DECIMAL(10),
  data                   TEXT,
  CONSTRAINT fit_projects_pk PRIMARY KEY(id),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE,
  FOREIGN KEY (fit_project_id) REFERENCES fit_project(id) ON DELETE CASCADE,
  FOREIGN KEY (fit_knowledge_base_id) REFERENCES fit_knowledge_base(id) ON DELETE CASCADE
);
CREATE INDEX fit_projects_knowledge_base_idx ON fit_project_link(fit_knowledge_base_id);
CREATE INDEX fit_project_link_harvested_record_idx ON fit_project_link(harvested_record_id);
CREATE INDEX fit_project_link_idx ON fit_project_link(fit_project_id);
INSERT INTO fit_project VALUES (1,'FULLTEXT_ANALYSER');
INSERT INTO fit_project VALUES (2,'SEMANTIC_ENRICHMENT');
INSERT INTO fit_project VALUES (3,'CLASSIFIER');

-- 15. 12. 2020 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (89, 415, 'KTG001');

-- 15. 12. 2020 tomascejpek
INSERT INTO harvested_record_format(id, name) VALUES (67, 'EBOOK');

-- 15. 12. 2020 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (145,'MKPE','www.mlp.cz','search.mlp.cz',null,'ebook');
UPDATE import_conf SET library_id='145',is_library=FALSE WHERE id = 345;

-- 18. 12. 2020 tomascejpek
UPDATE oai_harvest_conf SET url='https://biblio.idu.cz/api/oai',set_spec='cpk' WHERE import_conf_id=365;

-- 05. 01. 2021 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (195, 'MENDELU', 'https://mendelu.cz/', 'https://katalog.mendelu.cz/', 'Brno', 'JM');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (395, 195, 200, 'mendelu', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (395,'https://katalog.mendelu.cz/api/oai/','5','marc21',NULL);

-- 05. 01. 2021 tomascejpek
UPDATE oai_harvest_conf SET url='https://katalog.vfu.cz/api/oai/',set_spec='cpk' WHERE import_conf_id=385;

-- 05. 01. 2021 tomascejpek
UPDATE oai_harvest_conf SET url='https://katalog.knir.cz/api/oai',set_spec='cpk' WHERE import_conf_id=387;

-- 05. 01. 2021 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (221, 'MKCHEB', 'https://www.knih-cheb.cz/', 'https://kpwin.knih-cheb.cz/', 'Cheb', 'KV');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (421, 221, 200, 'mkcheb', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (421,'https://kpwin.knih-cheb.cz/api/oai','cpk','marc21',NULL,'oai:(.*)');

-- 06. 01. 2021 tomascejpek
UPDATE oai_harvest_conf SET url='https://clavius.lib.cas.cz/katalog/l.dll',set_spec='CPK' WHERE import_conf_id=331;

-- 08. 01. 2021 tomascejpek
UPDATE oai_harvest_conf SET url='https://hodonin.tritius.cz/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=374;

-- 11. 01. 2021 tomascejpek
UPDATE import_conf SET interception_enabled='true' WHERE id=365;

-- 13. 01. 2020 tomascejpek
UPDATE oai_harvest_conf set extract_id_regex='s/[^:]+:[^:]+:[^\\/]+\\/([^\\/]+)/kjm_us_cat*$1/' WHERE import_conf_id=303;
UPDATE oai_harvest_conf set extract_id_regex='s/[^:]+:[^:]+:[^:]+:(.+)/li_us_cat*$1/' WHERE import_conf_id=308;
UPDATE oai_harvest_conf set extract_id_regex='s/[^:]+:[^:]+:[^\\/]+\\/([^\\/]+)/cbvk_us_cat*$1/' WHERE import_conf_id=328;
UPDATE oai_harvest_conf set extract_id_regex='s/[^:]+:[^:]+:[^\\/]+\\/([^\\/]+)/kl_us_cat*$1/' WHERE import_conf_id=336;
UPDATE oai_harvest_conf set extract_id_regex='s/[^:]+:[^:]+:[^\\/]+\\/([^\\/]+)/upol_us_cat*$1/' WHERE import_conf_id=359;
UPDATE oai_harvest_conf set extract_id_regex='s/[^:]+:[^:]+:[^:]+:(.+)/vy_us_cat*$1/' WHERE import_conf_id=392;

-- 14. 01. 2021 tomascejpek
DELETE FROM download_import_conf WHERE import_conf_id=332;
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (332,'https://katalog.knihovnakv.cz/tritius/oai-provider','CPK_1','marc21',NULL);
UPDATE import_conf SET item_id='tritius' WHERE id=332;

-- 15. 01. 2021 tomascejpek
UPDATE import_conf SET item_id='other' WHERE id=332;

-- 19. 01. 2021 tomascejpek
UPDATE kramerius_conf SET url='https://kramerius.mzk.cz/search/api/v5.0' WHERE import_conf_id=99001;
UPDATE kramerius_conf SET url='https://kramerius.kvkli.cz/search/api/v5.0' WHERE import_conf_id=99021;

-- 22. 01. 2021 tomascejpek
ALTER TABLE harvested_record ADD COLUMN loans DECIMAL(10);
ALTER TABLE harvested_record ADD COLUMN callnumber VARCHAR(100);

-- 28. 01. 2021 tomascejpek
UPDATE kramerius_conf SET availability_dest_url='https://ndk.cz/uuid/' where import_conf_id=99004;

-- 01. 02. 2021 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (223, 'MKRIC', 'https://knihovna.ricany.cz/', 'https://tritius-knihovna.ricany.cz/', 'Říčany', 'SC');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (423, 223, 200, 'mkric', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (423,'https://tritius-knihovna.ricany.cz/tritius/oai-provider','CPK_1','marc21',NULL);

-- 17. 02. 2021 tomascejpek
UPDATE kramerius_conf SET availability_dest_url='https://kramerius.lib.cas.cz/uuid/' WHERE import_conf_id=99003;
UPDATE kramerius_conf SET availability_dest_url='https://kramerius.svkhk.cz/uuid/' WHERE import_conf_id=99014;
UPDATE kramerius_conf SET availability_dest_url='https://kramerius.svkos.cz/uuid/' WHERE import_conf_id=99019;
UPDATE kramerius_conf SET availability_dest_url='https://kramerius.kvkli.cz/uuid/' WHERE import_conf_id=99021;
UPDATE kramerius_conf SET availability_dest_url='https://kramerius.mjh.cz/uuid/' WHERE import_conf_id=99034;

-- 18. 02. 2021 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (99047, 'KRAM-HMT', '', '', '','kramerius');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99047,99047,200,'kram-hmt',8,false,true,false,false,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99047,'http://kramerius.husitskemuzeum.cz/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'http://kramerius.husitskemuzeum.cz/search/handle/');

-- 18. 02. 2021 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (99048, 'KRAM-NULK', '', '', '','kramerius');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99048,99048,200,'kram-nulk',8,false,true,false,false,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99048,'https://kramerius.nulk.cz/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'https://kramerius.nulk.cz/uuid/');

-- 08. 03. 2021 tomascejpek
UPDATE oai_harvest_conf SET set_spec='CPKALL' WHERE import_conf_id=303;
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (68, 303, 'BOG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (90, 418, 'SVG503');

-- 09. 03. 2021 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (75, 401, 'KOG001');

-- 11. 03. 2021 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (225, 'MKBOSKOVICE', 'https://www.kulturaboskovice.cz/knihovna', 'https://boskovice.tritius.cz/', 'Boskovice', 'JM');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (425, 225, 200, 'mkboskovice', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (425,'https://boskovice.tritius.cz/tritius/oai-provider','CPK_1','marc21',NULL);

-- 11. 03. 2021 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (224, 'MKMILOVICE', 'http://milovice.knihovna.cz/', 'https://sck.tritius.cz/library/milovice', 'Milovice', 'SC');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (424, 224, 200, 'mkmilovice', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (424,'https://sck.tritius.cz/tritius/oai-provider','CPK_1','marc21',NULL);

-- 11. 03. 2021 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (217, 'MUNI', '', '', 'Brno', 'JM');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, ziskej_enabled) VALUES (417, 217, 200, 'muni', 11, false, true, true, true, 'U', 'other', true);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (417,NULL,NULL,'marc21',NULL);

-- 12. 03. 2021 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (226, 'KMOL', 'http://www.kmol.cz/', 'hhttps://tritius.kmol.cz/', 'Olomouc', 'OL');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (426, 226, 200, 'kmol', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (426,'https://tritius.kmol.cz/tritius/oai-provider','CPK_1','marc21',NULL);

-- 19. 03. 2021 tomascejpek
CREATE TABLE title_old_spelling (
  id                   SERIAL,
  key                  VARCHAR(128),
  value                VARCHAR(128)
);
CREATE INDEX title_old_spelling_key_idx ON title_old_spelling(key);

-- 25. 03. 2021 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (216, 'CZTCPK', '', '', null, 'bibliography');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (416, 216, 200, 'cztcpk', 11, false, true, true, false, 'U');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name, format) VALUES (416,null,null,null);

-- 12. 04. 2021 tomascejpek
UPDATE import_conf SET ziskej_enabled=FALSE WHERE id=417;

-- 15. 04. 2021 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (88, 412, 'KMG502');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (98, 425, 'BKG501');

-- 15. 04. 2021 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (228, 'SLAVOJ', 'http://slavoj.cz/', 'https://katalog.slavoj.cz/', 'Dvůr Králové nad Labem', 'KH');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (428, 228, 200, 'slavoj', 11, false, true, true, true, 'U', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (428,'https://koha.slavoj.cz/cgi-bin/koha/oai.pl','CPK','marccpk',NULL);

-- 16. 04. 2021 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (73, 399, 'CVG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (74, 368, 'FMG002');

-- 21. 04. 2021 tomascejpek
ALTER TABLE library ALTER COLUMN region TYPE VARCHAR(60);
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (167, 'CZHISTBIB', 'https://biblio.hiu.cas.cz/', 'https://biblio.hiu.cas.cz/search', 'Bibliography', 'bibliography/HISTOGRAFBIB');
UPDATE import_conf SET library_id=167 WHERE id=367;
UPDATE library SET name='ARCHBIB',region='bibliography/HISTOGRAFBIB' WHERE id=166;

-- 22. 04. 2021 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (229, 'MVK', 'https://www.mvk.cz/', 'https://katalog.mvk.cz/', 'Vsetín', 'ZL');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (429, 229, 200, 'mvk', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (429,'https://mvk.portaro.cz/api/oai','cpk','marc21',NULL);

-- 29. 04. 2021 tomascejpek
UPDATE library SET name='MKRICANY' WHERE id=223;
UPDATE import_conf SET id_prefix='mkricany' WHERE id=423;

-- 12. 05. 2021 tomascejpek
DELETE FROM oai_harvest_conf WHERE import_conf_id=1304;
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format) VALUES (1304,null,null,null);

-- 18. 05. 2021 tomascejpek
UPDATE kramerius_conf SET url='https://kramerius.upm.cz/search/api/v5.0',availability_dest_url='https://kramerius.upm.cz/uuid/' WHERE import_conf_id=99040;

-- 25. 05. 2021 tomascejpek
ALTER TABLE oai_harvest_conf ADD COLUMN url_full_harvest VARCHAR(128);
ALTER TABLE oai_harvest_conf ADD COLUMN set_spec_full_harvest VARCHAR(128);

-- 25. 05. 2021 tomascejpek
UPDATE oai_harvest_conf SET set_spec_full_harvest='CPK1' WHERE import_conf_id IN (328,336);

-- 03. 06. 2021 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (227, 'MKTREBIC', 'http://www.knihovnatr.cz/', 'https://trebic.tritius.cz/', 'Olomouc', 'VY');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (427, 227, 200, 'mktrebic', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (427,'https://trebic.tritius.cz/tritius/oai-provider','CPKTEST_1','marc21',NULL);

-- 10. 06. 2021 tomascejpek
UPDATE library SET city='Třebíč' WHERE id=227;

-- 11. 06. 2021 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (96, 423, 'ABG502');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (99, 426, 'OLG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (100, 427, 'TRG001');

-- 11. 06. 2021 tomascejpek
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script, generate_biblio_linker_keys) VALUES (1327,132,200,'sfxjibkkkv',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy', false);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex,reharvest) VALUES (1327,'https://sfx.knihovny.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-KKKV.xml','downloadAndImportRecordsJob','sfx',null,true);
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script, generate_biblio_linker_keys) VALUES (1328,191,200,'sfxjibuzei',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy', false);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex,reharvest) VALUES (1328,'https://sfx.knihovny.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-UZEI.xml','downloadAndImportRecordsJob','sfx',null,true);
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script, generate_biblio_linker_keys) VALUES (1329,114,200,'sfxjibsvkul',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy', false);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex,reharvest) VALUES (1329,'https://sfx.knihovny.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-SVKUL.xml','downloadAndImportRecordsJob','sfx',null,true);
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script, generate_biblio_linker_keys) VALUES (1330,112,200,'sfxjibkkvy',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy', false);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex,reharvest) VALUES (1330,'https://sfx.knihovny.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-KKVysociny.xml','downloadAndImportRecordsJob','sfx',null,true);

-- 14. 06. 2021 tomascejpek
UPDATE download_import_conf SET url='https://sfx.techlib.cz/sfxlcl41/cgi/public/get_file.cgi?file=institutional_holding-NTK.xml' WHERE import_conf_id=1318;

-- 28. 06. 2021 tomascejpek
UPDATE oai_harvest_conf SET url_full_harvest='https://roznov.portaro.cz/api/oai' WHERE import_conf_id=387;
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (61, 387, 'VSG502');

-- 30. 06. 2021 tomascejpek
CREATE TABLE kram_dnnt_label (
  id                    SERIAL,
  kram_availability_id INTEGER,
  label                 VARCHAR(100) NOT NULL,
  CONSTRAINT kram_dnnt_labels_pk PRIMARY KEY(id),
  FOREIGN KEY (kram_availability_id) REFERENCES kram_availability(id) ON DELETE CASCADE
);
CREATE INDEX kram_dnnt_label_availability_id_idx ON kram_dnnt_label(kram_availability_id);

-- 01. 07. 2021 tomascejpek
UPDATE kramerius_conf SET availability_dest_url='https://www.digitalniknihovna.cz/mzk/uuid/' WHERE import_conf_id=99001;
UPDATE kramerius_conf SET availability_dest_url='https://dk.uzei.cz/uzei/uuid/' WHERE import_conf_id=99017;
UPDATE kramerius_conf SET availability_source_url='https://kramerius.kr-olomoucky.cz/search/api/v5.0' WHERE import_conf_id=99012;

-- 22. 07. 2021 tomascejpek
UPDATE kramerius_conf SET url='https://cdk.lib.cas.cz/search/api/v5.0' WHERE import_conf_id IN (99019,99037);
UPDATE kramerius_conf SET url_solr='https://cdk.lib.cas.cz/solr-select-only/k4' WHERE import_conf_id IN (99019,99037);
UPDATE kramerius_conf SET collection='"vc:41f345fc-d0ad-11ea-b976-005056b593cd"' WHERE import_conf_id=99019;
UPDATE kramerius_conf SET collection='"vc:9ecedcad-aa68-4967-8d65-f938c5ce3a6b"' WHERE import_conf_id=99037;
UPDATE kramerius_conf SET availability_source_url='https://kramerius.svkos.cz/search/api/v5.0' WHERE import_conf_id=99019;
UPDATE kramerius_conf SET availability_source_url='https://library.nfa.cz/search/api/v5.0' WHERE import_conf_id=99037;

-- 02. 08. 2021 tomascejpek
ALTER TABLE kramerius_conf ADD COLUMN fulltext_harvest_frequency CHAR(1) DEFAULT 'U';
CREATE OR REPLACE VIEW fulltext_job_stat AS
SELECT
    bje.job_execution_id,
    (array_agg(conf_id_param.long_val))[1] import_conf_id,
    bje.start_time,
    bje.end_time,
    bje.status,
    to_param.date_val to_param
FROM batch_job_instance bji
         JOIN batch_job_execution bje ON bje.job_instance_id = bji.job_instance_id
         JOIN batch_job_execution_params conf_id_param ON conf_id_param.job_execution_id = bje.job_execution_id AND conf_id_param.key_name = 'configurationId'
         LEFT JOIN batch_job_execution_params to_param ON to_param.job_execution_id = bje.job_execution_id AND to_param.key_name = 'to'
         JOIN kramerius_conf kc ON kc.import_conf_id = conf_id_param.long_val
WHERE bji.job_name IN ('krameriusFulltextJob')
GROUP BY bje.job_execution_id,to_param.date_val
;
CREATE OR REPLACE VIEW fulltext_summary AS
WITH last_harvest_date AS (
    SELECT
        import_conf_id,
        COALESCE(MAX(CASE WHEN status = 'COMPLETED' THEN to_param END), MAX(CASE WHEN status = 'COMPLETED' THEN end_time END)) last_successful_harvest_date,
        COALESCE(MAX(CASE WHEN status = 'FAILED' THEN to_param END), MAX(CASE WHEN status = 'FAILED' THEN end_time END)) last_failed_harvest_date
    FROM fulltext_job_stat
    GROUP BY import_conf_id
)
SELECT ic.id,
       l.name,
       ic.id_prefix,
       CASE WHEN kc.url_solr IS NOT NULL THEN kc.url_solr ELSE kc.url END url,
       lhd.last_successful_harvest_date,
       lhd.last_failed_harvest_date
FROM last_harvest_date lhd
         JOIN import_conf ic ON ic.id = lhd.import_conf_id
         LEFT JOIN kramerius_conf kc ON kc.import_conf_id = ic.id
         JOIN library l ON l.id = ic.library_id
ORDER BY lhd.last_successful_harvest_date DESC NULLS LAST
;

-- 09 .08. 2021 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (230, 'MKCHRUDIM', 'https://www.knihovna-cr.cz/', 'https://katalog.knihovna-cr.cz/', 'Chrudim', 'PA');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (430, 230, 200, 'mkchrudim', 11, false, true, true, true, 'U', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (430,'https://koha.knihovna-cr.cz/cgi-bin/koha/oai.pl','CPK','marccpk',NULL,'CRG001:(.*)');
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (231, 'MKBOHUMIN', 'https://www.k3bohumin.cz/', 'https://katalog.k3bohumin.cz/', 'Bohumin', 'MS');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (431, 231, 200, 'mkbohumin', 11, false, true, true, true, 'U', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (431,'https://koha.k3bohumin.cz/cgi-bin/koha/oai.pl','CPK','marccpk',NULL,'KAG505:(.*)');
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (232, 'MKJH', 'https://www.knihjh.cz/', 'https://jh.tritius.cz/', 'Jindřichův Hradec', 'VY');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (432, 232, 200, 'mkjh', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (432,'https://jh.tritius.cz/tritius/oai-provider','CPK_1','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (233, 'MKHAVIROV', 'https://knihovnahavirov.cz/', 'https://katalog.knih-havirov.cz/', 'Havířov', 'MS');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (433, 233, 200, 'mkhavirov', 11, false, true, true, true, 'U', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (433,'https://koha.knih-havirov.cz/cgi-bin/koha/oai.pl','CPK','marccpk',NULL,'KAG503:(.*)');
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (234, 'MKCHOCEN', 'https://www.knihovnachocen.cz/', 'https://chocen-katalog.koha.cloud/', 'Choceň', 'PA');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (434, 234, 200, 'mkchocen', 11, false, true, true, true, 'U', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (434,'https://chocen-koha-katalog.koha.cloud/cgi-bin/koha/oai.pl','CPK','marccpk',NULL,'UOG502:(.*)');
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (235, 'GEOL', 'http://www.geology.cz/', '', 'Bibliography', 'bibliography');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (435, 235, 200, 'geol', 11, false, true, true, false, 'U', null);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (435,'http://oai.geology.cz:8080/katalog/l.dll','GEOL','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (236, 'ENVI', 'http://www.geology.cz/', '', 'Bibliography', 'bibliography');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (436, 236, 200, 'envi', 11, false, true, true, false, 'U', null);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (436,'http://oai.geology.cz:8080/katalog/l.dll','ENVI','marc21',NULL);
UPDATE oai_harvest_conf SET extract_id_regex='TUG504:(.*)' WHERE import_conf_id=428;

-- 23 .08. 2021 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (237, 'MKDB', 'https://www.knihovna.dolnibousov.cz/', 'https://katalog.dolni-bousov.cz/', 'Dolní Bousov', 'SC');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (437, 237, 200, 'mkdb', 11, false, true, true, true, 'U', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (437,'https://koha.dolni-bousov.cz/cgi-bin/koha/oai.pl','CPK','marccpk',NULL,'MBG504:(.*)');

-- 23 .08. 2021 tomascejpek
UPDATE oai_harvest_conf SET url='https://koha.knihovna-litvinov.cz/cgi-bin/koha/oai.pl' WHERE import_conf_id=419;

-- 23 .08. 2021 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (93, 419, 'MOG501');

-- 23. 08. 2021 tomascejpek
INSERT INTO harvested_record_format(id, name) VALUES (69, 'THESIS_BACHELOR');
INSERT INTO harvested_record_format(id, name) VALUES (70, 'THESIS_MASTER');
INSERT INTO harvested_record_format(id, name) VALUES (71, 'THESIS_ADVANCED_MASTER');
INSERT INTO harvested_record_format(id, name) VALUES (72, 'THESIS_DISSERTATION');
INSERT INTO harvested_record_format(id, name) VALUES (73, 'THESIS_HABILITATION');
INSERT INTO harvested_record_format(id, name) VALUES (74, 'THESIS_OTHER');

-- 23. 08. 2021 tomascejpek
INSERT INTO harvested_record_format(id, name) VALUES (68, 'BOARD_GAMES');

-- 24. 08. 2020 tomascejpek
ALTER TABLE import_conf ADD COLUMN mappings996 VARCHAR(20);
UPDATE import_conf SET mappings996='aleph' WHERE id IN (300,304,307,313,315,321,324,325,326,330,333,335,337,361,391);
UPDATE import_conf SET mappings996='tritius' WHERE id IN (312,314,332,334,346,350,353,356,364,368,370,371,373,374,375,376,377,378,380,381,383,384,386,388,390,397,398,399,401,402,409,410,411,412,415,423,424,425,426,427,432);
UPDATE import_conf SET mappings996='koha' WHERE id IN (306,311,340,418,419,428,430,431,433,434,437);
UPDATE import_conf SET mappings996='caslin' WHERE id IN (316);
UPDATE import_conf SET mappings996='dawinci' WHERE id IN (301,342,369,393,403,404,405,406,407,408);

-- 24. 08. 2021 tomascejpek
CREATE TABLE caslin_links (
  id                SERIAL,
  sigla             VARCHAR(10) NOT NULL,
  url               VARCHAR (500) NOT NULL,
  updated           TIMESTAMP NOT NULL,
  last_harvest      TIMESTAMP NOT NULL,
  CONSTRAINT caslin_links_pk PRIMARY KEY(id)
);
CREATE INDEX caslin_links_sigla_idx ON caslin_links(sigla);

-- 16. 09. 2021 tomascejpek
UPDATE kramerius_conf SET url='https://kramerius.knihovna-pardubice.cz/search/api/v5.0',availability_dest_url='https://kramerius.knihovna-pardubice.cz/uuid/' WHERE import_conf_id=99026;

-- 17. 09. 2021 tomascejpek
UPDATE oai_harvest_conf SET url='https://knihovnaml.tritius.cz/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=386;

-- 23. 09. 2021 tomascejpek
DROP TABLE biblio_linker_similar;
CREATE TABLE biblio_linker_similar (
  id                   SERIAL,
  harvested_record_id  DECIMAL(10),
  harvested_record_similar_id DECIMAL(10),
  url_id               TEXT,
  type                 VARCHAR(20),
  CONSTRAINT biblio_linker_similar_pk PRIMARY KEY(id),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);
CREATE INDEX bls_harvested_record_id_idx ON biblio_linker_similar(harvested_record_id);
CREATE INDEX bls_harvested_record_similar_id_idx ON biblio_linker_similar(harvested_record_similar_id);

-- 29. 09. 2021 tomascejpek
CREATE TABLE import_conf_mapping_field (
  import_conf_id         DECIMAL(10) PRIMARY KEY,
  parent_import_conf_id  DECIMAL(10) NOT NULL,
  mapping                VARCHAR(100),
  CONSTRAINT import_conf_mapping_field_import_conf_fk        FOREIGN KEY (import_conf_id) REFERENCES import_conf(id),
  CONSTRAINT import_conf_mapping_field_parent_import_conf_fk FOREIGN KEY (parent_import_conf_id) REFERENCES import_conf(id)
);
CREATE INDEX import_conf_mapping_field_parent_id_idx ON import_conf_mapping_field(parent_import_conf_id);
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (220, 'USDBIBL', '', '', null, 'bibliography');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (420, 220, 200, 'usdbibl', 11, false, true, true, false, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (420,NULL,NULL,'marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (222, 'KNAVALL', '', '', null, null);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, generate_dedup_keys, generate_biblio_linker_keys, indexed) VALUES (422, 222, 200, 'knavall', 11, false, true, true, true, 'U', false, false, false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (422,'https://aleph.lib.cas.cz/OAI','KNA01','marc21',NULL);

-- 12. 10. 2021 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (238, 'KAND', 'https://www.narodni-divadlo.cz/', 'https://www.archivndknihovna.cz/', 'Praha', 'PR');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (438, 238, 200, 'kand', 11, false, true, true, true, 'U', 'koha', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (438,'https://koha.archivndknihovna.cz/cgi-bin/koha/oai.pl','CPK','marccpk',NULL,'ABE309:(.*)');

-- 13. 10. 2021 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (239, 'MKVALMEZ', 'https://www.mekvalmez.cz/', 'https://katalog.mekvalmez.cz/', 'Valašské Meziříčí', 'ZL');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (439, 239, 200, 'mkvalmez', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (439,'https://katalog.mekvalmez.cz/api/oai','cpk','marc21',NULL,'oai:(.*)');

-- 18. 10. 2021 tomascejpek
UPDATE import_conf SET item_id='aleph',interception_enabled=true WHERE id=361;
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (69, 361, 'BOB026');
INSERT INTO import_conf_mapping_field (import_conf_id,parent_import_conf_id,mapping) VALUES (361,422,'599$aCPK-UVGZ');

-- 01. 10. 2021 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (104, 431, 'KAG505');

-- 25. 10. 2021 tomascejpek
UPDATE kramerius_conf SET url='https://cdk.lib.cas.cz/search/api/v5.0',url_solr='https://cdk.lib.cas.cz/solr-select-only/k4',collection='"vc:5af0d476-df3d-4709-8f28-5c33d9d3f4b5"',availability_source_url='http://kramerius.kfbz.cz/search/api/v5.0' WHERE import_conf_id=99023;

-- 25. 10. 2021 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (240, 'QUEER', 'https://www.stud.cz/informace/queer-knihovna.html', 'https://katalog.queerknihovna.cz/', 'Brno', 'JM');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (440, 240, 200, 'queer', 11, false, true, true, true, 'U', 'koha', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (440,'https://koha.queerknihovna.cz/cgi-bin/koha/oai.pl','CPK','marccpk',NULL,'BOE035:(.*)');

-- 26. 10. 2021 tomascejpek
UPDATE oai_harvest_conf SET url='https://tritius.knih-pe.cz/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=380;

-- 04. 11. 2021 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (92, 421, 'CHG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (103, 430, 'CRG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (112, 380, 'PEG001');

-- 05. 11. 2021 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (105, 432, 'JHG001');

-- 08. 11. 2021 tomascejpek
DELETE FROM sigla WHERE id IN (49,52);

-- 08. 11. 2021 tomascejpek
DROP TABLE inspiration;
CREATE TABLE inspiration (
  id                    SERIAL,
  harvested_record_id   DECIMAL(10),
  name                  VARCHAR(128),
  CONSTRAINT inspiration_pk PRIMARY KEY(id),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);
CREATE INDEX inspiration_harvested_record_idx ON inspiration(harvested_record_id);
CREATE INDEX inspiration_name_idx ON inspiration(name);

-- 11. 11. 2021 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (241, 'MKJICIN', 'https://knihovna.jicin.cz/', 'https://katalog.knihovna.jicin.cz/', 'Jičín', 'KH');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (441, 241, 200, 'mkjicin', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (441,'https://katalog.knihovna.jicin.cz/api/oai','cpk','marc21',NULL);

-- 07. 12. 2021 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (242, 'MKFPR', 'https://www.knihovnafrenstat.cz/', 'https://katalog.knihovnafrenstat.cz/', 'Frenštát pod Radhoštěm', 'MS');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (442, 242, 200, 'mkfpr', 11, false, true, true, true, 'U', 'koha', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (442,'https://koha.knihovnafrenstat.cz/cgi-bin/koha/oai.pl','CPK','marccpk',NULL,'NJG502:(.*)');

-- 07. 12. 2021 tomascejpek
UPDATE library SET catalog_url='https://katalog.knihovnaberoun.cz/' WHERE id=189;
UPDATE import_conf SET item_id='koha',mappings996='koha' WHERE id=389;
UPDATE oai_harvest_conf SET url='https://koha.knihovnaberoun.cz/cgi-bin/koha/oai.pl',set_spec='CPK',metadata_prefix='marccpk',extract_id_regex='BEG001:(.*)' WHERE import_conf_id=389;

-- 08. 12. 2021 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (243, 'MKKNO', 'https://biblio.cz/', 'https://katalog.biblio.cz/', 'Kostelec nad Orlicí', 'KH');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (443, 243, 200, 'mkkno', 11, false, true, true, true, 'U', 'koha', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (443,'https://koha-katalog.biblio.cz/cgi-bin/koha/oai.pl','CPK','marccpk',NULL,'RKG503:(.*)');

-- 09. 12. 2021 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (244, 'AMBIS', 'https://www.ambis.cz/', 'https://ambis.tritius.cz/', 'Praha', 'PR');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (444, 244, 200, 'ambis', 11, false, true, true, true, 'U', 'other', 'tritius');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (444,'https://ambis.tritius.cz/tritius/oai-provider','CPK_1','marc21',NULL);

-- 03. 01. 2022 tomascejpek
UPDATE import_conf SET mappings996='tritius' WHERE id=379;
UPDATE oai_harvest_conf SET url='https://orlova.knihovny.net/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=379;

-- 04. 01. 2022 tomascejpek
UPDATE oai_harvest_conf SET url='https://baze.knihovnazn.cz/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=384;

-- 04. 01. 2022 tomascejpek
UPDATE kramerius_conf SET url='https://kramerius.kkvysociny.cz/search/api/v5.0',availability_dest_url='https://kramerius.kkvysociny.cz/uuid/' WHERE import_conf_id=99025;

-- 10. 01. 2022 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (108, 437, 'MBG504');

-- 10. 01. 2022 tomascejpek
INSERT INTO import_conf_mapping_field (import_conf_id,parent_import_conf_id,mapping) VALUES (349,422,'599$aCPK-UDUBIBL');
INSERT INTO import_conf_mapping_field (import_conf_id,parent_import_conf_id,mapping) VALUES (420,422,'599$aCPK-USDBIBL');
UPDATE import_conf SET interception_enabled=TRUE WHERE id=349;

-- 28. 01. 2022 tomascejpek
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, generate_dedup_keys) VALUES (445, 114, 200, 'bookport', 11, false, true, true, true, 'U', false);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format) VALUES (445,null,null,null);

-- 02. 02. 2022 tomascejpek
UPDATE oai_harvest_conf SET url='https://milovice.tritius.cz/tritius/oai-provider' WHERE import_conf_id=424;
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (97, 424, 'NBG505');

-- 04. 02. 2022 tomascejpek
UPDATE download_import_conf SET url='https://sfx.knihovny.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-NLK.xml',import_job_name='downloadAndImportRecordsJob',format='sfx',reharvest=true WHERE import_conf_id=1316;
UPDATE import_conf SET filtering_enabled=FALSE WHERE id=1316;

-- 09. 02. 2022 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (53, 379, 'KAG502');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (101, 428, 'TUG504');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (107, 434, 'UOG502');

-- 11. 02. 2022 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (246, 'MKNBK', 'http://knihovna-nbk.cz/', 'https://tritius.knihovna-nbk.cz/', 'Nymburk', 'SC');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (446, 246, 200, 'mknbk', 11, false, true, true, true, 'U', 'other', 'tritius');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (446,'https://tritius.knihovna-nbk.cz/tritius/oai-provider','CPK_1','marc21',NULL);

-- 01. 03. 2022 tomascejpek
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script, generate_biblio_linker_keys) VALUES (1331,136,200,'sfxjibsvkkl',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy', false);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex,reharvest) VALUES (1331,'https://sfx.knihovny.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-SVKKL.xml','downloadAndImportRecordsJob','sfx',null,true);

-- 03. 03. 2022 tomascejpek
DELETE FROM oai_harvest_conf WHERE import_conf_id=99004;
UPDATE kramerius_conf SET metadata_stream='BIBLIO_MODS' WHERE import_conf_id=99004;

-- 04. 03. 2022 tomascejpek
UPDATE kramerius_conf SET availability_source_url='https://kramerius.svkhk.cz/search/api/v5.0' WHERE import_conf_id=99014;

-- 07. 03. 2022 tomascejpek
UPDATE oai_harvest_conf SET extract_id_regex=NULL WHERE import_conf_id IN (421,439);

-- 08. 03. 2022 tomascejpek
INSERT INTO import_conf_mapping_field (import_conf_id,parent_import_conf_id,mapping) VALUES (360,422,'599$aCLB-CPK');

-- 16. 03. 2022 tomascejpek
UPDATE oai_harvest_conf set extract_id_regex='s/[^:]+:[^:]+:[^\\/]+\\/([^\\/]+)/KjmUsCat*$1/' WHERE import_conf_id=303;
UPDATE oai_harvest_conf set extract_id_regex='s/[^:]+:[^:]+:[^:]+:(.+)/LiUsCat*$1/' WHERE import_conf_id=308;
UPDATE oai_harvest_conf set extract_id_regex='s/[^:]+:[^:]+:[^\\/]+\\/([^\\/]+)/CbvkUsCat*$1/' WHERE import_conf_id=328;
UPDATE oai_harvest_conf set extract_id_regex='s/[^:]+:[^:]+:[^\\/]+\\/([^\\/]+)/KlUsCat*$1/' WHERE import_conf_id=336;
UPDATE oai_harvest_conf set extract_id_regex='s/[^:]+:[^:]+:[^\\/]+\\/([^\\/]+)/UpolUsCat*$1/' WHERE import_conf_id=359;
UPDATE oai_harvest_conf set extract_id_regex='s/[^:]+:[^:]+:[^:]+:(.+)/VyUsCat*$1/' WHERE import_conf_id=392;

-- 18. 03. 2022 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (247, 'MKHORICE', 'https://knihovna.horice.org/', 'https://kpwin.horice.org/', 'Hořice', 'KH');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (447, 247, 200, 'mkhorice', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (447,'https://kpwin.horice.org/api/oai/','cpk','marc21',NULL);

-- 21. 03. 2022 tomascejpek
UPDATE oai_harvest_conf SET url='https://katalog.mvk.cz/api/oai' WHERE import_conf_id=429;

-- 30. 03. 2022 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (106, 433, 'KAG503');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (109, 438, 'ABE309');

-- 04. 04. 2022 tomascejpek
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, generate_dedup_keys) VALUES (448, 130, 200, 'bookport', 11, false, true, true, true, 'U', false);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format) VALUES (448,'https://bookport.cz/marc21-12415.xml','downloadAndImportRecordsJob','xml');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, generate_dedup_keys) VALUES (449, 101, 200, 'bookport', 11, false, true, true, true, 'U', false);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format) VALUES (449,'https://bookport.cz/marc21-12416.xml','downloadAndImportRecordsJob','xml');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, generate_dedup_keys) VALUES (450, 113, 200, 'bookport', 11, false, true, true, true, 'U', false);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format) VALUES (450,'https://bookport.cz/marc21-12427.xml','downloadAndImportRecordsJob','xml');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, generate_dedup_keys) VALUES (451, 115, 200, 'bookport', 11, false, true, true, true, 'U', false);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format) VALUES (451,'https://bookport.cz/marc21-12417.xml','downloadAndImportRecordsJob','xml');

-- 08. 04. 2022 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (252, 'MKSTRAK', 'https://www.knih-st.cz/', 'https://katalog.knih-st.cz/', 'Strakonice', 'JC');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (452, 252, 200, 'mkstrak', 11, false, true, true, true, 'U', 'koha', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (452,'https://koha.knih-st.cz/cgi-bin/koha/oai.pl','CPK','marccpk',NULL);

-- 13. 04. 2022 tomascejpek
UPDATE import_conf set id_prefix='mkcaslav',mappings996='tritius' where id=372;
UPDATE oai_harvest_conf SET url='https://online.knihovnacaslav.cz/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=372;
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (453, 172, 200, 'cmuz', 11, false, true, true, true, 'U', 'other', 'tritius');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (453,'https://online.knihovnacaslav.cz/tritius/oai-provider','CPK_101','marc21',NULL);

-- 13. 04. 2022 tomascejpek
DELETE FROM oai_harvest_conf WHERE import_conf_id=453;
DELETE FROM import_conf WHERE id=453;
UPDATE library SET name='MKCASLAV',catalog_url='https://online.knihovnacaslav.cz/' WHERE id=172;

-- 19. 04. 2022 tomascejpek
UPDATE library SET region='JC' WHERE id=232;

-- 19. 04. 2022 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (111, 440, 'BOE035');

-- 25. 04. 2022 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (253, 'GEOBIBLINE', 'https://cuni.cz/', 'https://cuni.primo.exlibrisgroup.com/discovery/search?vid=420CKIS_INST:UKAZ&lang=cs', 'Bibliography', 'bibliography');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (453, 253, 200, 'geobibline', 11, false, true, true, false, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (453,'https://cuni.alma.exlibrisgroup.com/view/oai/420CKIS_INST/request','OAI_GEO','marc21',NULL);

-- 11. 05. 2022 tomascejpek
UPDATE kramerius_conf SET url='https://k4.kr-karlovarsky.cz/search/api/v5.0' WHERE import_conf_id=99020;

-- 17. 05. 2022 tomascejpek
UPDATE oai_harvest_conf SET extract_id_regex='STG001:(.*)' WHERE import_conf_id=452;

-- 19. 05. 2022 tomascejpek
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, generate_dedup_keys) VALUES (454, 102, 200, 'bookport', 11, false, true, true, true, 'U', false);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format) VALUES (454,'https://bookport.cz/marc21-8077.xml','downloadAndImportRecordsJob','xml');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, generate_dedup_keys) VALUES (455, 102, 200, 'bookport', 11, false, true, true, true, 'U', false);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format) VALUES (455,'https://bookport.cz/marc21-9561.xml','downloadAndImportRecordsJob','xml');

-- 27. 05. 2022 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (110, 439, 'VSG501');

-- 09. 06. 2022 tomascejpek
UPDATE library SET name='CLP' WHERE id=216;
UPDATE import_conf SET id_prefix='clp' WHERE id=416;

-- 09. 06. 2022 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (258, 'RSL', '', '', null, 'bibliography');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (458, 258, 200, 'rsl', 11, false, true, true, false, 'U');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format) VALUES (458,null,null,null);

-- 15. 06. 2022 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (257, 'PALMKNIHY', 'https://www.palmknihy.cz/', '', null, 'ebook');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (457, 257, 200, 'palmknihy', 11, false, true, true, false, 'U');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format) VALUES (457,null,null,null);
ALTER TABLE harvested_record ADD COLUMN palmknihy_id VARCHAR(20);
CREATE INDEX harvested_record_palmknihy_id_idx ON harvested_record(palmknihy_id);

-- 16. 06. 2022 tomascejpek
UPDATE import_conf SET id_prefix='mkcaslav' WHERE id=372;
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (46, 372, 'KHG505');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (102, 429, 'VSG001');

-- 17. 06. 2022 tomascejpek
CREATE TABLE sigla_all (
  id                SERIAL,
  sigla             VARCHAR(10) NOT NULL,
  import_conf_id    DECIMAL(10),
  cpk               BOOLEAN DEFAULT FALSE,
  ziskej            BOOLEAN DEFAULT FALSE,
  dnnt              BOOLEAN DEFAULT FALSE,
  CONSTRAINT sigla_all_pk PRIMARY KEY(id),
  FOREIGN KEY (import_conf_id) REFERENCES import_conf(id) ON DELETE SET NULL
);
CREATE INDEX sigla_all_sigla_idx ON sigla_all(sigla);
CREATE INDEX sigla_all_import_conf_id_idx ON sigla_all(import_conf_id);
CREATE INDEX sigla_all_cpk_idx ON sigla_all(cpk);
CREATE INDEX sigla_all_dnnt_idx ON sigla_all(dnnt);
CREATE INDEX sigla_all_ziskej_idx ON sigla_all(ziskej);

-- 17. 06. 2022 tomascejpek
UPDATE import_conf SET generate_dedup_keys=false,generate_biblio_linker_keys=false WHERE id=457;

-- 30. 06. 2022 tomascejpek
UPDATE download_import_conf SET url='http://ereading.cz/xml/xml_rent.xml',import_job_name='importPalmknihyJob',format='palmknihy',reharvest=true WHERE import_conf_id=457;

-- 15. 07. 2022 tomascejpek
UPDATE oai_harvest_conf SET url='https://nacr.kpsys.cz/api/oai' WHERE import_conf_id=367;

-- 21. 07. 2022 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (259, 'MKJIHLAVA', 'https://jihlava.tritius.cz/tritius/oai-provider', 'https://jihlava.tritius.cz/', 'Jihlava', 'VY');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (459, 259, 200, 'mkjihlava', 11, false, true, true, true, 'U', 'other', 'tritius');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (459,'https://jihlava.tritius.cz/tritius/oai-provider','CPK_1','marc21',NULL);

-- 29. 07. 2022 tomascejpek
UPDATE kramerius_conf SET dnnt_dest_url='https://kramerius.svkhk.cz/uuid/' WHERE import_conf_id=99014;

-- 01. 08. 2022 tomascejpek
UPDATE kramerius_conf SET url='https://kramerius.mjh.cz/search/api/v5.0' WHERE import_conf_id=99034;
UPDATE kramerius_conf SET availability_dest_url='https://k4.kr-karlovarsky.cz/search/handle/' WHERE import_conf_id=99020;
UPDATE kramerius_conf SET availability_dest_url='https://kramerius.techlib.cz/kramerius-web-client/uuid/' WHERE import_conf_id=99016;

-- 08. 08. 2022 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (116, 446, 'NBG001');

-- 11. 08. 2022 tomascejpek
ALTER TABLE oai_harvest_conf ADD COLUMN ictx VARCHAR(128);
ALTER TABLE oai_harvest_conf ADD COLUMN op VARCHAR(128);

-- 11. 08. 2022 tomascejpek
UPDATE import_conf SET item_id='other',interception_enabled=true WHERE id=359;
UPDATE oai_harvest_conf SET url='https://library.upol.cz/i2/i2.entry.cls',set_spec='UPOLCPK',extract_id_regex='s/[^:]+:[^:]+:[^:]+:(.+)/UpolUsCat*$1/',ictx='upol',op='oai' WHERE import_conf_id=359;

-- 17. 08. 2022 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (95, 359, 'OLD012');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (120, 459, 'JIG001');

-- 22. 08. 2022 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (260, 'NACR', 'https://www.nacr.cz/', 'https://knihovna.nacr.cz/', 'Praha', 'PR');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (460, 260, 200, 'nacr', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (460,'https://knihovna.nacr.cz/api/oai','cpk','marc21',NULL);

-- 22. 08. 2022 tomascejpek
ALTER TABLE import_conf ADD COLUMN catalog_serial_link BOOLEAN DEFAULT FALSE;

-- 02. 09. 2022 tomascejpek
UPDATE oai_harvest_conf SET url='https://nacr.kpsys.cz/api/oai' WHERE import_conf_id=366;

-- 05. 09. 2022 tomascejpek
UPDATE import_conf SET catalog_serial_link=true WHERE id in (308,328,336,343,359,387,392,421,429,439,441,447,460);

-- 08. 09. 2022 tomascejpek
UPDATE oai_harvest_conf SET url='https://biblio.hiu.cas.cz/api/oai' WHERE import_conf_id=367;

-- 08. 09. 2022 tomascejpek
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, generate_dedup_keys, generate_biblio_linker_keys, indexed) VALUES (461, 114, 200, 'svkul', 11, false, true, true, true, 'U', false, false, false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (461,'https://tritius.svkul.cz/tritius/oai-provider','PLM','marc21',NULL);

-- 14. 09. 2022 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (113, 441, 'JCG001');

-- 19. 09. 2022 tomascejpek
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, generate_dedup_keys, generate_biblio_linker_keys, indexed) VALUES (462, 104, 200, 'nkc-ebook', 11, false, true, true, true, 'U', true, false, false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (462,'https://aleph.nkp.cz/OAI','NKC-EBOOK','marc21',NULL);

-- 06. 10. 2022 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (256, 'UDUMUKN', 'https://www.udu.cas.cz/cz/knihovny/muzikologicka-knihovna', 'https://aleph.lib.cas.cz/', 'Praha', 'PR');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (456, 256, 200, 'udumukn', 11, false, true, true, true, 'U');
INSERT INTO import_conf_mapping_field (import_conf_id,parent_import_conf_id,mapping) VALUES (456,422,'599$aCPK-UDUMUKN');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (119, 456, 'ABB045');

-- 10. 10. 2022 tomascejpek
UPDATE import_conf SET item_id='aleph',mappings996='aleph' WHERE id=456;

-- 12. 10. 2022 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (263, 'MKBEN', 'https://www.knihovna-benesov.cz/', 'https://benesov.tritius.cz/', 'Benešov', 'SC');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (463, 263, 200, 'mkber', 11, false, true, true, true, 'U', 'other', 'tritius');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (463,'https://benesov.tritius.cz/tritius/oai-provider','CPK_1','marc21',NULL);

-- 17. 10. 2022 tomascejpek
UPDATE import_conf SET id_prefix='mkben' WHERE id=463;

-- 26. 10. 2022 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (123, 375, 'OPG503');

-- 04. 11. 2022 tomascejpek
ALTER TABLE caslin_links ADD hardcoded_url VARCHAR (100);

-- 15. 11. 2022 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (287, 'CNS', 'https://www.narodopisnaspolecnost.cz/', 'https://cns.tritius.cz/', 'Bibliography', 'bibliography');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (487, 287, 200, 'cns', 11, false, true, true, false, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (487,'https://cns.tritius.cz/tritius/oai-provider','CPK_1','marc21',NULL);

-- 15. 11. 2022 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (288, 'MKTURNOV', 'https://knihovna.turnov.cz/', 'https://turnov-katalog.koha-system.cz/', 'Turnov', 'LI');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (488, 288, 200, 'mkturnov', 11, false, true, true, true, 'U', 'koha', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (488,'https://turnov-opac.koha-system.cz/cgi-bin/koha/oai.pl','cpk','cpk',NULL);

-- 15. 11. 2022 tomascejpek
INSERT INTO format(format, description) VALUES('cpk', 'MARC21 XML');

-- 06. 12. 2022 tomascejpek
DROP TABLE inspiration;
CREATE TABLE inspiration (
  id                    SERIAL,
  name                  VARCHAR(128),
  type                  VARCHAR(128),
  CONSTRAINT inspiration_pk PRIMARY KEY(id)
);
CREATE TABLE harvested_record_inspiration (
  id                    SERIAL,
  harvested_record_id   DECIMAL(10),
  inspiration_id        INTEGER,
  updated               TIMESTAMP,
  last_harvest          TIMESTAMP,
  CONSTRAINT harvested_record_inspiration_pk PRIMARY KEY(id),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE,
  FOREIGN KEY (inspiration_id) REFERENCES inspiration(id) ON DELETE CASCADE
);
CREATE INDEX inspiration_name_idx ON inspiration(name);
CREATE INDEX inspiration_type_idx ON inspiration(type);
CREATE INDEX harvested_record_inspiration_id_idx ON harvested_record_inspiration(harvested_record_id);
CREATE INDEX harvested_record_inspiration_inspiration_id_idx ON harvested_record_inspiration(inspiration_id);
CREATE INDEX harvested_record_inspiration_updated_idx ON harvested_record_inspiration(updated);
CREATE INDEX harvested_record_inspiration_last_harvest_idx ON harvested_record_inspiration(last_harvest);

-- 06. 12. 2022 tomascejpek
UPDATE oai_harvest_conf SET extract_id_regex=null WHERE import_conf_id=363;

-- 07. 12. 2022 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (94, 363, 'ABE356');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (121, 460, 'ABE343');

-- 12. 12. 2022 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (148, 363, 'ABA012');

-- 02. 01. 2023 tomascejpek
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, generate_dedup_keys) VALUES (489, 100, 200, 'bookport', 11, false, true, true, true, 'U', false);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format) VALUES (489,'https://bookport.cz/marc21-15554.xml','downloadAndImportRecordsJob','xml');

-- 05. 01. 2023 tomascejpek
UPDATE oai_harvest_conf SET extract_id_regex='SMG506:(.*)' WHERE import_conf_id=488;
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (147, 488, 'SMG506');

-- 13. 01. 2023 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (290, 'MKNERATOVICE', 'https://www.knihovnaneratovice.cz/', 'https://katalog.knihovnaneratovice.cz/', 'Neratovice', 'SC');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (490, 290, 200, 'mkneratovice', 11, false, true, true, true, 'U', 'koha', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (490,'https://koha.knihovnaneratovice.cz/cgi-bin/koha/oai.pl','CPK','marccpk',NULL,'MEG502:(.*)');

-- 23. 01. 2023 tomascejpek
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (86, 410, 'SMG004');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (122, 463, 'BNG001');

-- 25. 01. 2023 tomascejpek
DELETE FROM download_import_conf WHERE import_conf_id in (351);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (351,'https://aleph.nkp.cz/OAI','ADR','marc21',NULL,'[^:]+:[^:]+:ADR10-(.*)');

-- 27. 01. 2023 tomascejpek
ALTER TABLE import_conf ADD COLUMN ziskej_edd_enabled BOOLEAN DEFAULT FALSE;

-- 01. 02. 2022 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (286, 'MKRYMAROV', 'https://knihovnarymarov.cz/', 'https://katalog.knihovnarymarov.cz', 'Rýmařov', 'MS');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (486, 286, 200, 'mkrymarov', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (486,'https://katalog.knihovnarymarov.cz/api/oai','cpk','marc21',NULL);

-- 01. 02. 2023 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (291, 'MKROKYCANY', 'https://www.rokyknih.cz/', 'https://katalog.rokyknih.cz/', 'Rokycany', 'PL');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (491, 291, 200, 'mkrokycany', 11, false, true, true, true, 'U', 'koha', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (491,'https://koha.rokyknih.cz/cgi-bin/koha/oai.pl','CPK','marccpk',NULL,'ROG001:(.*)');

-- 01. 02. 2022 tomascejpek
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (99049, 'KRAM-ROZHLAS', '', '', '','kramerius');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99049,99049,200,'kram-rozhlas',8,false,true,false,false,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99049,'https://kramerius.rozhlas.cz/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'https://kramerius.rozhlas.cz/uuid/');
