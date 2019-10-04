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

-- 19. 12. 2018 tomascejpek
CREATE SEQUENCE biblio_linker_seq_id MINVALUE 1;
CREATE TABLE biblio_linker (
  id                   DECIMAL(10) DEFAULT NEXTVAL('"biblio_linker_seq_id"')  PRIMARY KEY,
  updated              TIMESTAMP
);
ALTER TABLE harvested_record ADD COLUMN biblio_linker_id DECIMAL(10);
ALTER TABLE harvested_record ADD COLUMN biblio_linker_similar BOOLEAN DEFAULT FALSE;
CREATE TABLE biblio_linker_similar (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  harvested_record_similar_id DECIMAL(10),
  url_id               TEXT,
  type                 VARCHAR(20),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);
ALTER TABLE harvested_record ADD COLUMN next_biblio_linker_flag BOOLEAN DEFAULT FALSE;

-- 06. 05. 2019 tomascejpek
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
ALTER TABLE harvested_record ADD COLUMN bl_author VARCHAR(200);
CREATE INDEX harvested_record_bl_author_idx ON harvested_record(bl_author);
ALTER TABLE harvested_record ADD COLUMN bl_publisher VARCHAR(200);
CREATE INDEX harvested_record_bl_publisher_idx ON harvested_record(bl_publisher);
ALTER TABLE harvested_record ADD COLUMN bl_series VARCHAR(200);
CREATE INDEX harvested_record_bl_series_idx ON harvested_record(bl_series);
