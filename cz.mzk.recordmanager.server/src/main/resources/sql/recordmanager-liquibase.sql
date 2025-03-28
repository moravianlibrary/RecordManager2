--liquibase formatted sql
--changeset init:1
-- tables for Spring batch follows
CREATE TABLE BATCH_JOB_INSTANCE  (
	JOB_INSTANCE_ID BIGINT  NOT NULL PRIMARY KEY ,
	VERSION BIGINT ,
	JOB_NAME VARCHAR(100) NOT NULL,
	JOB_KEY VARCHAR(32) NOT NULL,
	constraint JOB_INST_UN unique (JOB_NAME, JOB_KEY)
) ;

CREATE TABLE BATCH_JOB_EXECUTION  (
	JOB_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY ,
	VERSION BIGINT  ,
	JOB_INSTANCE_ID BIGINT NOT NULL,
	CREATE_TIME TIMESTAMP NOT NULL,
	START_TIME TIMESTAMP DEFAULT NULL ,
	END_TIME TIMESTAMP DEFAULT NULL ,
	STATUS VARCHAR(10) ,
	EXIT_CODE VARCHAR(2500) ,
	EXIT_MESSAGE VARCHAR(2500) ,
	LAST_UPDATED TIMESTAMP,
	JOB_CONFIGURATION_LOCATION VARCHAR(2500) NULL,
	constraint JOB_INST_EXEC_FK foreign key (JOB_INSTANCE_ID)
	references BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
) ;

CREATE TABLE BATCH_JOB_EXECUTION_PARAMS  (
	JOB_EXECUTION_ID BIGINT NOT NULL ,
	TYPE_CD VARCHAR(6) NOT NULL ,
	KEY_NAME VARCHAR(100) NOT NULL ,
	STRING_VAL VARCHAR(250) ,
	DATE_VAL TIMESTAMP DEFAULT NULL ,
	LONG_VAL BIGINT ,
	DOUBLE_VAL DOUBLE PRECISION ,
	IDENTIFYING CHAR(1) NOT NULL ,
	constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID)
	references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;

CREATE TABLE BATCH_STEP_EXECUTION  (
	STEP_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY ,
	VERSION BIGINT NOT NULL,
	STEP_NAME VARCHAR(100) NOT NULL,
	JOB_EXECUTION_ID BIGINT NOT NULL,
	START_TIME TIMESTAMP NOT NULL ,
	END_TIME TIMESTAMP DEFAULT NULL ,
	STATUS VARCHAR(10) ,
	COMMIT_COUNT BIGINT ,
	READ_COUNT BIGINT ,
	FILTER_COUNT BIGINT ,
	WRITE_COUNT BIGINT ,
	READ_SKIP_COUNT BIGINT ,
	WRITE_SKIP_COUNT BIGINT ,
	PROCESS_SKIP_COUNT BIGINT ,
	ROLLBACK_COUNT BIGINT ,
	EXIT_CODE VARCHAR(2500) ,
	EXIT_MESSAGE VARCHAR(2500) ,
	LAST_UPDATED TIMESTAMP,
	constraint JOB_EXEC_STEP_FK foreign key (JOB_EXECUTION_ID)
	references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;

CREATE TABLE BATCH_STEP_EXECUTION_CONTEXT  (
	STEP_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
	SHORT_CONTEXT VARCHAR(2500) NOT NULL,
	SERIALIZED_CONTEXT TEXT ,
	constraint STEP_EXEC_CTX_FK foreign key (STEP_EXECUTION_ID)
	references BATCH_STEP_EXECUTION(STEP_EXECUTION_ID)
) ;

CREATE TABLE BATCH_JOB_EXECUTION_CONTEXT  (
	JOB_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
	SHORT_CONTEXT VARCHAR(2500) NOT NULL,
	SERIALIZED_CONTEXT TEXT ,
	constraint JOB_EXEC_CTX_FK foreign key (JOB_EXECUTION_ID)
	references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;

CREATE SEQUENCE BATCH_STEP_EXECUTION_SEQ MAXVALUE 9223372036854775807 NO CYCLE;
CREATE SEQUENCE BATCH_JOB_EXECUTION_SEQ MAXVALUE 9223372036854775807 NO CYCLE;
CREATE SEQUENCE BATCH_JOB_SEQ MAXVALUE 9223372036854775807 NO CYCLE;

CREATE TABLE recordmanager_key (
  name                 VARCHAR(128) PRIMARY KEY,
  val                  DECIMAL(10)
);

COMMENT ON TABLE recordmanager_key IS 'source of ids, used by Hibernate';

CREATE TABLE library (
  id                   DECIMAL(10) PRIMARY KEY,
  name                 VARCHAR(128),
  url                  VARCHAR(128),
  catalog_url          VARCHAR(128),
  city                 VARCHAR(60)
);

COMMENT ON TABLE library IS '';

CREATE TABLE contact_person (
  id                   DECIMAL(10) PRIMARY KEY,
  library_id           DECIMAL(10),
  name                 VARCHAR(128),
  email                VARCHAR(128),
  phone                VARCHAR(32),
  FOREIGN KEY (library_id) REFERENCES library(id)
);

COMMENT ON TABLE contact_person IS '';

CREATE TABLE import_conf (
  id                   DECIMAL(10) PRIMARY KEY,
  library_id           DECIMAL(10),
  contact_person_id    DECIMAL(10),
  id_prefix            VARCHAR(10),
  base_weight          DECIMAL(10),
  cluster_id_enabled   BOOLEAN DEFAULT FALSE,
  filtering_enabled    BOOLEAN DEFAULT FALSE,
  interception_enabled BOOLEAN DEFAULT FALSE,
  is_library           BOOLEAN DEFAULT FALSE,
  harvest_frequency    CHAR(1) DEFAULT 'U',
  CONSTRAINT import_conf_library_id_fk        FOREIGN KEY (library_id)        REFERENCES library(id),
  CONSTRAINT import_conf_contact_person_id_fk FOREIGN KEY (contact_person_id) REFERENCES contact_person(id)
);

COMMENT ON TABLE import_conf IS 'configuration of record sources';
COMMENT ON COLUMN import_conf.id_prefix IS 'prefix of source institution, used in SOLR as prefix of identifier';
COMMENT ON COLUMN import_conf.base_weight IS 'base weight for records from this source';
COMMENT ON COLUMN import_conf.cluster_id_enabled IS 'indicator whether cluster_id deduplication should be used for this source';
COMMENT ON COLUMN import_conf.filtering_enabled IS 'indicator whether additional filters for records from this source';
COMMENT ON COLUMN import_conf.interception_enabled IS 'indicator whether intercepting (intensional changes in records) should be applied for this source';
COMMENT ON COLUMN import_conf.is_library IS 'indicator whether this source is library or different type of institution. MOVE TO LIBRARY TABLE ???';
COMMENT ON COLUMN import_conf.harvest_frequency IS 'frequency of harvesting during automatic updates';


CREATE TABLE sigla (
  id                   DECIMAL(10),
  import_conf_id       DECIMAL(10),
  sigla                VARCHAR(20),
  CONSTRAINT sigla_pk PRIMARY KEY(import_conf_id,sigla),
  CONSTRAINT sigla_import_conf_fk FOREIGN KEY (import_conf_id) REFERENCES import_conf(id)
);

COMMENT ON TABLE sigla IS 'information about siglas(unique identifiers of libraries) for import configurations. MOVE TO IMPORT_CONF TABLE ???';

CREATE TABLE oai_harvest_conf (
  import_conf_id       DECIMAL(10) PRIMARY KEY,
  url                  VARCHAR(128),
  set_spec             VARCHAR(128),
  metadata_prefix      VARCHAR(128),
  granularity          VARCHAR(30),
  extract_id_regex     VARCHAR(128),
  harvest_job_name     VARCHAR(128),
  CONSTRAINT oai_harvest_conf_import_conf_fk FOREIGN KEY (import_conf_id) REFERENCES import_conf(id)
);

COMMENT ON TABLE oai_harvest_conf IS 'extension of import_conf for OAI';
COMMENT ON COLUMN oai_harvest_conf.url IS 'URL of OAI provider';
COMMENT ON COLUMN oai_harvest_conf.set_spec IS 'OAI set';


CREATE TABLE kramerius_conf (
  import_conf_id              DECIMAL(10)  PRIMARY KEY,
  url                         VARCHAR(128),
  url_solr		      VARCHAR(128),
  query_rows                  DECIMAL(10),
  metadata_stream             VARCHAR(128),
  auth_token 	              VARCHAR(128),
  fulltext_harvest_type       VARCHAR(128) DEFAULT 'fedora',
  download_private_fulltexts  BOOLEAN DEFAULT FALSE,
  CONSTRAINT kramerius_conf_import_conf_fk FOREIGN KEY (import_conf_id) REFERENCES import_conf(id)
);

COMMENT ON TABLE kramerius_conf IS 'extension of import_conf for Kramerius';
COMMENT ON COLUMN kramerius_conf.url IS 'url of Kramerius API endpoint';
COMMENT ON COLUMN kramerius_conf.url_solr IS 'url of Kramerius SOLR (used in specific cases when access to SOLR is granted by Kramerius owner)';
COMMENT ON COLUMN kramerius_conf.query_rows IS 'number of result rows requested from API, high numbers may cause latency problems';
COMMENT ON COLUMN kramerius_conf.metadata_stream IS 'type of metadata harvested from Kramerius';
COMMENT ON COLUMN kramerius_conf.auth_token IS 'Base64 token created from username and password provided by Kramerius owner to access private documents';
COMMENT ON COLUMN kramerius_conf.fulltext_harvest_type IS '[fedora|solr], fedora is default type, should work for most institutions';
COMMENT ON COLUMN kramerius_conf.download_private_fulltexts IS 'decides whether fulltexts should be harvested from private documents, auth_token is needed if true';

CREATE TABLE download_import_conf (
  import_conf_id       DECIMAL(10)  PRIMARY KEY,
  url                  VARCHAR(128),
  CONSTRAINT download_conf_import_conf_fk FOREIGN KEY (import_conf_id) REFERENCES import_conf(id)
);

CREATE TABLE format (
  format               VARCHAR(15) PRIMARY KEY,
  description          VARCHAR(255)
);

COMMENT ON TABLE format IS 'represents possible formats of harvested records (MARC, DC ..)';

CREATE SEQUENCE dedup_record_seq_id MINVALUE 1;
CREATE TABLE dedup_record (
  id                   DECIMAL(10) DEFAULT NEXTVAL('"dedup_record_seq_id"')  PRIMARY KEY,
  updated              TIMESTAMP
);

COMMENT ON TABLE dedup_record IS 'represents one deduplicated record which consists of one or more harvested records';
COMMENT ON COLUMN dedup_record.updated IS 'timestamp of last change';

CREATE TABLE harvested_record (
  id                   DECIMAL(10) PRIMARY KEY,
  import_conf_id       DECIMAL(10),
  record_id            VARCHAR(128),
  raw_001_id           VARCHAR(128),
  harvested            TIMESTAMP,
  updated              TIMESTAMP,
  deleted              TIMESTAMP,
  oai_timestamp        TIMESTAMP,
  format               VARCHAR(12) NOT NULL,
  dedup_record_id      DECIMAL(10),
  publication_year     DECIMAL(4),
  author_auth_key      VARCHAR(50),
  author_string        VARCHAR(200),
  issn_series          VARCHAR(300),
  issn_series_order    VARCHAR(300),
  uuid                 VARCHAR(100),
  scale                DECIMAL(10),
  weight               DECIMAL(10),
  cluster_id           VARCHAR(20),
  pages                DECIMAL(10),
  dedup_keys_hash      CHAR(40),
  next_dedup_flag      BOOLEAN DEFAULT TRUE,
  raw_record           BYTEA,
  UNIQUE (import_conf_id, record_id),
  FOREIGN KEY (import_conf_id) REFERENCES import_conf(id),
  FOREIGN KEY (format)              REFERENCES format(format)
);

COMMENT ON TABLE harvested_record IS 'basic table, contains full records in raw form';
COMMENT ON COLUMN harvested_record.id IS 'unique artificial record identifier';
COMMENT ON COLUMN harvested_record.import_conf_id IS 'source of record ';
COMMENT ON COLUMN harvested_record.record_id IS 'identifier of record, unique in source';
COMMENT ON COLUMN harvested_record.raw_001_id IS 'raw conent of 001 MARC field';
COMMENT ON COLUMN harvested_record.harvested IS 'timestamp of first record obtaining';
COMMENT ON COLUMN harvested_record.updated IS 'timestamp of last record change from source';
COMMENT ON COLUMN harvested_record.deleted IS 'timestamp of record deletion from source';
COMMENT ON COLUMN harvested_record.oai_timestamp IS 'raw timestamp from OAI protocol';
COMMENT ON COLUMN harvested_record.dedup_record_id IS 'dedup_record assigned to this record';
COMMENT ON COLUMN harvested_record.publication_year IS 'dedup_key: year of publication';
COMMENT ON COLUMN harvested_record.author_auth_key IS 'dedup_key: authority key of main author';
COMMENT ON COLUMN harvested_record.author_string IS 'dedup_key: normalized name of main author';
COMMENT ON COLUMN harvested_record.issn_series IS 'dedup_key: serie of issn';
COMMENT ON COLUMN harvested_record.issn_series_order IS 'dedup_key: order of issn series';
COMMENT ON COLUMN harvested_record.uuid IS 'UUID of record';
COMMENT ON COLUMN harvested_record.scale IS 'dedup_key: scale, for maps only';
COMMENT ON COLUMN harvested_record.weight IS 'estimated weight of record based on record quality';
COMMENT ON COLUMN harvested_record.cluster_id IS 'dedup_key: id for cluster deduplication, used for selected sources';
COMMENT ON COLUMN harvested_record.pages IS 'dedup_key: count of pages';
COMMENT ON COLUMN harvested_record.dedup_keys_hash IS 'SHA-1 hash of all dedup keys of record';
COMMENT ON COLUMN harvested_record.next_dedup_flag IS 'indicator whether this record should be deduplicated in next deduplication process';
COMMENT ON COLUMN harvested_record.format IS 'indicator format used for storing of raw_record';
COMMENT ON COLUMN harvested_record.raw_record IS 'raw record data in given format';


CREATE TABLE isbn (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  isbn                 DECIMAL(13),
  order_in_record      DECIMAL(4),
  note                 VARCHAR(300),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);

COMMENT ON TABLE isbn IS 'dedup_keys: table contatining ISBNs';

CREATE TABLE issn (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  issn                 VARCHAR(9),
  order_in_record      DECIMAL(4),
  note                 VARCHAR(100),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);

COMMENT ON TABLE issn IS 'dedup_keys: table contatining ISSNs';

CREATE TABLE cnb (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  cnb                  VARCHAR(100),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);

COMMENT ON TABLE cnb IS 'dedup_keys: table contatining CNBs';

CREATE TABLE title (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  title                VARCHAR(255),
  order_in_record      DECIMAL(4),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);

CREATE TABLE oclc (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  oclc                 VARCHAR(20),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);

COMMENT ON TABLE oclc IS 'dedup_keys: table contatining OCLCs';

CREATE TABLE language (
  harvested_record_id  DECIMAL(10),
  lang                 VARCHAR(5),
  PRIMARY KEY (harvested_record_id, lang),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);

CREATE TABLE harvested_record_format (
  id                   DECIMAL(10) PRIMARY KEY,
  name                 VARCHAR(50) UNIQUE
);

COMMENT ON TABLE harvested_record_format IS 'represents all possible physical formats of record';

CREATE TABLE harvested_record_format_link (
  harvested_record_id            DECIMAL(10),
  harvested_record_format_id     DECIMAL(10),
  CONSTRAINT record_link_pk      PRIMARY KEY (harvested_record_id, harvested_record_format_id),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE,
  FOREIGN KEY (harvested_record_format_id) REFERENCES harvested_record_format(id)
);

COMMENT ON TABLE harvested_record_format_link IS 'link table';

CREATE TABLE authority_record (
  id                   DECIMAL(10) PRIMARY KEY,
  import_conf_id       DECIMAL(10),
  oai_record_id        VARCHAR(128),
  authority_code       VARCHAR(128),
  harvested            TIMESTAMP,
  updated              TIMESTAMP,
  deleted              TIMESTAMP,
  format               VARCHAR(12) NOT NULL,
  raw_record           BYTEA,
  CONSTRAINT authority_record_import_conf_fk  FOREIGN KEY (import_conf_id)      REFERENCES import_conf(id),
  CONSTRAINT authority_record_format_fk       FOREIGN KEY (format)              REFERENCES format(format),
  CONSTRAINT authority_code_unique UNIQUE(authority_code)
);

COMMENT ON TABLE authority_record IS 'table holding authority records information';

CREATE TABLE antikvariaty (
  id                   DECIMAL(10) PRIMARY KEY,
  updated              TIMESTAMP,
  url                  VARCHAR(500),
  title                VARCHAR(255),
  pub_year             DECIMAL(5)
);

COMMENT ON TABLE antikvariaty IS 'data from muj-antikvariat.cz, used for record enrichment';

CREATE TABLE antikvariaty_catids (
  id_from_catalogue   VARCHAR(100),
  antikvariaty_id     DECIMAL(10),
  CONSTRAINT antikvariaty_catids_pk PRIMARY KEY (id_from_catalogue, antikvariaty_id),
  CONSTRAINT antikvariaty_catids_fk FOREIGN KEY (antikvariaty_id) REFERENCES antikvariaty(id)
);

COMMENT ON TABLE antikvariaty_catids IS 'extracted identifiers from antikvariaty records. These are used for mapping to real records.';

CREATE TABLE fulltext_kramerius (
  id                  DECIMAL(10) PRIMARY KEY,
  harvested_record_id DECIMAL(10),
  uuid_page           VARCHAR(50),
  is_private          BOOLEAN,
  order_in_document   DECIMAL(10),
  page                VARCHAR(50),
  fulltext            BYTEA,
  CONSTRAINT fulltext_kramerius_harvested_record_id_fk FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id)
);

COMMENT ON TABLE fulltext_kramerius IS 'harvested OCRs from digital libraries, page by page. REDUNDANT DATA, CONSIDER NORMALIZATION ???';
COMMENT ON COLUMN fulltext_kramerius.harvested_record_id IS 'link to record';
COMMENT ON COLUMN fulltext_kramerius.uuid_page IS 'UUID of page';
COMMENT ON COLUMN fulltext_kramerius.is_private IS 'is this fulltext publicly available?';
COMMENT ON COLUMN fulltext_kramerius.order_in_document IS 'sequential order of page in record';
COMMENT ON COLUMN fulltext_kramerius.page IS 'string notation of page';
COMMENT ON COLUMN fulltext_kramerius.fulltext IS 'raw OCR';

CREATE TABLE skat_keys (
  skat_record_id      DECIMAL(10),
  sigla               VARCHAR(20),
  local_record_id     VARCHAR(128),
  manually_merged     BOOLEAN DEFAULT FALSE,
  CONSTRAINT skat_keys_pk PRIMARY KEY(skat_record_id,sigla,local_record_id)
);

COMMENT ON TABLE skat_keys IS 'dedup_keys: extracted identifiers of merged records from SKAT';
COMMENT ON COLUMN skat_keys.skat_record_id IS 'identifier of SKAT record';
COMMENT ON COLUMN skat_keys.sigla IS 'sigla of local local record';
COMMENT ON COLUMN skat_keys.local_record_id IS 'identifier of local record';
COMMENT ON COLUMN skat_keys.manually_merged is 'indicator, whether source SKAT record was manually checked by librarian';

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

COMMENT ON TABLE cosmotron_996 IS 'records from cosmotron, used for periodicals';

CREATE TABLE obalkyknih_toc (
  id                   DECIMAL(10) PRIMARY KEY,
  book_id              DECIMAL(10),
  nbn                  VARCHAR(32),
  oclc                 VARCHAR(32),
  ean                  VARCHAR(32),
  isbn                 DECIMAL(13),
  toc                  VARCHAR(1048576)
);

COMMENT ON TABLE obalkyknih_toc IS 'downloaded table of contents from obalkyknih.cz';

CREATE TABLE inspiration (
  id					DECIMAL(10) PRIMARY KEY,
  harvested_record_id	DECIMAL(10),
  name					VARCHAR(32),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);

-- inserts into reference tables follow

-- format
INSERT INTO format (format,description) VALUES ('marc21-xml','MARC21 XML');
INSERT INTO format (format,description) VALUES ('xml-marc','MARC21 XML');
INSERT INTO format (format,description) VALUES ('marccpk','MARC21 XML');
INSERT INTO format (format,description) VALUES ('dublinCore','Dublin Core');
INSERT INTO format (format,description) VALUES ('oai_marcxml_cpk','MARC21 XML');
INSERT INTO format (format,description) VALUES ('marc21e','MARC21 XML');
INSERT INTO format (format,description) VALUES ('ese','Dublin Core');

-- harvested_record_format
INSERT INTO harvested_record_format(id, name) VALUES (1, 'BOOKS');
INSERT INTO harvested_record_format(id, name) VALUES (2, 'PERIODICALS');
INSERT INTO harvested_record_format(id, name) VALUES (3, 'ARTICLES');
INSERT INTO harvested_record_format(id, name) VALUES (4, 'MAPS');
INSERT INTO harvested_record_format(id, name) VALUES (5, 'MUSICAL_SCORES');
INSERT INTO harvested_record_format(id, name) VALUES (6, 'VISUAL_DOCUMENTS');
INSERT INTO harvested_record_format(id, name) VALUES (7, 'MANUSCRIPTS');
INSERT INTO harvested_record_format(id, name) VALUES (8, 'OTHER_MICROFORMS');
INSERT INTO harvested_record_format(id, name) VALUES (9, 'LARGE_PRINTS');
INSERT INTO harvested_record_format(id, name) VALUES (10, 'OTHER_BRAILLE');
INSERT INTO harvested_record_format(id, name) VALUES (11, 'ELECTRONIC_SOURCE');
INSERT INTO harvested_record_format(id, name) VALUES (12, 'AUDIO_DOCUMENTS');
INSERT INTO harvested_record_format(id, name) VALUES (13, 'AUDIO_CD');
INSERT INTO harvested_record_format(id, name) VALUES (14, 'AUDIO_DVD');
INSERT INTO harvested_record_format(id, name) VALUES (15, 'AUDIO_LP');
INSERT INTO harvested_record_format(id, name) VALUES (16, 'AUDIO_CASSETTE');
INSERT INTO harvested_record_format(id, name) VALUES (17, 'AUDIO_OTHER');
INSERT INTO harvested_record_format(id, name) VALUES (18, 'VIDEO_DOCUMENTS');
INSERT INTO harvested_record_format(id, name) VALUES (19, 'VIDEO_BLURAY');
INSERT INTO harvested_record_format(id, name) VALUES (20, 'VIDEO_VHS');
INSERT INTO harvested_record_format(id, name) VALUES (21, 'VIDEO_DVD');
INSERT INTO harvested_record_format(id, name) VALUES (22, 'VIDEO_CD');
INSERT INTO harvested_record_format(id, name) VALUES (23, 'VIDEO_OTHER');
INSERT INTO harvested_record_format(id, name) VALUES (24, 'OTHER_KIT');
INSERT INTO harvested_record_format(id, name) VALUES (25, 'OTHER_OBJECT');
INSERT INTO harvested_record_format(id, name) VALUES (26, 'OTHER_MIX_DOCUMENT');
INSERT INTO harvested_record_format(id, name) VALUES (27, 'NORMS');
INSERT INTO harvested_record_format(id, name) VALUES (100, 'OTHER_UNSPECIFIED');

-- create views
CREATE OR REPLACE VIEW harvested_record_view AS
SELECT
  import_conf_id,
  record_id,
  deleted,
  format,
  convert_from(raw_record, 'UTF8') AS raw_record
FROM
  harvested_record
;

CREATE OR REPLACE VIEW dedup_record_last_update AS
SELECT
  dr.id dedup_record_id,
  GREATEST(dr.updated, (SELECT MAX(updated) FROM harvested_record hr WHERE hr.dedup_record_id = dr.id)) last_update
FROM
  dedup_record dr
;

CREATE MATERIALIZED VIEW dedup_record_last_update_mat AS
SELECT
  dedup_record_id,
  last_update
FROM
  dedup_record_last_update
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

CREATE VIEW antikvariaty_url_view AS
SELECT
  hr.dedup_record_id,
  a.url
FROM harvested_record hr
  INNER JOIN antikvariaty_catids ac on hr.cluster_id = ac.id_from_catalogue
  INNER JOIN antikvariaty a on ac.antikvariaty_id = a.id
ORDER BY hr.weight DESC
;

--changeset init:2 context:cpk
INSERT INTO library (id, name, url, catalog_url, city) VALUES (100, 'MZK', 'www.mzk.cz', 'vufind.mzk.cz', 'Brno');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (101, 'NLK', 'medvik.cz', 'medivk.cz', 'Praha');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (102, 'MKP', 'www.mlp.cz', 'search.mlp.cz', 'Praha');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (103, 'KJM', 'kjm.cz', 'http://katalog.kjm.cz:8080/Carmen/', 'Brno');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (104, 'NKP', 'nkp.cz', 'aleph.nkp.cz', 'Praha');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (105, 'VPK', 'vpk.cz', 'vpk.cz', 'Praha');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (106, 'TRE', 'katalogknih.cz', 'vufind.katalogknih.cz', 'Česká Třebová');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (107, 'NTK', 'techlib.cz', 'aleph.techlib.cz', 'Praha');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (108, 'KVKL', 'http://www.kvkli.cz/', 'pac.kvkli.cz/arl-li/', 'Liberec');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (109, 'VPK', 'vpk.cz', 'vpk.cz', 'Liberec');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (110, 'ANTIKVARIATY', 'muj-antikvariat.cz', 'muj-antikvariat.cz', null);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (111, 'VKTA', 'knihovnatabor.cz', 'vkta.cz/Clavius', 'Tábor');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (112, 'KKVY', 'kkvysociny.cz', 'kkvysociny.cz/clavius', 'Havlíčkův Brod');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (113, 'SVKHK', 'svkhk.cz', 'aleph2.svkhk.cz', 'Hradec Králové');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (114, 'SVKUL', 'svkul.cz', 'katalog.svkul.cz', 'Ústí nad Labem');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (115, 'VKOL', 'vkol.cz', 'www.vkol.cz/cs/katalog/', 'Olomouc');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (116, 'CASLIN', 'caslin.cz', 'aleph.nkp.cz', 'Praha');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (117, 'NKP2', 'nkp.cz', 'aleph.nkp.cz', 'Praha');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (118, 'SFXJIB', 'jib.cz', 'http://www.jib.cz', 'Praha');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (119, 'ANL', 'nkp.cz', 'aleph.nkp.cz', null);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (120, 'MZKNORMS', 'mzk.cz', 'aleph.mzk.cz', null);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (121, 'SLK', 'nkp.cz', 'aleph.nkp.cz', 'Praha');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (122, 'KPSYS', '', '', 'KPSYS');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (123, 'CNB', 'nkp.cz', 'aleph.nkp.cz', 'Praha');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (125, 'KKL', 'nkp.cz', 'aleph.nkp.cz', 'Praha');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (126, 'STT', 'nkp.cz', 'aleph.nkp.cz', 'Praha');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (128, 'CBVK', 'cbvk.cz', 'katalog.cbvk.cz', 'České Budějovice');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (127, 'OPENLIB', 'openlibrary.org', '', null);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (129, 'VKTATEST', 'knihovnatabor.cz', 'vkta.cz/Clavius', 'Tábor');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (130, 'KNAV', 'cas.cz', 'aleph.lib.cas.cz', 'Praha');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (131, 'BCBT', 'cas.cz', 'clavius.lib.cas.cz/katalog/', null);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (132, 'KKKV', 'knihovnakv.cz', 'katalog.kvary.cz', 'Karlovy Vary');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (133, 'KKPC', 'kkpce.cz', 'aleph.knihovna-pardubice.cz', 'Pardubice');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (134, 'MKKH', 'knihovna-kh.cz', 'kutnahora.knihovny.net:8080/Carmen/', 'Kutná Hora');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (135, 'SVKOS', 'svkos.cz', 'katalog.svkos.cz', 'Ostrava');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (136, 'SVKKL', 'svkkl.cz', 'ipac.svkkl.cz', 'Kladno');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (137, 'SVKPK', 'svkpl.cz', 'aleph.svkpl.cz', 'Plzeň');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (138, 'IIR', 'iir.cz', 'katalog.iir.cz:8080/Carmen/', 'Praha');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (139, 'MANUSCRIPTORIUM', 'www.manuscriptorium.com', 'www.manuscriptorium.com', null);

INSERT INTO contact_person (id, library_id, name) VALUES (200, 100, 'Jan Novak');

INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (319, 119, 200, 'anl', 14, false, false, false, false, 'U');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (318, 118, 200, 'sfxjib', 8, false, false, false, false, 'U');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (1301, 101, 200, 'sfxjibnlk', 8, false, false, false, true, 'U');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (1300, 100, 200, 'sfxjibmzk', 8, false, false, false, true, 'U');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (321, 121, 200, 'slk', 10, false, false, false, true, 'U');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (1302, 101, 200, 'sfxnlkper', 8, false, false, false, true, 'U');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (325, 125, 200, 'kkl', 11, false, false, false, false, 'U');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (326, 126, 200, 'stt', 11, false, false, false, false, 'U');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (327, 127, 200, 'openlib', 8, false, false, true, false, 'U');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (331, 131, 200, 'bcbt', 8, false, false, false, false, 'U');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (300, 100, 200, 'mzk', 13, true, false, false, true, 'D');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (302, 102, 200, 'mkp', 8, false, false, false, true, 'D');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (304, 104, 200, 'nkp', 14, true, false, false, true, 'D');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (307, 107, 200, 'ntk', 14, false, false, false, true, 'D');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (306, 106, 200, 'tre', 11, false, true, false, true, 'D');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (311, 111, 200, 'vkta', 10, false, false, false, true, 'D');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (312, 112, 200, 'kkvy', 12, false, false, false, true, 'D');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (313, 113, 200, 'svkhk', 14, false, false, false, true, 'D');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (314, 114, 200, 'svkul', 14, false, false, false, true, 'D');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (400, 104, 200, 'auth', 0, false, false, false, true, 'D');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (317, 117, 200, 'nkp2', 14, false, false, false, true, 'D');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (315, 115, 200, 'vkol', 14, true, false, false, true, 'D');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (301, 101, 200, 'nlk', 14, false, false, true, true, 'D');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (320, 120, 200, 'unmz', 10, false, false, true, false, 'D');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (322, 122, 200, 'kpsys', 12, false, false, false, true, 'D');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (323, 123, 200, 'cnb', 11, false, false, false, false, 'D');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (329, 129, 200, 'vktatest', 10, false, false, false, true, 'D');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (330, 130, 200, 'knav', 11, false, false, false, true, 'D');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (324, 100, 200, 'mzk', 13, true, false, false, true, 'D');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (303, 103, 200, 'kjm', 0, false, false, false, true, 'U');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (309, 109, 200, 'vpk', 14, false, false, false, true, 'U');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (305, 105, 200, 'vpk', 14, false, false, false, true, 'U');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (332, 132, 200, 'kkkv', 12, false, false, false, true, 'U');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (333, 133, 200, 'kkpc', 11, false, false, false, true, 'U');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (334, 134, 200, 'mkkh', 11, false, false, false, true, 'U');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (335, 135, 200, 'svkos', 14, false, false, false, true, 'U');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (336, 136, 200, 'svkkl', 14, false, false, false, true, 'U');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (337, 137, 200, 'svkpk', 14, false, false, false, true, 'U');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (338, 138, 200, 'iir', 12, false, false, false, true, 'U');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (339, 139, 200, 'manuscript', 8, false, false, false, false, 'U');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (316, 116, 200, 'caslin', 11, false, true, true, false, 'D');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (308, 108, 200, 'kvkl', 14, false, false, false, true, 'D');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (328, 128, 200, 'cbvk', 14, false, false, false, true, 'D');

INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (301, 'http://oai.medvik.cz/medvik2cpk/oai', null, 'xml-marc', 'DAY', null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (303, 'http://katalog.kjm.cz/l.dll', null, 'marc21', 'DAY', null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (309, 'http://sc.vpk.cz/cgi-bin/oai2', null, 'marc21', 'SECOND', null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (312, 'http://www.kkvysociny.cz/clavius/l.dll', 'CPK', 'marc21', 'DAY', null, 'oaiPartitionedHarvestJob');
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (311, 'http://www.vkta.cz/Clavius/l.dll', 'CPK', 'marc21', 'DAY', null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (313, 'http://aleph.svkhk.cz/OAI', 'HKAOAI', 'marc21', 'SECOND', null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (314, 'http://katalog.svkul.cz/l.dll', 'CPK', 'marc21', 'DAY', null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (315, 'http://aleph.vkol.cz/OAI', 'VKOLOAI', 'marc21', 'SECOND', null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (335, 'http://katalog.svkos.cz/OAI', 'MZK-CPK', 'marc21', 'SECOND', null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (316, 'http://aleph.nkp.cz/OAI', 'SKC', 'marc21', 'SECOND', null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (317, 'http://aleph.nkp.cz/OAI', 'NKC-CPK', 'marc21', 'SECOND', null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (318, null, null, 'marc21', null, null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (400, 'http://aleph.nkp.cz/OAI', 'AUT', 'marc21', 'SECOND', null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (1300, null, null, 'marc21', null, null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (1301, null, null, 'marc21', null, null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (304, 'http://aleph.nkp.cz/OAI', 'NKC-CPK', 'marc21', 'SECOND', null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (307, 'http://aleph.techlib.cz/OAI', 'CPK', 'marc21', 'SECOND', null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (322, 'http://portaro.eu/pracovni/api/oai', '0', 'marc21', 'DAY', null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (323, 'http://aleph.nkp.cz/OAI', 'CNB', 'marc21', 'SECOND', null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (305, 'http://sc.vpk.cz/cgi-bin/oai2', null, 'marc21', 'SECOND', null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (1302, null, null, 'marc21', null, null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (300, 'http://aleph.mzk.cz/OAI', 'MZK01-CPK', 'marc21', 'SECOND', null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (324, 'http://aleph.mzk.cz/OAI', 'MZK03-CPK', 'marc21', 'SECOND', null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (327, null, null, 'marc21', null, null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (329, 'http://www.clavius.sk/carmentest/l.dll', 'CPK', 'marc21', 'DAY', null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (302, 'http://web2.mlp.cz/cgi/oaie', 'complete', 'marc21e', 'SECOND', null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (330, 'http://aleph.lib.cas.cz/OAI', 'KNA', 'marc21', 'SECOND', null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (319, null, null, 'marc21', null, '[^:]+:(.*)', null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (321, null, null, 'marc21', null, '[^:]+:(.*)', null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (325, null, null, 'marc21', null, '[^:]+:(.*)', null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (326, null, null, 'marc21', null, '[^:]+:(.*)', null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (320, 'http://aleph.mzk.cz/OAI', 'MZK04', 'marc21', 'SECOND', '[^:]+:[^:]+:MZK04-(.*)', null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (306, 'http://opac.moderniknihovna.cz/cgi-bin/koha/oai.pl', 'CPK', 'marccpk', 'DAY', 'UOG505:(.*)', null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (331, null, null, 'marc21', null, null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (332, null, null, 'marc21', null, null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (333, null, null, 'marc21', null, null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (334, null, null, 'marc21', null, null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (336, null, null, 'marc21', null, null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (337, null, null, 'marc21', null, null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (338, null, null, 'marc21', null, null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (339, 'http://dbase.aipberoun.cz/manu3/oai/', 'digitized-xr', 'ese', 'SECOND', null, null);
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (308, 'http://93.99.138.143/i2/i2.ws.oai.cls', 'CPK1', 'oai_marcxml_cpk', 'SECOND', null, 'cosmotronHarvestJob');
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (328, 'http://katalog.cbvk.cz/i2/i2.ws.oai.cls', 'CPK1', 'oai_marcxml_cpk', 'SECOND', null, 'cosmotronHarvestJob');

INSERT INTO sigla (import_conf_id, sigla, id) VALUES (300, 'BOA001', 1);
INSERT INTO sigla (import_conf_id, sigla, id) VALUES (301, 'ABA008', 2);
INSERT INTO sigla (import_conf_id, sigla, id) VALUES (302, 'ABG001', 3);
INSERT INTO sigla (import_conf_id, sigla, id) VALUES (303, 'BOG001', 4);
INSERT INTO sigla (import_conf_id, sigla, id) VALUES (304, 'ABA001', 5);
INSERT INTO sigla (import_conf_id, sigla, id) VALUES (304, 'ABA003', 6);
INSERT INTO sigla (import_conf_id, sigla, id) VALUES (304, 'ABA018', 7);
INSERT INTO sigla (import_conf_id, sigla, id) VALUES (304, 'ABA019', 8);
INSERT INTO sigla (import_conf_id, sigla, id) VALUES (304, 'ABA025', 9);
INSERT INTO sigla (import_conf_id, sigla, id) VALUES (306, 'UOG505', 10);
INSERT INTO sigla (import_conf_id, sigla, id) VALUES (306, 'UOG011', 11);
INSERT INTO sigla (import_conf_id, sigla, id) VALUES (307, 'ABA013', 12);
INSERT INTO sigla (import_conf_id, sigla, id) VALUES (307, 'ABA031', 13);
INSERT INTO sigla (import_conf_id, sigla, id) VALUES (308, 'LIA001', 14);
INSERT INTO sigla (import_conf_id, sigla, id) VALUES (311, 'TAG001', 15);
INSERT INTO sigla (import_conf_id, sigla, id) VALUES (312, 'HBG001', 16);
INSERT INTO sigla (import_conf_id, sigla, id) VALUES (313, 'HKA001', 17);
INSERT INTO sigla (import_conf_id, sigla, id) VALUES (314, 'ULG001', 18);
INSERT INTO sigla (import_conf_id, sigla, id) VALUES (315, 'OLA001', 19);
INSERT INTO sigla (import_conf_id, sigla, id) VALUES (321, 'ABA004', 20);
INSERT INTO sigla (import_conf_id, sigla, id) VALUES (322, 'ZLG001', 21);
INSERT INTO sigla (import_conf_id, sigla, id) VALUES (324, 'BOA001', 22);
INSERT INTO sigla (import_conf_id, sigla, id) VALUES (325, 'ABA003', 23);
INSERT INTO sigla (import_conf_id, sigla, id) VALUES (328, 'CBA001', 24);
INSERT INTO sigla (import_conf_id, sigla, id) VALUES (330, 'ABA007', 25);
INSERT INTO sigla (import_conf_id, sigla, id) VALUES (332, 'KVG001', 26);
INSERT INTO sigla (import_conf_id, sigla, id) VALUES (333, 'PAG001', 27);
INSERT INTO sigla (import_conf_id, sigla, id) VALUES (334, 'KHG001', 28);
INSERT INTO sigla (import_conf_id, sigla, id) VALUES (335, 'OSA001', 29);
INSERT INTO sigla (import_conf_id, sigla, id) VALUES (336, 'KLG001', 30);
INSERT INTO sigla (import_conf_id, sigla, id) VALUES (337, 'PNA001', 31);
INSERT INTO sigla (import_conf_id, sigla, id) VALUES (338, 'ABC016', 32);

--changeset xrosecky:3
ALTER TABLE import_conf ADD COLUMN mapping_script VARCHAR(256);

--changeset tomascejpek:4 context:cpk
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (1304, 130, 200, 'sfxknav', 8, false, false, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (1304,NULL,NULL,'marc21',NULL);

--changeset tomascejpek:5 context:cpk
INSERT INTO library (id, name, url, catalog_url, city) VALUES (400, 'AUTHORITY', 'nkp.cz', 'aleph.nkp.cz', NULL);
UPDATE import_conf SET library_id=400 WHERE id=400;
UPDATE import_conf SET is_library=false WHERE id=400;
UPDATE import_conf SET mapping_script='AuthorityMarc.groovy' WHERE id=400;
INSERT INTO harvested_record_format(id, name) VALUES (28, 'PERSON');

--changeset tomascejpek:6
ALTER TABLE title ADD COLUMN similarity_enabled BOOLEAN DEFAULT(FALSE);

--changeset tomascejpek:7 context:cpk
UPDATE import_conf SET library_id=104 WHERE id in (321,325,326);
UPDATE import_conf SET is_library=true WHERE id in (325,326);

--changeset tomascejpek:8 context:cpk
UPDATE import_conf SET filtering_enabled=true WHERE id=400;

--changeset tomascejpek:9 context:cpk
INSERT INTO library (id, name, url, catalog_url, city) VALUES (140, 'MKUO', 'knihovna-uo.cz', 'vufind.knihovna-uo.cz/vufind/', 'Ústí nad Orlicí');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (340, 140, 200, 'mkuo', 8, false, true, false, true, 'D');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (340,'http://katalog-usti.knihovna-uo.cz/cgi-bin/koha/oai.pl','CPK','marc21',NULL,'UOG001:(.*)');

--changeset xrosecky:10
ALTER TABLE import_conf ADD COLUMN generate_dedup_keys BOOLEAN DEFAULT(TRUE);

--changeset tomascejpek:11 context:cpk
INSERT INTO library (id, name, url, catalog_url, city) VALUES (141, 'OSOBNOSTI', 'osobnostiregionu.cz', 'http://hledani.osobnostiregionu.cz/', NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency,mapping_script) VALUES (341, 141, 200, 'osobnosti', 0, false, false, false, false, 'U', 'AuthorityMarc.groovy');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (341,NULL,NULL,'marc21',NULL);

--changeset tomascejpek:12 context:cpk
UPDATE oai_harvest_conf SET metadata_prefix='marccpk' WHERE import_conf_id=340;

--changeset tomascejpek:13 context:cpk
UPDATE import_conf SET harvest_frequency='D' WHERE id=334;
UPDATE oai_harvest_conf SET url='http://109.73.209.153/clavius/l.dll', set_spec='NKP' WHERE import_conf_id=334;
UPDATE import_conf SET harvest_frequency='D' WHERE id=338;
UPDATE oai_harvest_conf SET url='http://katalogold.iir.cz:81/l.dll', set_spec='CPK' WHERE import_conf_id=338;

--changeset tomascejpek:14 context:cpk
INSERT INTO library (id, name, url, catalog_url, city) VALUES (142, 'BMC', 'nlk.cz', 'www.medvik.cz/bmc/', NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (342, 142, 200, 'bmc', 8, false, false, false, false, 'D');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (342,'http://oai.medvik.cz/bmc/oai',NULL,'xml-marc',NULL);

--changeset tomascejpek:15 context:cpk
UPDATE oai_harvest_conf SET set_spec='CPK' WHERE import_conf_id=334;

--changeset tomascejpek:16 context:cpk
UPDATE import_conf SET harvest_frequency='U' WHERE id in (329,323);

--changeset tomascejpek:17 context:cpk
UPDATE import_conf SET harvest_frequency='D' WHERE id=336;
UPDATE oai_harvest_conf SET url='http://svk7.svkkl.cz/i2/i2.ws.oai.cls', set_spec='CPK1', metadata_prefix='oai_marcxml_cpk', harvest_job_name='cosmotronHarvestJob' WHERE import_conf_id=336;

--changeset tomascejpek:18
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

--changeset tomascejpek:19
ALTER TABLE download_import_conf ADD COLUMN import_job_name VARCHAR(128);

--changeset tomascejpek:20
ALTER TABLE download_import_conf ADD COLUMN format VARCHAR(128);
ALTER TABLE download_import_conf ADD COLUMN extract_id_regex VARCHAR(128);

--changeset tomascejpek:21 context:cpk
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

--changeset tomascejpek:22 context:cpk
UPDATE harvested_record_format SET name='OTHER_PERSON' WHERE id=28;

--changeset tomascejpek:23
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

--changeset tomascejpek:24 context:cpk
INSERT INTO library (id, name, url, catalog_url, city) VALUES (143, 'KFBZ', 'kfbz.cz', 'katalog.kfbz.cz/', 'Zlín');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (343, 143, 200, 'kfbz', 12, false, false, false, true, 'D');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (343,'http://katalog.kfbz.cz/api/oai','0','marc21',NULL);

--changeset tomascejpek:25
ALTER TABLE inspiration ALTER COLUMN name TYPE VARCHAR(128);

--changeset tomascejpek:26 context:cpk
UPDATE import_conf SET id_prefix='nkp' WHERE id in (321,325,326);
UPDATE oai_harvest_conf SET extract_id_regex='s/[^:]+:(.*)/SLK01-$1/' WHERE import_conf_id=321;
UPDATE oai_harvest_conf SET extract_id_regex='s/[^:]+:(.*)/KKL01-$1/' WHERE import_conf_id=325;
UPDATE oai_harvest_conf SET extract_id_regex='s/[^:]+:(.*)/STT01-$1/' WHERE import_conf_id=326;
UPDATE oai_harvest_conf SET url='http://ipac.kvkli.cz/i2/i2.ws.oai.cls' WHERE import_conf_id=308;

--changeset tomascejpek:27 context:cpk
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (345, 102, 200, 'mkpe', 8, false, false, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id, url, set_spec, metadata_prefix, granularity, extract_id_regex, harvest_job_name) VALUES (345, 'http://web2.mlp.cz/cgi/oai', 'ebook', 'marc21', null, null, null);

--changeset tomascejpek:28
INSERT INTO harvested_record_format(id, name) VALUES (29, 'LEGISLATIVE_GOVERNMENT_ORDERS');
INSERT INTO harvested_record_format(id, name) VALUES (30, 'LEGISLATIVE_REGULATIONS');
INSERT INTO harvested_record_format(id, name) VALUES (31, 'LEGISLATIVE_COMMUNICATION');
INSERT INTO harvested_record_format(id, name) VALUES (32, 'LEGISLATIVE_LAWS');
INSERT INTO harvested_record_format(id, name) VALUES (33, 'LEGISLATIVE_LAWS_TEXT');
INSERT INTO harvested_record_format(id, name) VALUES (34, 'LEGISLATIVE_FINDING');
INSERT INTO harvested_record_format(id, name) VALUES (35, 'LEGISLATIVE_CONSTITUTIONAL_LAWS');
INSERT INTO harvested_record_format(id, name) VALUES (36, 'LEGISLATIVE_DECISIONS');

--changeset tomascejpek:29 context:cpk
INSERT INTO library (id, name, url, catalog_url, city) VALUES (144, 'ZAKONY', 'zakonyprolidi.cz', '', NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (344, 144, 200, 'zakony', 8, false, true, false, false, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (344,NULL,NULL,'marc21',NULL);

--changeset tomascejpek:30
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

--changeset tomascejpek:31 context:cpk
DELETE FROM sigla WHERE id IN (4,26,27,31);
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (33, 340, 'UOG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (34, 340, 'UOG009');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (35, 340, 'UOG010');

--changeset tomascejpek:32 
CREATE OR REPLACE VIEW dedup_record_orphaned AS
SELECT
  dr.id dedup_record_id,
  dr.updated AS orphaned
FROM
  dedup_record dr
WHERE
  NOT EXISTS(SELECT 1 FROM harvested_record hr WHERE hr.dedup_record_id = dr.id and deleted is null)
;

--changeset tomascejpek:33 context:cpk
UPDATE import_conf SET interception_enabled='true' WHERE id=312;

--changeset tomascejpek:34 context:cpk
INSERT INTO library (id, name, url, catalog_url, city) VALUES (146, 'MKPR', 'http://knihovnaprerov.cz/', 'katalog.knihovnaprerov.cz/', 'Přerov');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (346, 146, 200, 'mkpr', 12, false, false, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (346,'http://katalog.knihovnaprerov.cz/l.dll','CPK','marc21',NULL);

--changeset tomascejpek:35 context:cpk
UPDATE import_conf SET base_weight=9 WHERE id=346;

--changeset tomascejpek:36 context:cpk
DELETE FROM oai_harvest_conf WHERE import_conf_id in (319,321,325,326);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (319,'local:/data/imports/aleph.ANL','importOaiRecordsJob',null,'[^:]+:(.*)');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (321,'local:/data/imports/aleph.SLK','importOaiRecordsJob',null,'s/[^:]+:(.*)/SLK01-$1/');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (325,'local:/data/imports/aleph.KKL','importOaiRecordsJob',null,'s/[^:]+:(.*)/KKL01-$1/');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (326,'local:/data/imports/aleph.STT','importOaiRecordsJob',null,'s/[^:]+:(.*)/STT01-$1/');

--changeset tomascejpek:37 context:cpk
INSERT INTO library (id, name, url, catalog_url, city) VALUES (147, 'UPV', 'http://upv.cz/', 'https://isdv.upv.cz/webapp/pts.frm', NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (347, 147, 200, 'upv', 8, false, false, false, false, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (347,NULL,NULL,'marc21',NULL);

--changeset tomascejpek:38
INSERT INTO harvested_record_format(id, name) VALUES (58, 'PATENTS');

--changeset tomascejpek:39
ALTER TABLE harvested_record ADD COLUMN source_info VARCHAR(255);
CREATE INDEX harvested_record_source_info_idx ON harvested_record(source_info);

--changeset tomascejpek:40
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

--changeset tomascejpek:41
CREATE TABLE short_title (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  short_title          VARCHAR(255),
  order_in_record      DECIMAL(4),
  similarity_enabled   BOOLEAN DEFAULT FALSE,
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);
CREATE INDEX short_title_harvested_record_idx ON short_title(harvested_record_id);

--changeset tomascejpek:42
INSERT INTO harvested_record_format(id, name) VALUES (59, 'COMPUTER_CARRIERS');

--changeset tomascejpek:43
INSERT INTO harvested_record_format(id, name) VALUES (60, 'OTHER_OTHER');

--changeset tomascejpek:44
UPDATE harvested_record_format SET name='OTHER_COMPUTER_CARRIER' WHERE id=59;

--changeset tomascejpek:45 context:cpk
UPDATE import_conf SET base_weight=13 WHERE id in (308,314,328,335,336);
UPDATE import_conf SET base_weight=10 WHERE id=313;

--changeset tomascejpek:46 context:cpk
INSERT INTO library (id, name, url, catalog_url, city) VALUES (148, 'NPMK', 'http://npmk.cz/', 'http://katalog.npmk.cz/', NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (348, 148, 200, 'npmk', 11, false, false, false, false, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (348,'http://katalog.npmk.cz/api/oai','4','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (149, 'EUHB', 'https://www.lib.cas.cz/', '', NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (349, 149, 200, 'euhb', 9, false, false, false, false, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (349,'http://aleph.lib.cas.cz/OAI','EUHB','marc21',NULL);

--changeset tomascejpek:47
INSERT INTO harvested_record_format(id, name) VALUES (61, 'PATENTS_UTILITY_MODELS');
INSERT INTO harvested_record_format(id, name) VALUES (62, 'PATENTS_PATENT_APPLICATIONS');
INSERT INTO harvested_record_format(id, name) VALUES (63, 'PATENTS_PATENTS');

--changeset tomascejpek:48 context:cpk
UPDATE import_conf SET generate_dedup_keys=false WHERE id=347;

--changeset tomascejpek:49 context:cpk
UPDATE import_conf SET id_prefix='muzibib' WHERE id=349;
UPDATE library SET name='MUZIBIB' WHERE id=149;
UPDATE import_conf SET generate_dedup_keys=false WHERE id=331;

--changeset tomascejpek:50 context:cpk
UPDATE oai_harvest_conf SET set_spec='cpk' WHERE import_conf_id=302;

--changeset tomascejpek:51 context:cpk
UPDATE import_conf SET filtering_enabled=true WHERE id=314;

--changeset tomascejpek:52
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

--changeset tomascejpek:53 context:cpk
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys) VALUES (352,101,200,'mesh',0,false,false,false,false,'U',null,false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (352,NULL,NULL,'marc21',NULL);

--changeset tomascejpek:54 context:cpk
INSERT INTO library (id, name, url, catalog_url, city) VALUES (150, 'MKCHODOV', 'https://www.knihovnachodov.cz/', 'https://www.knihovnachodov.cz/Katalog/', 'Chodov');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (350, 150, 200, 'mkchodov', 11, false, false, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (350,'https://www.knihovnachodov.cz/Tritius/oai-provider','CPK_124','marc21',NULL);

--changeset tomascejpek:55 context:cpk
UPDATE import_conf SET filtering_enabled=true WHERE id=327;

--changeset tomascejpek:56 context:cpk
INSERT INTO library (id, name, url, catalog_url, city) VALUES (153, 'OKPB', 'http://www.okpb.cz', 'http://www.okpb.cz/clavius/', 'Opava');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (353, 153, 200, 'okpb', 12, false, false, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (353,'http://www.okpb.cz/clavius/l.dll','CPK','marc21',NULL);

--changeset tomascejpek:57 context:cpk
UPDATE library SET city='Bibliography' WHERE id in (119,131,142,148,149);
UPDATE library SET city=NULL WHERE id=116;

--changeset tomascejpek:58
ALTER TABLE import_conf ADD COLUMN mapping_dedup_script VARCHAR(256);

--changeset tomascejpek:59 context:cpk
UPDATE import_conf SET mapping_dedup_script='AuthorityMergedBaseMarc.groovy' WHERE id in (341,400);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (151, 'LIBRARY', 'nkp.cz', '', NULL);
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,generate_dedup_keys,mapping_script,mapping_dedup_script) VALUES (351, 151, 200, 'library', 0, false, true, false, false, 'U', false, 'AdresarKnihovenBaseMarc.groovy', 'AdresarKnihovenBaseMarc.groovy');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (351,'local:/data/imports/aleph.ADR','importOaiRecordsJob',null,'[^:]+:(.*)');
INSERT INTO sigla (id,import_conf_id,sigla) VALUES (36,304,'ABA000');

--changeset tomascejpek:60 context:cpk
DELETE FROM download_import_conf WHERE import_conf_id in (321,325,326);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (321,'http://aleph.nkp.cz/OAI','SLK-CPK','marc21',NULL);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (325,'http://aleph.nkp.cz/OAI','KKL-CPK','marc21',NULL);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (326,'http://aleph.nkp.cz/OAI','STT-CPK','marc21',NULL);

--changeset tomascejpek:61
CREATE TABLE publisher_number (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  publisher_number     VARCHAR(255),
  order_in_record      DECIMAL(4),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);
COMMENT ON TABLE publisher_number IS 'dedup_keys: table contatining publisher numbers';
CREATE INDEX publisher_number_harvested_record_idx ON publisher_number(harvested_record_id);

--changeset tomascejpek:62 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (37, 343, 'ZLG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (38, 346, 'PRG001');

--changeset tomascejpek:63
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

--changeset tomascejpek:64 context:cpk
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
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1313,null,null,'sfx',null);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1314,'http://sfx.jib.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-MZK.xml','downloadAndImportRecordsJob','sfx',null);

--changeset tomascejpek:65 context:cpk
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1315,100,200,'sfxjibfree',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1316,101,200,'sfxjibnlk',8,false,true,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1318,107,200,'sfxjibntk',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1319,107,200,'sfxjibuochb',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1320,107,200,'sfxjibvscht',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1315,'http://sfx.jib.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding.xml','downloadAndImportRecordsJob','sfx',null);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1316,null,null,'sfxnlk',null);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1318,null,null,'sfx',null);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1319,null,null,'sfx',null);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1320,null,null,'sfx',null);

--changeset tomascejpek:66
INSERT INTO harvested_record_format(id, name) VALUES (64, 'OTHER_DICTIONARY_ENTRY');

--changeset tomascejpek:67 context:cpk
INSERT INTO library (id,name,url,catalog_url,city) VALUES (154,'TDKIV','nkp.cz','aleph.nkp.cz',null);
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (354,154,200,'tdkiv',8,false,false,true,false,'U','DictionaryLocal.groovy',false,'DictionaryMerged.groovy');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (354,'local:/data/imports/aleph.KTD','importOaiRecordsJob',null,'[^:]+:(.*)');

--changeset tomascejpek:68 context:cpk
DELETE FROM sigla WHERE id=21;
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (40, 350, 'SOG504');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (41, 353, 'OPG001');

--changeset tomascejpek:69 context:cpk
UPDATE oai_harvest_conf SET url='https://katalog.kfbz.cz/api/oai' WHERE import_conf_id=343;

--changeset tomascejpek:70 context:cpk
INSERT INTO library (id, name, url, catalog_url, city) VALUES (157, 'AGROVOC', 'http://aims.fao.org', 'http://aims.fao.org', NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (357, 157, 200, 'agrovoc', 0, false, false, false, false, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (357,NULL,NULL,'marc21',NULL);

--changeset tomascejpek:71 context:cpk
UPDATE import_conf SET filtering_enabled=true WHERE id in ('300','301','302','304','307','308','311','312','313','315','319','320','321','324','325','326','328','330','331','332','333','334','335','336','337','338','342','343','345','346','347','348','349','350','353');

--changeset tomascejpek:72
CREATE OR REPLACE VIEW dedup_record_orphaned AS
SELECT
  dr.id dedup_record_id,
  GREATEST(dr.updated, (SELECT MAX(updated) FROM harvested_record hr WHERE hr.dedup_record_id = dr.id)) orphaned
FROM
  dedup_record dr
WHERE
  NOT EXISTS(SELECT 1 FROM harvested_record hr WHERE hr.dedup_record_id = dr.id and deleted is null)
;

--changeset tomascejpek:73
ALTER TABLE import_conf ADD COLUMN item_id VARCHAR(15);

--changeset tomascejpek:74 context:cpk
UPDATE import_conf SET item_id='aleph',interception_enabled=true WHERE id in (300,304,307,313,315,321,324,325,326,330,335,337);
UPDATE import_conf SET item_id='tre',interception_enabled=true WHERE id=306;
UPDATE import_conf SET item_id='nlk',interception_enabled=true WHERE id=301;
UPDATE import_conf SET item_id='svkul',interception_enabled=true WHERE id=314;
UPDATE import_conf SET item_id='other',interception_enabled=true WHERE id in (302,308,311,312,328,334,336,338,340,343,346,350,353);

--changeset tomascejpek:75 context:cpk
UPDATE oai_harvest_conf SET url='https://www.knihovnachodov.cz/tritius/oai-provider' WHERE import_conf_id=350;

--changeset tomascejpek:76 context:cpk
INSERT INTO library (id, name, url, catalog_url, city) VALUES (156, 'KJDPB', 'https://www.kjd.pb.cz/', 'http://gw.kjd.pb.cz:8080/', 'Příbram');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (356, 156, 200, 'kjdpb', 12, false, true, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (356,'http://gw.kjd.pb.cz:8080/l.dll','CPK','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (158, 'KNIHBIB', 'https://www.lib.cas.cz/', 'https://www.lib.cas.cz/', 'Bibliography');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (358, 158, 200, 'knihbib', 11, false, true, false, false, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (358,'http://aleph.lib.cas.cz/OAI','KVO','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (159, 'UPOL', 'https://www.knihovna.upol.cz/', 'https://library.upol.cz/i2/i2.entry.cls', 'Olomouc');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (359, 159, 200, 'upol', 11, false, true, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,harvest_job_name) VALUES (359,'http://library.upol.cz/i2/i2.ws.oai.cls','UPOLCPKALL','oai_marcxml_cpk',NULL,'cosmotronHarvestJob');

--changeset tomascejpek:77
ALTER TABLE harvested_record ADD COLUMN upv_application_id VARCHAR(20);
CREATE INDEX harvested_record_upv_appl_dx ON harvested_record(upv_application_id);

--changeset tomascejpek:78
ALTER TABLE harvested_record
ADD COLUMN source_info_t VARCHAR(255),
ADD COLUMN source_info_x VARCHAR(30),
ADD COLUMN source_info_g VARCHAR(255);
CREATE INDEX harvested_record_source_info_t_idx ON harvested_record(source_info_t);
CREATE INDEX harvested_record_source_info_x_idx ON harvested_record(source_info_x);
CREATE INDEX harvested_record_source_info_g_idx ON harvested_record(source_info_g);
DROP INDEX IF EXISTS harvested_record_source_info_idx;
ALTER TABLE harvested_record DROP COLUMN source_info;

--changeset tomascejpek:79 context:cpk
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1321,138,200,'sfxjibirel',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1321,'http://sfx.jib.cz/sfxirel/cgi/public/get_file.cgi?file=institutional_holding-IREL.xml','downloadAndImportRecordsJob','sfx',null);

--changeset tomsacejpek:80 context:cpk
UPDATE oai_harvest_conf SET url='http://kutnahora.tritius.cz/tritius/oai-provider' WHERE import_conf_id=334;

--changeset tomascejpek:81 context:cpk
UPDATE oai_harvest_conf SET url='https://kutnahora.tritius.cz/tritius/oai-provider' WHERE import_conf_id=334;
UPDATE oai_harvest_conf SET url='http://katalog.kkvysociny.cz/clavius/l.dll' WHERE import_conf_id=312;

--changeset tomascejpek:82 context:cpk
INSERT INTO library (id, name, url, catalog_url, city) VALUES (160, 'CELITEBIB', 'https://www.lib.cas.cz/', 'https://www.lib.cas.cz/', 'Bibliography');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (360, 160, 200, 'celitebib', 11, false, true, false, false, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (360,'https://aleph.lib.cas.cz/OAI','UCLA','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (161, 'CVGZ', 'https://www.cvgz.cas.cz/', 'https://www.lib.cas.cz/', 'Brno');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (361, 161, 200, 'cvgz', 11, false, true, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (361,'https://aleph.lib.cas.cz/OAI','CVGZ','marc21',NULL);

--changeset tomascejpek:83 context:cpk
UPDATE oai_harvest_conf SET extract_id_regex='s/^(.*)/KKV01-$1/' WHERE import_conf_id=332;

--changeset tomascejpek:84 context:cpk
DELETE FROM download_import_conf WHERE import_conf_id=319;
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (319,'http://aleph.nkp.cz/OAI','ANL','marc21',NULL);

--changeset tomascejpek:85
ALTER TABLE cosmotron_996 DROP CONSTRAINT cosmotron_996_harvested_record_id_fkey;
ALTER TABLE cosmotron_996 ADD COLUMN parent_record_id VARCHAR(128);
ALTER TABLE cosmotron_996 ADD CONSTRAINT cosmotron_996_uniqueid UNIQUE (record_id,import_conf_id);
DROP INDEX IF EXISTS cosmotron_996_harvested_record_idx;
ALTER TABLE cosmotron_996 DROP COLUMN harvested_record_id;
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

--changeset tomascejpek:86 context:cpk
UPDATE oai_harvest_conf set extract_id_regex='s/[^:]+:[^:]+:([^\\/]+)\\/([^\\/]+)/$1_$2/' WHERE import_conf_id in (308,328,336);

--changeset tomascejpek:87 context:cpk
UPDATE oai_harvest_conf SET url='https://katalog1.kjd.pb.cz/l.dll' WHERE import_conf_id=356;

--changeset tomascejpek:88 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (42, 356, 'PBG001');
UPDATE import_conf SET item_id='other',interception_enabled=true WHERE id=356;

--changeset tomascejpek:89
DROP INDEX cosmotron_996_conf_id_parent_id_idx;
CREATE INDEX cosmotron_996_conf_id_parent_id_idx ON cosmotron_996 (parent_record_id,import_conf_id);

--changeset tomascejpek:90 context:cpk
INSERT INTO library (id, name, url, catalog_url, city) VALUES (163, 'PKJAK', 'http://npmk.cz/', 'http://katalog.npmk.cz/', 'Praha');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (363, 163, 200, 'pkjak', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (363,'https://katalog.npmk.cz/api/oai','5','marc21',NULL,'oai:(.*)');

--changeset tomascejpek:91
INSERT INTO harvested_record_format(id, name) VALUES (65, 'BLIND_AUDIO');
INSERT INTO harvested_record_format(id, name) VALUES (66, 'BLIND_BRAILLE');

--changeset tomascejpek:92 context:cpk
UPDATE import_conf SET interception_enabled=true WHERE id=360;

--changeset tomascejpek:93 context:cpk
UPDATE import_conf SET mapping_script='AdresarKnihovenLocal.groovy' WHERE id=351;

--changeset tomascejpek:94
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

--changeset tomascejpek:95 context:cpk
UPDATE oai_harvest_conf SET harvest_job_name='oaiHarvestOneByOneJob' WHERE import_conf_id in (301,342);

--changeset tomascejpek:96 context:cpk
UPDATE import_conf SET base_weight=11 WHERE id=349;
INSERT INTO library (id, name, url, catalog_url, city) VALUES (165, 'DIVABIB', 'http://www.idu.cz/cs/bibliograficke-oddeleni', 'http://vis.idu.cz/Biblio.aspx', 'Bibliography');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (365, 165, 200, 'divabib', 11, false, true, false, false, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (365,'http://vis.idu.cz:8080/biblio/api/oai','4','marc21',NULL);

--changeset tomascejpek:97 context:cpk
UPDATE import_conf SET interception_enabled=TRUE WHERE id=342;

--changeset tomascejpek:98 context:cpk
UPDATE oai_harvest_conf SET url='https://ipac.svkkl.cz/i2/i2.ws.oai.cls' WHERE import_conf_id=336;

--changeset tomascejpek:99 context:cpk
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

--changeset tomascejpek:100 context:cpk
UPDATE import_conf SET interception_enabled=TRUE WHERE id=332;

--changeset tomascejpek:101 context:cpk
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

--changeset tomascejpek:102 context:cpk
UPDATE kramerius_conf SET harvest_job_name='krameriusHarvestJob' WHERE import_conf_id=99001;

--changeset tomascejpek:103
ALTER TABLE harvested_record ADD COLUMN sigla VARCHAR(10);
CREATE INDEX harvested_record_sigla_idx ON harvested_record(sigla);

--changeset tomascejpek:104 context:cpk
UPDATE import_conf SET filtering_enabled=TRUE WHERE id in (339,99001);

--changeset tomascejpek:105 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (39, 337, 'PNA001');

--changeset tomascejpek:106
DROP TABLE authority_record;

--changeset tomascejpek:107
CREATE TABLE authority (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  authority_id         VARCHAR(20),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);
COMMENT ON TABLE authority IS 'table contatining authority ids';
CREATE INDEX authority_harvested_record_idx ON authority(harvested_record_id);
CREATE INDEX authority_idx ON authority(authority_id);

--changeset tomascejpek:108 context:cpk
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1322,137,200,'sfxjibsvkpk',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1322,'http://sfx.jib.cz/sfxirel/cgi/public/get_file.cgi?file=institutional_holding-SVKPL.xml','downloadAndImportRecordsJob','sfx',null);

--changeset tomascejpek:109 context:cpk
DELETE FROM oai_harvest_conf WHERE import_conf_id=332;
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (332,'local:/data/imports/kkkv_upd','importRecordsJob','xml','s/^(.*)/KKV01-$1/');

--changeset tomascejpek:110 context:cpk
UPDATE import_conf SET item_id='aleph' WHERE id=332;
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (43, 332, 'KVG001');

--changeset tomascejpek:111
ALTER TABLE kramerius_conf ADD COLUMN collection VARCHAR(128);

--changeset tomascejpek:112 context:cpk
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99003,130,200,'kram-knav',8,false,true,false,true,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection) VALUES (99003,'https://cdk.lib.cas.cz/search/api/v5.0','https://cdk.lib.cas.cz/solr-select-only/k4',20,'DC',null,'solr',true,'krameriusHarvestJob','vc:c4bb27af-3a51-4ac2-95c7-fd393b489e26');

--changeset tomascejpek:113 context:cpk
UPDATE download_import_conf SET url='http://sfx.jib.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-SVKPL.xml' WHERE import_conf_id=1322;

--changeset tomascejpek:114 context:cpk
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1323,104,200,'sfxjibmus',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1323,'http://sfx.jib.cz/sfxmus3/cgi/public/get_file.cgi?file=institutional_holding.xml','downloadAndImportRecordsJob','sfx',null);
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1324,104,200,'sfxjibkiv',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1324,'http://sfx.jib.cz/sfxkiv3/cgi/public/get_file.cgi?file=institutional_holding.xml','downloadAndImportRecordsJob','sfx',null);
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1325,107,200,'sfxjibtech',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1325,'http://sfx.techlib.cz/sfxlcl41/cgi/public/get_file.cgi?file=institutional_holding-NTK.xml','downloadAndImportRecordsJob','sfx',null);

--changeset tomascejpek:115
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

--changeset tomascejpek:116 context:cpk
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99010,101,200,'kram-nlk',8,false,true,false,true,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection) VALUES (99010,'https://cdk.lib.cas.cz/search/api/v5.0','https://cdk.lib.cas.cz/solr-select-only/k4',50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob','"vc:3c06120c-ffc0-4b96-b8df-80bc12e030d9"');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99011,114,200,'kram-svkul',8,false,true,false,true,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection) VALUES (99011,'https://cdk.lib.cas.cz/search/api/v5.0','https://cdk.lib.cas.cz/solr-select-only/k4',50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob','"vc:cd324f70-c034-46f1-9674-e0df4f93de86"');

--changeset tomascejpek:117
ALTER TABLE harvested_record ADD COLUMN last_harvest TIMESTAMP;
CREATE INDEX harvested_record_last_harvest_idx ON harvested_record(last_harvest);

--changeset tomascejpek:118 context:cpk
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

--changeset tomascejpek:119 context:cpk
INSERT INTO library (id, name, url, catalog_url, city) VALUES (189, 'MKBER', 'https://www.knihovnaberoun.cz/', 'https://beroun.knihovny.net/Clavius/', 'Beroun');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (389, 189, 200, 'mkber', 11, false, true, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (389,'https://beroun.knihovny.net/Clavius/l.dll','CPK','marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city) VALUES (190, 'MKMOST', 'http://www.knihovnamost.cz/', 'https://most.tritius.cz/', 'Most');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (390, 190, 200, 'mkmost', 11, false, true, false, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (390,'https://most.tritius.cz/tritius/oai-provider','CPK','marc21',NULL);

--changeset tomascejpek:120 context:cpk
UPDATE import_conf SET interception_enabled=true, item_id='other' WHERE id>=370 AND id<=390;
UPDATE import_conf SET interception_enabled=true, item_id='aleph' WHERE id=333;

--changeset tomascejpek:121 context:cpk
UPDATE oai_harvest_conf SET url='https://tritius.knihovnachodov.cz/tritius/oai-provider' WHERE import_conf_id=350;

--changeset tomascejpek:122 context:cpk
UPDATE download_import_conf SET url='http://sfx.techlib.cz/sfxlcl41/cgi/public/get_file.cgi?file=institutional_holding-NTK.xml' WHERE import_conf_id=1318;
UPDATE download_import_conf SET import_job_name='downloadAndImportRecordsJob' WHERE import_conf_id=1318;

--changeset tomascejpek:123 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (45, 371, 'BVG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (48, 374, 'HOG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (58, 384, 'ZNG001');

--changeset tomascejpek:124 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (44, 370, 'ABG503');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (51, 377, 'HKG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (52, 378, 'PIG501');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (57, 383, 'FMG502');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (60, 386, 'CHG501');

--changeset tomascejpek:125
ALTER TABLE library ADD COLUMN region VARCHAR(15);

--changeset tomascejpek:126 context:cpk
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

--changeset tomascejpek:127
ALTER TABLE obalkyknih_toc ADD updated TIMESTAMP;
ALTER TABLE obalkyknih_toc ADD last_harvest TIMESTAMP;

--changeset tomascejpek:128
CREATE INDEX ean_idx ON ean(ean);

--changeset tomascejpek:129 context:cpk
DELETE FROM oai_harvest_conf WHERE import_conf_id=344;
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (344,null,null,null,null);

--changeset tomascejpek:130
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

--changeset tomascejpek:131
ALTER TABLE import_conf ADD COLUMN metaproxy_enabled BOOLEAN DEFAULT FALSE;

--changeset tomascejpek:132 context:cpk
UPDATE import_conf SET metaproxy_enabled=TRUE WHERE id IN (300,301,304,307,308,313,314,315,316,321,325,326,328,330,335,336,337,338,343);

--changeset tomascejpek:133 context:cpk
UPDATE oai_harvest_conf SET set_spec = '3' WHERE import_conf_id = 342;
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (393, 142, 200, 'bmc', 8, false, true, true, false, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,harvest_job_name) VALUES (393,'http://oai.medvik.cz/bmc/oai','79','xml-marc',NULL,'oaiHarvestOneByOneJob');

--changeset tomascejpek:134 context:cpk
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

--changeset tomascejpek:135 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (192, 'KKDVY', 'https://www.kkdvyskov.cz/', 'https://www.library.sk/arl-vy/', 'Vyškov', 'JM');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (392, 192, 200, 'kkdvy', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (392,'https://www.library.sk/arl-vy/cs/oai/','CPK','oai_marcxml_cpk',NULL,'s/[^:]+:[^:]+:[^:]+:(.+)/VyUsCat_$1/');

--changeset tomascejpek:136 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (191, 'UZEI', 'https://www.uzei.cz/', 'https://aleph.uzei.cz/', 'Praha', 'PR');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (391, 191, 200, 'uzei', 11, false, true, true, true, 'U', 'aleph');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format) VALUES (391,null,'importRecordsJob','xml');

--changeset tomascejpek:137 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (47, 373, 'CKG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (49, 375, 'OPG503');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (54, 380, 'PEG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (55, 381, 'PIG001');

--changeset tomascejpek:138 context:cpk
UPDATE oai_harvest_conf SET url='https://ipac.kvkli.cz/arl-li/cs/oai/',set_spec='CPK',extract_id_regex='s/[^:]+:[^:]+:[^:]+:(.+)/LiUsCat_$1/' WHERE import_conf_id=308;
UPDATE oai_harvest_conf SET url='https://katalog.svkul.cz/l.dll' WHERE import_conf_id=314;

--changeset tomascejpek:139 context:cpk
UPDATE library SET url='https://www.knihovnabreclav.cz/',catalog_url='https://breclav.knihovny.net/Carmen' WHERE id=171;
UPDATE oai_harvest_conf SET url='https://aleph.knihovna-pardubice.cz/OAI',set_spec='PAG_OAI_CPK_MARC21' WHERE import_conf_id=333;

--changeset tomascejpek:140 context:cpk
UPDATE oai_harvest_conf SET set_spec='CPK_1' WHERE import_conf_id=350;

--changeset tomascejpek:141
ALTER TABLE import_conf ADD COLUMN ziskej_enabled BOOLEAN DEFAULT FALSE;

--changeset tomascejpek:142 context:cpk
UPDATE import_conf SET ziskej_enabled=TRUE WHERE id IN (300,301,302,304,306,307,308,311,312,313,314,315,316,319,321,
  324,325,326,328,330,331,332,333,334,335,336,337,338,340,342,343,346,348,349,350,353,356,358,359,360,361,362,363,364,
  365,366,367,368,369,370,371,372,373,374,375,376,377,378,379,380,381,382,383,384,385,386,387,388,389,390,391,392,393);

--changeset tomascejpek:143 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (50, 376, 'KLG002');

--changeset tomascejpek:144 context:cpk
UPDATE kramerius_conf SET query_rows=50,metadata_stream='BIBLIO_MODS',collection='"vc:c4bb27af-3a51-4ac2-95c7-fd393b489e26"' WHERE import_conf_id=99003;

--changeset tomascejpek:145 context:cpk
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script) VALUES (1326,177,200,'sfxjibmkhk',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (1326,'https://sfx.knihovny.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-KMHK.xml','downloadAndImportRecordsJob','sfx',null);

--changeset tomascejpek:146 context:cpk
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

--changeset tomascejpek:147 context:cpk
UPDATE oai_harvest_conf SET harvest_job_name='cosmotronHarvestJob' WHERE import_conf_id=392;
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (66, 392, 'VYG001');

--changeset tomascejpek:148 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (197, 'MKTRUT', 'https://www.mktrutnov.cz/', 'https://trutnov.tritius.cz/', 'Trutnov', 'KH');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, ziskej_enabled) VALUES (397, 197, 200, 'mktrut', 11, false, true, true, true, 'U', 'other', true);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (397,'https://trutnov.tritius.cz/tritius/oai-provider','CPK_1','marc21',NULL);

--changeset tomascejpek:149 context:cpk
UPDATE oai_harvest_conf SET set_spec='CPK_1' WHERE import_conf_id=388;

--changeset tomascejpek:150 context:cpk
UPDATE download_import_conf SET extract_id_regex='s/^(.*)/UZP01-$1/',url='local:/data/imports/uzp01_upd' WHERE import_conf_id=391;
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (196, 'CZPB', 'https://kas.uzei.cz/', 'https://kas.uzei.cz/', 'Bibliography', 'bibliography');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, ziskej_enabled) VALUES (396, 196, 200, 'czpb', 11, false, true, true, false, 'U', true);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format) VALUES (396,'local:/data/imports/uzp02_upd','importRecordsJob','xml');

--changeset tomascejpek:151 context:cpk
UPDATE library SET catalog_url='https://katalog.kjm.cz' WHERE id=103;
UPDATE import_conf SET base_weight=11,filtering_enabled=TRUE,interception_enabled=TRUE,item_id='other',ziskej_enabled=TRUE WHERE id=303;
UPDATE oai_harvest_conf SET url='https://katalog.kjm.cz/oai',set_spec='KJMCPKDATE',metadata_prefix='oai_marcxml_cpk',extract_id_regex='s/[^:]+:[^:]+:([^\\/]+)\\/([^\\/]+)/$1_$2/',harvest_job_name='cosmotronHarvestJob' WHERE import_conf_id=303;

--changeset tomascejpek:152 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (65, 333, 'PAG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (62, 388, 'KVG501');

--changeset tomascejpek:153 context:cpk
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

--changeset tomascejpek:154 context:cpk
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

--changeset tomascejpek:155 context:cpk
UPDATE oai_harvest_conf SET set_spec='CPK_1' WHERE import_conf_id=334;

--changeset tomascejpek:156 context:cpk
UPDATE oai_harvest_conf SET set_spec='KJMALL' WHERE import_conf_id=303;

--changeset tomascejpek:157 context:cpk
UPDATE oai_harvest_conf SET url='https://tritius.knihovnatrinec.cz/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=383;

--changeset tomascejpek:158 context:cpk
UPDATE oai_harvest_conf SET url='https://pribram.tritius.cz/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=356;

--changeset tomascejpek:159 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (209, 'MKCL', 'http://www.knihovna-cl.cz/', 'https://tritius.knihovna-cl.cz/', 'Česká Lípa', 'LI');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, ziskej_enabled) VALUES (409, 209, 200, 'mkcl', 11, false, true, true, true, 'U', 'other', true);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (409,'https://tritius.knihovna-cl.cz/tritius/oai-provider','CPK_1','marc21',NULL);

--changeset tomascejpek:160 context:cpk
UPDATE oai_harvest_conf SET set_spec='default' WHERE import_conf_id=343;

--changeset tomascejpek:161 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (210, 'MKSEM', 'https://www.knihovnasemily.cz', 'https://semily.tritius.cz/', 'Semily', 'LI');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, ziskej_enabled) VALUES (410, 210, 200, 'mksem', 11, false, true, true, true, 'U', 'other', true);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (410,'https://semily.tritius.cz/tritius/oai-provider','CPK_1','marc21',NULL);

--changeset tomascejpek:162 context:cpk
UPDATE oai_harvest_conf SET url='https://katalog.svkul.cz/tritius/oai-provider',set_spec='CPK_2' WHERE import_conf_id=314;

--changeset tomascejpek:163 context:cpk
UPDATE oai_harvest_conf SET set_spec='CPK_1' WHERE import_conf_id=390;

--changeset tomascejpek:164 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (64, 390, 'MOG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (71, 397, 'TUG001');

--changeset tomascejpek:165 context:cpk
UPDATE oai_harvest_conf set extract_id_regex='s/[^:]+:[^:]+:([^\\/]+)\\/([^\\/]+)/$1_$2/' WHERE import_conf_id=359;

--changeset tomascejpek:166 context:cpk
UPDATE oai_harvest_conf SET url='https://brandysnl.tritius.cz/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=370;

--changeset tomascejpek:167
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

--changeset tomascejpek:168 context:cpk
UPDATE oai_harvest_conf SET url='https://tritius.knihovnaprerov.cz/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=346;

--changeset tomascejpek:169 context:cpk
UPDATE oai_harvest_conf SET url='https://tritius.kkvysociny.cz/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=312;

--changeset tomascejpek:170
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

--changeset tomascejpek:171 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (212, 'MKHOL', 'https://knihovna.holesov.info/', 'https://tritius.holesov.info/', 'Holešov', 'ZL');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, ziskej_enabled) VALUES (412, 212, 200, 'mkhol', 11, false, true, true, true, 'U', 'other', true);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (412,'https://tritius.holesov.info/tritius/oai-provider','CPK_1','marc21',NULL);

--changeset tomascejpek:172 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (211, 'MKJAR', 'http://www.knihovnajaromer.cz/', 'https://jaromer.tritius.cz/', 'Jaroměř', 'KH');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, ziskej_enabled) VALUES (411, 211, 200, 'mkjar', 11, false, true, true, true, 'U', 'other', true);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (411,'https://jaromer.tritius.cz/tritius/oai-provider','CPK_1','marc21',NULL);

--changeset tomascejpek:173 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (67, 391, 'ABA009');

--changeset tomascejpek:174 context:cpk
UPDATE oai_harvest_conf SET url='https://chodov.tritius.cz/tritius/oai-provider' WHERE import_conf_id=350;

--changeset tomascejpek:175 context:cpk
UPDATE oai_harvest_conf SET url='https://tritius.knihovnachodov.cz/tritius/oai-provider' WHERE import_conf_id=350;

--changeset tomascejpek:176 context:cpk
UPDATE oai_harvest_conf SET url='https://tritius.booksy.cz/tritius/oai-provider' WHERE import_conf_id=364;

--changeset tomascejpek:177 context:cpk
UPDATE oai_harvest_conf SET url='https://katalog-usti.knihovna-uo.cz/cgi-bin/koha/oai.pl' WHERE import_conf_id=340;

--changeset tomascejpek:178 context:cpk
UPDATE oai_harvest_conf SET url='https://db.knih-ck.cz/clavius/l.dll' WHERE import_conf_id=373;

--changeset tomascejpek:179 context:cpk
UPDATE oai_harvest_conf SET url='https://katalog.knihmil.cz/LANius/l.dll' WHERE import_conf_id=378;

--changeset tomascejpek:180 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (76, 364, 'SVG001');

--changeset tomascejpek:181 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (78, 369, 'KAG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (79, 403, 'KAG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (80, 404, 'KAG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (81, 405, 'KAG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (82, 406, 'KAG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (83, 407, 'KAG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (84, 408, 'KAG001');

--changeset tomascejpek:182 context:cpk
UPDATE oai_harvest_conf SET url='https://web2.mlp.cz/cgi/oaie' WHERE import_conf_id=345;

--changeset tomascejpek:183 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (213, 'BOOKPORT', 'https://www.bookport.cz/', '', null, null);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (413, 213, 200, 'bookport', 11, false, true, true, false, 'U');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name, format) VALUES (413,'https://www.bookport.cz/marc21.xml','importRecordsJob','xml');

--changeset tomascejpek:184 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (214, 'MUNIPRESS', 'https://www.press.muni.cz/', '', null, 'ebook');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (414, 214, 200, 'munipress', 11, false, true, true, false, 'U');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name, format) VALUES (414,null,null,null);

--changeset tomascejpek:185 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (215, 'MKKLAT', 'http://www.knih-kt.cz/', 'https://klatovy.tritius.cz/', 'Klatovy', 'PL');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, ziskej_enabled) VALUES (415, 215, 200, 'mkklat', 11, false, true, true, true, 'U', 'other', true);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (415,'https://klatovy.tritius.cz/tritius/oai-provider','CPK_1','marc21',NULL);

--changeset tomascejpek:186 context:cpk
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99017,191,200,'kram-uzei',8,false,true,false,true,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection) VALUES (99017,'https://cdk.lib.cas.cz/search/api/v5.0','https://cdk.lib.cas.cz/solr-select-only/k4',50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob','"vc:91a19b3d-8271-4889-8652-6c9d5864bd1b"');

--changeset tomascejpek:187
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

--changeset tomascejpek:188 context:cpk
UPDATE download_import_conf SET url='https://muj-antikvariat.cz/assets/obalkyknih.xml' WHERE import_conf_id=500;

--changeset tomascejpek:189 context:cpk
UPDATE library SET region='ebook' WHERE id in (127,213);

--changeset tomascejpek:190
ALTER TABLE cosmotron_996 ADD COLUMN last_harvest TIMESTAMP;
CREATE INDEX cosmotron_996_last_harvest_idx ON cosmotron_996(last_harvest);

--changeset tomascejpek:191 context:cpk
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99004,104,200,'kram-nkp', 8,false,true,false,true,'U',null,true,null,null);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (99004,'http://kramerius5.nkp.cz/oaiprovider','monograph','oai_dc',NULL);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection) VALUES (99004,'https://kramerius5.nkp.cz/search/api/v5.0',null,50,'DC',null,'fedora',true,null,null);

--changeset tomascejpek:192
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

--changeset tomascejpek:193 context:cpk
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

--changeset tomascejpek:194 context:cpk
UPDATE oai_harvest_conf SET url='https://kmhk.tritius.cz/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=377;

--changeset tomascejpek:195
ALTER TABLE kramerius_conf ADD COLUMN availability_harvest_frequency CHAR(1) DEFAULT 'U';

--changeset tomascejpek:196
ALTER TABLE import_conf ADD COLUMN indexed BOOLEAN DEFAULT TRUE;

--changeset tomascejpek:197
ALTER TABLE kramerius_conf ADD COLUMN dnnt_dest_url VARCHAR(128);

--changeset tomascejpek:198 context:cpk
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

--changeset tomascejpek:199 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (85, 409, 'CLG001');

--changeset tomascejpek:200 context:cpk
DELETE FROM sigla WHERE id=54;

--changeset tomascejpek:201 context:cpk
UPDATE kramerius_conf SET dnnt_dest_url='https://ndk.cz/uuid/' WHERE import_conf_id=99004;

--changeset tomascejpek:202
CREATE TABLE ziskej_library (
  id                SERIAL,
  sigla             VARCHAR(10) NOT NULL,
  updated           TIMESTAMP NOT NULL,
  last_harvest      TIMESTAMP NOT NULL,
  CONSTRAINT ziskej_libraries_pk PRIMARY KEY(id)
);

--changeset tomascejpek:203 context:cpk
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

--changeset tomascejpek:204
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

--changeset tomascejpek:205
ALTER TABLE kram_availability ADD level DECIMAL(10);

--changeset tomascejpek:206 context:cpk
UPDATE oai_harvest_conf SET url='https://mkkl.tritius.cz/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=376;

--changeset tomascejpek:207 context:cpk
UPDATE import_conf SET ziskej_enabled=FALSE;
UPDATE import_conf SET ziskej_enabled=TRUE WHERE id IN (300,301,302,304,307,311,312,314,315,324,330,332,335,343,356,370,383,388);

--changeset tomascejpek:208 context:cpk
UPDATE oai_harvest_conf SET url='https://koha.knihovnatabor.cz/cgi-bin/koha/oai.pl',set_spec='CPK',metadata_prefix='marccpk',extract_id_regex='TAG001:(.*)' WHERE import_conf_id=311;

--changeset tomascejpek:209 context:cpk
UPDATE oai_harvest_conf SET url='https://pisek.knihovny.net/l.dll' WHERE import_conf_id=381;

--changeset tomascejpek:210 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (72, 398, 'KMG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (87, 411, 'NAG502');

--changeset tomascejpek:211 context:cpk
UPDATE import_conf SET item_id='other' WHERE id=314;
UPDATE import_conf SET item_id='dawinci' WHERE id in (301,369,403,404,405,406,407,408);
UPDATE import_conf SET item_id='koha' WHERE id in (306,340);

--changeset tomascejpek:212 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (77, 402, 'PNG001');

--changeset tomascejpek:213
ALTER TABLE download_import_conf ADD COLUMN reharvest BOOLEAN DEFAULT FALSE;

--changeset tomascejpek:214 context:cpk
UPDATE download_import_conf SET reharvest=TRUE WHERE import_conf_id IN (341,1305,1306,1307,1308,1309,1310,1311,1312,1314,1315,1318,1322,1326);

--changeset tomascejpek:215 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (218, 'MKMT', 'https://www.mkmt.cz/', 'https://katalog.mkmt.cz/', 'Moravská Třebová', 'PA');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (418, 218, 200, 'mkmt', 11, false, true, true, true, 'U', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (418,'https://koha.mkmt.cz/cgi-bin/koha/oai.pl','CPK','marccpk',NULL,'SVG503:(.*)');

--changeset tomascejpek:216 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (219, 'MKLIT', 'https://www.knihovna-litvinov.cz/', 'https://opac.knihovna-litvinov.cz/vufind/', 'Litvínov', 'US');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (419, 219, 200, 'mklit', 11, false, true, true, true, 'U', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (419,'https://opac.knihovna-litvinov.cz/cgi-bin/koha/oai.pl','CPK','marccpk',NULL,'MOG501:(.*)');

--changeset tomascejpek:217 context:cpk
UPDATE import_conf SET item_id='koha' WHERE id=311;

--changeset tomascejpek:218
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

--changeset tomascejpek:219 context:cpk
INSERT INTO fit_project VALUES (1,'FULLTEXT_ANALYSER');
INSERT INTO fit_project VALUES (2,'SEMANTIC_ENRICHMENT');
INSERT INTO fit_project VALUES (3,'CLASSIFIER');

--changeset tomascejpek:220 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (89, 415, 'KTG001');

--changeset tomascejpek:221
INSERT INTO harvested_record_format(id, name) VALUES (67, 'EBOOK');

--changeset tomascejpek:222 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (145,'MKPE','www.mlp.cz','search.mlp.cz',null,'ebook');
UPDATE import_conf SET library_id='145',is_library=FALSE WHERE id = 345;

--changeset tomascejpek:223 context:cpk
UPDATE oai_harvest_conf SET url='https://biblio.idu.cz/api/oai',set_spec='cpk' WHERE import_conf_id=365;

--changeset tomascejpek:224 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (195, 'MENDELU', 'https://mendelu.cz/', 'https://katalog.mendelu.cz/', 'Brno', 'JM');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (395, 195, 200, 'mendelu', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (395,'https://katalog.mendelu.cz/api/oai/','5','marc21',NULL);

--changeset tomascejpek:225 context:cpk
UPDATE oai_harvest_conf SET url='https://katalog.vfu.cz/api/oai/',set_spec='cpk' WHERE import_conf_id=385;

--changeset tomascejpek:226 context:cpk
UPDATE oai_harvest_conf SET url='https://katalog.knir.cz/api/oai',set_spec='cpk' WHERE import_conf_id=387;

--changeset tomascejpek:227 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (221, 'MKCHEB', 'https://www.knih-cheb.cz/', 'https://kpwin.knih-cheb.cz/', 'Cheb', 'KV');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (421, 221, 200, 'mkcheb', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (421,'https://kpwin.knih-cheb.cz/api/oai','cpk','marc21',NULL,'oai:(.*)');

--changeset tomascejpek:228 context:cpk
UPDATE oai_harvest_conf SET url='https://clavius.lib.cas.cz/katalog/l.dll',set_spec='CPK' WHERE import_conf_id=331;

--changeset tomascejpek:229 context:cpk
UPDATE oai_harvest_conf SET url='https://hodonin.tritius.cz/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=374;

--changeset tomascejpek:230 context:cpk
UPDATE import_conf SET interception_enabled='true' WHERE id=365;

--changeset tomascejpek:231 context:cpk
UPDATE oai_harvest_conf set extract_id_regex='s/[^:]+:[^:]+:[^\\/]+\\/([^\\/]+)/kjm_us_cat*$1/' WHERE import_conf_id=303;
UPDATE oai_harvest_conf set extract_id_regex='s/[^:]+:[^:]+:[^:]+:(.+)/li_us_cat*$1/' WHERE import_conf_id=308;
UPDATE oai_harvest_conf set extract_id_regex='s/[^:]+:[^:]+:[^\\/]+\\/([^\\/]+)/cbvk_us_cat*$1/' WHERE import_conf_id=328;
UPDATE oai_harvest_conf set extract_id_regex='s/[^:]+:[^:]+:[^\\/]+\\/([^\\/]+)/kl_us_cat*$1/' WHERE import_conf_id=336;
UPDATE oai_harvest_conf set extract_id_regex='s/[^:]+:[^:]+:[^\\/]+\\/([^\\/]+)/upol_us_cat*$1/' WHERE import_conf_id=359;
UPDATE oai_harvest_conf set extract_id_regex='s/[^:]+:[^:]+:[^:]+:(.+)/vy_us_cat*$1/' WHERE import_conf_id=392;

--changeset tomascejpek:232 context:cpk
DELETE FROM download_import_conf WHERE import_conf_id=332;
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (332,'https://katalog.knihovnakv.cz/tritius/oai-provider','CPK_1','marc21',NULL);
UPDATE import_conf SET item_id='tritius' WHERE id=332;

--changeset tomascejpek:233 context:cpk
UPDATE import_conf SET item_id='other' WHERE id=332;

--changeset tomascejpek:234 context:cpk
UPDATE kramerius_conf SET url='https://kramerius.mzk.cz/search/api/v5.0' WHERE import_conf_id=99001;
UPDATE kramerius_conf SET url='https://kramerius.kvkli.cz/search/api/v5.0' WHERE import_conf_id=99021;

--changeset tomascejpek:235
ALTER TABLE harvested_record ADD COLUMN loans DECIMAL(10);
ALTER TABLE harvested_record ADD COLUMN callnumber VARCHAR(100);

--changeset tomascejpek:236 context:cpk
UPDATE kramerius_conf SET availability_dest_url='https://ndk.cz/uuid/' where import_conf_id=99004;

--changeset tomascejpek:237 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (223, 'MKRIC', 'https://knihovna.ricany.cz/', 'https://tritius-knihovna.ricany.cz/', 'Říčany', 'SC');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (423, 223, 200, 'mkric', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (423,'https://tritius-knihovna.ricany.cz/tritius/oai-provider','CPK_1','marc21',NULL);

--changeset tomascejpek:238 context:cpk
UPDATE kramerius_conf SET availability_dest_url='https://kramerius.lib.cas.cz/uuid/' WHERE import_conf_id=99003;
UPDATE kramerius_conf SET availability_dest_url='https://kramerius.svkhk.cz/uuid/' WHERE import_conf_id=99014;
UPDATE kramerius_conf SET availability_dest_url='https://kramerius.svkos.cz/uuid/' WHERE import_conf_id=99019;
UPDATE kramerius_conf SET availability_dest_url='https://kramerius.kvkli.cz/uuid/' WHERE import_conf_id=99021;
UPDATE kramerius_conf SET availability_dest_url='https://kramerius.mjh.cz/uuid/' WHERE import_conf_id=99034;

--changeset tomascejpek:239 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (99047, 'KRAM-HMT', '', '', '','kramerius');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99047,99047,200,'kram-hmt',8,false,true,false,false,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99047,'http://kramerius.husitskemuzeum.cz/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'http://kramerius.husitskemuzeum.cz/search/handle/');

--changeset tomascejpek:240 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (99048, 'KRAM-NULK', '', '', '','kramerius');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99048,99048,200,'kram-nulk',8,false,true,false,false,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99048,'https://kramerius.nulk.cz/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'https://kramerius.nulk.cz/uuid/');

--changeset tomascejpek:241 context:cpk
UPDATE oai_harvest_conf SET set_spec='CPKALL' WHERE import_conf_id=303;
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (68, 303, 'BOG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (90, 418, 'SVG503');

--changeset tomascejpek:242 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (75, 401, 'KOG001');

--changeset tomascejpek:243 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (225, 'MKBOSKOVICE', 'https://www.kulturaboskovice.cz/knihovna', 'https://boskovice.tritius.cz/', 'Boskovice', 'JM');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (425, 225, 200, 'mkboskovice', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (425,'https://boskovice.tritius.cz/tritius/oai-provider','CPK_1','marc21',NULL);

--changeset tomascejpek:244 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (224, 'MKMILOVICE', 'http://milovice.knihovna.cz/', 'https://sck.tritius.cz/library/milovice', 'Milovice', 'SC');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (424, 224, 200, 'mkmilovice', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (424,'https://sck.tritius.cz/tritius/oai-provider','CPK_1','marc21',NULL);

--changeset tomascejpek:245 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (217, 'MUNI', '', '', 'Brno', 'JM');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, ziskej_enabled) VALUES (417, 217, 200, 'muni', 11, false, true, true, true, 'U', 'other', true);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (417,NULL,NULL,'marc21',NULL);

--changeset tomascejpek:246 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (226, 'KMOL', 'http://www.kmol.cz/', 'hhttps://tritius.kmol.cz/', 'Olomouc', 'OL');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (426, 226, 200, 'kmol', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (426,'https://tritius.kmol.cz/tritius/oai-provider','CPK_1','marc21',NULL);

--changeset tomascejpek:247
CREATE TABLE title_old_spelling (
  id                   SERIAL,
  key                  VARCHAR(128),
  value                VARCHAR(128)
);
CREATE INDEX title_old_spelling_key_idx ON title_old_spelling(key);

--changeset tomascejpek:248 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (216, 'CZTCPK', '', '', null, 'bibliography');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (416, 216, 200, 'cztcpk', 11, false, true, true, false, 'U');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name, format) VALUES (416,null,null,null);

--changeset tomascejpek:249 context:cpk
UPDATE import_conf SET ziskej_enabled=FALSE WHERE id=417;

--changeset tomascejpek:250 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (88, 412, 'KMG502');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (98, 425, 'BKG501');

--changeset tomascejpek:251 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (228, 'SLAVOJ', 'http://slavoj.cz/', 'https://katalog.slavoj.cz/', 'Dvůr Králové nad Labem', 'KH');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (428, 228, 200, 'slavoj', 11, false, true, true, true, 'U', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (428,'https://koha.slavoj.cz/cgi-bin/koha/oai.pl','CPK','marccpk',NULL);

--changeset tomascejpek:252 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (73, 399, 'CVG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (74, 368, 'FMG002');

--changeset tomascejpek:253
ALTER TABLE library ALTER COLUMN region TYPE VARCHAR(60);

--changeset tomascejpek:254 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (167, 'CZHISTBIB', 'https://biblio.hiu.cas.cz/', 'https://biblio.hiu.cas.cz/search', 'Bibliography', 'bibliography/HISTOGRAFBIB');
UPDATE import_conf SET library_id=167 WHERE id=367;
UPDATE library SET name='ARCHBIB',region='bibliography/HISTOGRAFBIB' WHERE id=166;

--changeset tomascejpek:255 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (229, 'MVK', 'https://www.mvk.cz/', 'https://katalog.mvk.cz/', 'Vsetín', 'ZL');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (429, 229, 200, 'mvk', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (429,'https://mvk.portaro.cz/api/oai','cpk','marc21',NULL);

--changeset tomascejpek:256 context:cpk
UPDATE library SET name='MKRICANY' WHERE id=223;
UPDATE import_conf SET id_prefix='mkricany' WHERE id=423;

--changeset tomascejpek:257 context:cpk
DELETE FROM oai_harvest_conf WHERE import_conf_id=1304;
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format) VALUES (1304,null,null,null);

--changeset tomascejpek:258 context:cpk
UPDATE kramerius_conf SET url='https://kramerius.upm.cz/search/api/v5.0',availability_dest_url='https://kramerius.upm.cz/uuid/' WHERE import_conf_id=99040;

--changeset tomascejpek:259
ALTER TABLE oai_harvest_conf ADD COLUMN url_full_harvest VARCHAR(128);
ALTER TABLE oai_harvest_conf ADD COLUMN set_spec_full_harvest VARCHAR(128);

--changeset tomascejpek:260 context:cpk
UPDATE oai_harvest_conf SET set_spec_full_harvest='CPK1' WHERE import_conf_id IN (328,336);

--changeset tomascejpek:261 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (227, 'MKTREBIC', 'http://www.knihovnatr.cz/', 'https://trebic.tritius.cz/', 'Olomouc', 'VY');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (427, 227, 200, 'mktrebic', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (427,'https://trebic.tritius.cz/tritius/oai-provider','CPKTEST_1','marc21',NULL);

--changeset tomascejpek:262 context:cpk
UPDATE library SET city='Třebíč' WHERE id=227;

--changeset tomascejpek:263 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (96, 423, 'ABG502');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (99, 426, 'OLG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (100, 427, 'TRG001');

--changeset tomascejpek:264 context:cpk
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script, generate_biblio_linker_keys) VALUES (1327,132,200,'sfxjibkkkv',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy', false);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex,reharvest) VALUES (1327,'https://sfx.knihovny.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-KKKV.xml','downloadAndImportRecordsJob','sfx',null,true);
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script, generate_biblio_linker_keys) VALUES (1328,191,200,'sfxjibuzei',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy', false);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex,reharvest) VALUES (1328,'https://sfx.knihovny.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-UZEI.xml','downloadAndImportRecordsJob','sfx',null,true);
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script, generate_biblio_linker_keys) VALUES (1329,114,200,'sfxjibsvkul',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy', false);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex,reharvest) VALUES (1329,'https://sfx.knihovny.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-SVKUL.xml','downloadAndImportRecordsJob','sfx',null,true);
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script, generate_biblio_linker_keys) VALUES (1330,112,200,'sfxjibkkvy',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy', false);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex,reharvest) VALUES (1330,'https://sfx.knihovny.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-KKVysociny.xml','downloadAndImportRecordsJob','sfx',null,true);

--changeset tomascejpek:265 context:cpk
UPDATE download_import_conf SET url='https://sfx.techlib.cz/sfxlcl41/cgi/public/get_file.cgi?file=institutional_holding-NTK.xml' WHERE import_conf_id=1318;

--changeset tomascejpek:266 context:cpk
UPDATE oai_harvest_conf SET url_full_harvest='https://roznov.portaro.cz/api/oai' WHERE import_conf_id=387;
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (61, 387, 'VSG502');

--changeset tomascejpek:267
CREATE TABLE kram_dnnt_label (
  id                    SERIAL,
  kram_availability_id INTEGER,
  label                 VARCHAR(100) NOT NULL,
  CONSTRAINT kram_dnnt_labels_pk PRIMARY KEY(id),
  FOREIGN KEY (kram_availability_id) REFERENCES kram_availability(id) ON DELETE CASCADE
);
CREATE INDEX kram_dnnt_label_availability_id_idx ON kram_dnnt_label(kram_availability_id);

--changeset tomascejpek:268 context:cpk
UPDATE kramerius_conf SET availability_dest_url='https://www.digitalniknihovna.cz/mzk/uuid/' WHERE import_conf_id=99001;
UPDATE kramerius_conf SET availability_dest_url='https://dk.uzei.cz/uzei/uuid/' WHERE import_conf_id=99017;
UPDATE kramerius_conf SET availability_source_url='https://kramerius.kr-olomoucky.cz/search/api/v5.0' WHERE import_conf_id=99012;

--changeset tomascejpek:269 context:cpk
UPDATE kramerius_conf SET url='https://cdk.lib.cas.cz/search/api/v5.0' WHERE import_conf_id IN (99019,99037);
UPDATE kramerius_conf SET url_solr='https://cdk.lib.cas.cz/solr-select-only/k4' WHERE import_conf_id IN (99019,99037);
UPDATE kramerius_conf SET collection='"vc:41f345fc-d0ad-11ea-b976-005056b593cd"' WHERE import_conf_id=99019;
UPDATE kramerius_conf SET collection='"vc:9ecedcad-aa68-4967-8d65-f938c5ce3a6b"' WHERE import_conf_id=99037;
UPDATE kramerius_conf SET availability_source_url='https://kramerius.svkos.cz/search/api/v5.0' WHERE import_conf_id=99019;
UPDATE kramerius_conf SET availability_source_url='https://library.nfa.cz/search/api/v5.0' WHERE import_conf_id=99037;

--changeset tomascejpek:270
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

--changeset tomascejpek:271 context:cpk
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

--changeset tomascejpek:272 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (237, 'MKDB', 'https://www.knihovna.dolnibousov.cz/', 'https://katalog.dolni-bousov.cz/', 'Dolní Bousov', 'SC');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (437, 237, 200, 'mkdb', 11, false, true, true, true, 'U', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (437,'https://koha.dolni-bousov.cz/cgi-bin/koha/oai.pl','CPK','marccpk',NULL,'MBG504:(.*)');

--changeset tomascejpek:273 context:cpk
UPDATE oai_harvest_conf SET url='https://koha.knihovna-litvinov.cz/cgi-bin/koha/oai.pl' WHERE import_conf_id=419;

--changeset tomascejpek:274 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (93, 419, 'MOG501');

--changeset tomascejpek:275
INSERT INTO harvested_record_format(id, name) VALUES (69, 'THESIS_BACHELOR');
INSERT INTO harvested_record_format(id, name) VALUES (70, 'THESIS_MASTER');
INSERT INTO harvested_record_format(id, name) VALUES (71, 'THESIS_ADVANCED_MASTER');
INSERT INTO harvested_record_format(id, name) VALUES (72, 'THESIS_DISSERTATION');
INSERT INTO harvested_record_format(id, name) VALUES (73, 'THESIS_HABILITATION');
INSERT INTO harvested_record_format(id, name) VALUES (74, 'THESIS_OTHER');

--changeset tomascejpek:276
INSERT INTO harvested_record_format(id, name) VALUES (68, 'BOARD_GAMES');

--changeset tomascejpek:277
CREATE TABLE caslin_links (
  id                SERIAL,
  sigla             VARCHAR(10) NOT NULL,
  url               VARCHAR (500) NOT NULL,
  updated           TIMESTAMP NOT NULL,
  last_harvest      TIMESTAMP NOT NULL,
  CONSTRAINT caslin_links_pk PRIMARY KEY(id)
);
CREATE INDEX caslin_links_sigla_idx ON caslin_links(sigla);

--changeset tomascejpek:278
ALTER TABLE import_conf ADD COLUMN mappings996 VARCHAR(20);

--changeset tomascejpek:279 context:cpk
UPDATE import_conf SET mappings996='aleph' WHERE id IN (300,304,307,313,315,321,324,325,326,330,333,335,337,361,391);
UPDATE import_conf SET mappings996='tritius' WHERE id IN (312,314,332,334,346,350,353,356,364,368,370,371,373,374,375,376,377,378,380,381,383,384,386,388,390,397,398,399,401,402,409,410,411,412,415,423,424,425,426,427,432);
UPDATE import_conf SET mappings996='koha' WHERE id IN (306,311,340,418,419,428,430,431,433,434,437);
UPDATE import_conf SET mappings996='caslin' WHERE id IN (316);
UPDATE import_conf SET mappings996='dawinci' WHERE id IN (301,342,369,393,403,404,405,406,407,408);

--changeset tomascejpek:280 context:cpk
UPDATE kramerius_conf SET url='https://kramerius.knihovna-pardubice.cz/search/api/v5.0',availability_dest_url='https://kramerius.knihovna-pardubice.cz/uuid/' WHERE import_conf_id=99026;

--changeset tomascejpek:281 context:cpk
UPDATE oai_harvest_conf SET url='https://knihovnaml.tritius.cz/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=386;

--changeset tomascejpek:282
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

--changeset tomascejpek:283
CREATE TABLE import_conf_mapping_field (
  import_conf_id         DECIMAL(10) PRIMARY KEY,
  parent_import_conf_id  DECIMAL(10) NOT NULL,
  mapping                VARCHAR(100),
  CONSTRAINT import_conf_mapping_field_import_conf_fk        FOREIGN KEY (import_conf_id) REFERENCES import_conf(id),
  CONSTRAINT import_conf_mapping_field_parent_import_conf_fk FOREIGN KEY (parent_import_conf_id) REFERENCES import_conf(id)
);
CREATE INDEX import_conf_mapping_field_parent_id_idx ON import_conf_mapping_field(parent_import_conf_id);

--changeset tomascejpek:284 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (220, 'USDBIBL', '', '', null, 'bibliography');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (420, 220, 200, 'usdbibl', 11, false, true, true, false, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (420,NULL,NULL,'marc21',NULL);
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (222, 'KNAVALL', '', '', null, null);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, generate_dedup_keys, generate_biblio_linker_keys, indexed) VALUES (422, 222, 200, 'knavall', 11, false, true, true, true, 'U', false, false, false);

--changeset tomascejpek:285 context:cpk
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (422,'https://aleph.lib.cas.cz/OAI','KNA01','marc21',NULL);

--changeset tomascejpek:286 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (238, 'KAND', 'https://www.narodni-divadlo.cz/', 'https://www.archivndknihovna.cz/', 'Praha', 'PR');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (438, 238, 200, 'kand', 11, false, true, true, true, 'U', 'koha', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (438,'https://koha.archivndknihovna.cz/cgi-bin/koha/oai.pl','CPK','marccpk',NULL,'ABE309:(.*)');

--changeset tomascejpek:287 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (239, 'MKVALMEZ', 'https://www.mekvalmez.cz/', 'https://katalog.mekvalmez.cz/', 'Valašské Meziříčí', 'ZL');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (439, 239, 200, 'mkvalmez', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (439,'https://katalog.mekvalmez.cz/api/oai','cpk','marc21',NULL,'oai:(.*)');

--changeset tomascejpek:288 context:cpk
UPDATE import_conf SET item_id='aleph',interception_enabled=true WHERE id=361;
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (69, 361, 'BOB026');
INSERT INTO import_conf_mapping_field (import_conf_id,parent_import_conf_id,mapping) VALUES (361,422,'599$aCPK-UVGZ');

--changeset tomascejpek:289 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (104, 431, 'KAG505');

--changeset tomascejpek:290 context:cpk
UPDATE kramerius_conf SET url='https://cdk.lib.cas.cz/search/api/v5.0',url_solr='https://cdk.lib.cas.cz/solr-select-only/k4',collection='"vc:5af0d476-df3d-4709-8f28-5c33d9d3f4b5"',availability_source_url='http://kramerius.kfbz.cz/search/api/v5.0' WHERE import_conf_id=99023;

--changeset tomascejpek:291 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (240, 'QUEER', 'https://www.stud.cz/informace/queer-knihovna.html', 'https://katalog.queerknihovna.cz/', 'Brno', 'JM');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (440, 240, 200, 'queer', 11, false, true, true, true, 'U', 'koha', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (440,'https://koha.queerknihovna.cz/cgi-bin/koha/oai.pl','CPK','marccpk',NULL,'BOE035:(.*)');

--changeset tomascejpek:292 context:cpk
UPDATE oai_harvest_conf SET url='https://tritius.knih-pe.cz/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=380;

--changeset tomascejpek:293 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (92, 421, 'CHG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (103, 430, 'CRG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (112, 380, 'PEG001');

--changeset tomascejpek:294 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (105, 432, 'JHG001');

--changeset tomascejpek:295 context:cpk
DELETE FROM sigla WHERE id IN (49,52);

--changeset tomascejpek:296
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

--changeset tomascejpek:297 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (241, 'MKJICIN', 'https://knihovna.jicin.cz/', 'https://katalog.knihovna.jicin.cz/', 'Jičín', 'KH');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (441, 241, 200, 'mkjicin', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (441,'https://katalog.knihovna.jicin.cz/api/oai','cpk','marc21',NULL);

--changeset tomascejpek:298 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (242, 'MKFPR', 'https://www.knihovnafrenstat.cz/', 'https://katalog.knihovnafrenstat.cz/', 'Frenštát pod Radhoštěm', 'MS');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (442, 242, 200, 'mkfpr', 11, false, true, true, true, 'U', 'koha', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (442,'https://koha.knihovnafrenstat.cz/cgi-bin/koha/oai.pl','CPK','marccpk',NULL,'NJG502:(.*)');

--changeset tomascejpek:299 context:cpk
UPDATE library SET catalog_url='https://katalog.knihovnaberoun.cz/' WHERE id=189;
UPDATE import_conf SET item_id='koha',mappings996='koha' WHERE id=389;
UPDATE oai_harvest_conf SET url='https://koha.knihovnaberoun.cz/cgi-bin/koha/oai.pl',set_spec='CPK',metadata_prefix='marccpk',extract_id_regex='BEG001:(.*)' WHERE import_conf_id=389;

--changeset tomascejpek:300 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (243, 'MKKNO', 'https://biblio.cz/', 'https://katalog.biblio.cz/', 'Kostelec nad Orlicí', 'KH');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (443, 243, 200, 'mkkno', 11, false, true, true, true, 'U', 'koha', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (443,'https://koha-katalog.biblio.cz/cgi-bin/koha/oai.pl','CPK','marccpk',NULL,'RKG503:(.*)');

--changeset tomascejpek:301 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (244, 'AMBIS', 'https://www.ambis.cz/', 'https://ambis.tritius.cz/', 'Praha', 'PR');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (444, 244, 200, 'ambis', 11, false, true, true, true, 'U', 'other', 'tritius');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (444,'https://ambis.tritius.cz/tritius/oai-provider','CPK_1','marc21',NULL);

--changeset tomascejpek:302 context:cpk
UPDATE import_conf SET mappings996='tritius' WHERE id=379;
UPDATE oai_harvest_conf SET url='https://orlova.knihovny.net/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=379;

--changeset tomascejpek:303 context:cpk
UPDATE oai_harvest_conf SET url='https://baze.knihovnazn.cz/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=384;

--changeset tomascejpek:304 context:cpk
UPDATE kramerius_conf SET url='https://kramerius.kkvysociny.cz/search/api/v5.0',availability_dest_url='https://kramerius.kkvysociny.cz/uuid/' WHERE import_conf_id=99025;

--changeset tomascejpek:305 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (108, 437, 'MBG504');

--changeset tomascejpek:306 context:cpk
INSERT INTO import_conf_mapping_field (import_conf_id,parent_import_conf_id,mapping) VALUES (349,422,'599$aCPK-UDUBIBL');
INSERT INTO import_conf_mapping_field (import_conf_id,parent_import_conf_id,mapping) VALUES (420,422,'599$aCPK-USDBIBL');
UPDATE import_conf SET interception_enabled=TRUE WHERE id=349;

--changeset tomascejpek:307 context:cpk
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, generate_dedup_keys) VALUES (445, 114, 200, 'bookport', 11, false, true, true, true, 'U', false);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format) VALUES (445,null,null,null);

--changeset tomascejpek:308 context:cpk
UPDATE oai_harvest_conf SET url='https://milovice.tritius.cz/tritius/oai-provider' WHERE import_conf_id=424;
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (97, 424, 'NBG505');

--changeset tomascejpek:309 context:cpk
UPDATE download_import_conf SET url='https://sfx.knihovny.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-NLK.xml',import_job_name='downloadAndImportRecordsJob',format='sfx',reharvest=true WHERE import_conf_id=1316;
UPDATE import_conf SET filtering_enabled=FALSE WHERE id=1316;

--changeset tomascejpek:310 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (53, 379, 'KAG502');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (101, 428, 'TUG504');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (107, 434, 'UOG502');

--changeset tomascejpek:311 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (246, 'MKNBK', 'http://knihovna-nbk.cz/', 'https://tritius.knihovna-nbk.cz/', 'Nymburk', 'SC');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (446, 246, 200, 'mknbk', 11, false, true, true, true, 'U', 'other', 'tritius');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (446,'https://tritius.knihovna-nbk.cz/tritius/oai-provider','CPK_1','marc21',NULL);

--changeset tomascejpek:312 context:cpk
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script, generate_biblio_linker_keys) VALUES (1331,136,200,'sfxjibsvkkl',8,false,false,false,true,'U','SfxMarcLocal.groovy',false,'SfxMarc.groovy', false);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex,reharvest) VALUES (1331,'https://sfx.knihovny.cz/sfxlcl3/cgi/public/get_file.cgi?file=institutional_holding-SVKKL.xml','downloadAndImportRecordsJob','sfx',null,true);

--changeset tomascejpek:313 context:cpk
DELETE FROM oai_harvest_conf WHERE import_conf_id=99004;
UPDATE kramerius_conf SET metadata_stream='BIBLIO_MODS' WHERE import_conf_id=99004;

--changeset tomascejpek:314 context:cpk
UPDATE kramerius_conf SET availability_source_url='https://kramerius.svkhk.cz/search/api/v5.0' WHERE import_conf_id=99014;

--changeset tomascejpek:315 context:cpk
UPDATE oai_harvest_conf SET extract_id_regex=NULL WHERE import_conf_id IN (421,439);

--changeset tomascejpek:316 context:cpk
INSERT INTO import_conf_mapping_field (import_conf_id,parent_import_conf_id,mapping) VALUES (360,422,'599$aCLB-CPK');

--changeset tomascejpek:317 context:cpk
UPDATE oai_harvest_conf set extract_id_regex='s/[^:]+:[^:]+:[^\\/]+\\/([^\\/]+)/KjmUsCat*$1/' WHERE import_conf_id=303;
UPDATE oai_harvest_conf set extract_id_regex='s/[^:]+:[^:]+:[^:]+:(.+)/LiUsCat*$1/' WHERE import_conf_id=308;
UPDATE oai_harvest_conf set extract_id_regex='s/[^:]+:[^:]+:[^\\/]+\\/([^\\/]+)/CbvkUsCat*$1/' WHERE import_conf_id=328;
UPDATE oai_harvest_conf set extract_id_regex='s/[^:]+:[^:]+:[^\\/]+\\/([^\\/]+)/KlUsCat*$1/' WHERE import_conf_id=336;
UPDATE oai_harvest_conf set extract_id_regex='s/[^:]+:[^:]+:[^\\/]+\\/([^\\/]+)/UpolUsCat*$1/' WHERE import_conf_id=359;
UPDATE oai_harvest_conf set extract_id_regex='s/[^:]+:[^:]+:[^:]+:(.+)/VyUsCat*$1/' WHERE import_conf_id=392;

--changeset tomascejpek:318 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (247, 'MKHORICE', 'https://knihovna.horice.org/', 'https://kpwin.horice.org/', 'Hořice', 'KH');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (447, 247, 200, 'mkhorice', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (447,'https://kpwin.horice.org/api/oai/','cpk','marc21',NULL);

--changeset tomascejpek:319 context:cpk
UPDATE oai_harvest_conf SET url='https://katalog.mvk.cz/api/oai' WHERE import_conf_id=429;

--changeset tomascejpek:320 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (106, 433, 'KAG503');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (109, 438, 'ABE309');

--changeset tomascejpek:321 context:cpk
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, generate_dedup_keys) VALUES (448, 130, 200, 'bookport', 11, false, true, true, true, 'U', false);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format) VALUES (448,'https://bookport.cz/marc21-12415.xml','downloadAndImportRecordsJob','xml');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, generate_dedup_keys) VALUES (449, 101, 200, 'bookport', 11, false, true, true, true, 'U', false);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format) VALUES (449,'https://bookport.cz/marc21-12416.xml','downloadAndImportRecordsJob','xml');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, generate_dedup_keys) VALUES (450, 113, 200, 'bookport', 11, false, true, true, true, 'U', false);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format) VALUES (450,'https://bookport.cz/marc21-12427.xml','downloadAndImportRecordsJob','xml');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, generate_dedup_keys) VALUES (451, 115, 200, 'bookport', 11, false, true, true, true, 'U', false);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format) VALUES (451,'https://bookport.cz/marc21-12417.xml','downloadAndImportRecordsJob','xml');

--changeset tomascejpek:322 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (252, 'MKSTRAK', 'https://www.knih-st.cz/', 'https://katalog.knih-st.cz/', 'Strakonice', 'JC');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (452, 252, 200, 'mkstrak', 11, false, true, true, true, 'U', 'koha', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (452,'https://koha.knih-st.cz/cgi-bin/koha/oai.pl','CPK','marccpk',NULL);

--changeset tomascejpek:323 context:cpk
UPDATE import_conf set mappings996 = 'tritius' where id = 372;
UPDATE oai_harvest_conf SET url='https://online.knihovnacaslav.cz/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=372;
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (453, 172, 200, 'cmuz', 11, false, true, true, true, 'U', 'other', 'tritius');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (453,'https://online.knihovnacaslav.cz/tritius/oai-provider','CPK_101','marc21',NULL);

--changeset tomascejpek:324 context:cpk
DELETE FROM oai_harvest_conf WHERE import_conf_id=453;
DELETE FROM import_conf WHERE id=453;
UPDATE library SET name='MKCASLAV',catalog_url='https://online.knihovnacaslav.cz/' WHERE id=172;

--changeset tomascejpek:325 context:cpk
UPDATE library SET region='JC' WHERE id=232;

--changeset tomascejpek:326 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (111, 440, 'BOE035');

--changeset tomascejpek:327 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (253, 'GEOBIBLINE', 'https://cuni.cz/', 'https://cuni.primo.exlibrisgroup.com/discovery/search?vid=420CKIS_INST:UKAZ&lang=cs', 'Bibliography', 'bibliography');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (453, 253, 200, 'geobibline', 11, false, true, true, false, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (453,'https://cuni.alma.exlibrisgroup.com/view/oai/420CKIS_INST/request','OAI_GEO','marc21',NULL);

--changeset tomascejpek:328 context:cpk
UPDATE kramerius_conf SET url='https://k4.kr-karlovarsky.cz/search/api/v5.0' WHERE import_conf_id=99020;

--changeset tomascejpek:329 context:cpk
UPDATE oai_harvest_conf SET extract_id_regex='STG001:(.*)' WHERE import_conf_id=452;

--changeset tomascejpek:330 context:cpk
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, generate_dedup_keys) VALUES (454, 102, 200, 'bookport', 11, false, true, true, true, 'U', false);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format) VALUES (454,'https://bookport.cz/marc21-8077.xml','downloadAndImportRecordsJob','xml');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, generate_dedup_keys) VALUES (455, 102, 200, 'bookport', 11, false, true, true, true, 'U', false);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format) VALUES (455,'https://bookport.cz/marc21-9561.xml','downloadAndImportRecordsJob','xml');

--changeset tomascejpek:331 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (110, 439, 'VSG501');

--changeset tomascejpek:332 context:cpk
UPDATE library SET name='CLP' WHERE id=216;
UPDATE import_conf SET id_prefix='clp' WHERE id=416;

--changeset tomascejpek:333 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (258, 'RSL', '', '', null, 'bibliography');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (458, 258, 200, 'rsl', 11, false, true, true, false, 'U');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format) VALUES (458,null,null,null);

--changeset tomascejpek:334
ALTER TABLE harvested_record ADD COLUMN palmknihy_id VARCHAR(20);
CREATE INDEX harvested_record_palmknihy_id_idx ON harvested_record(palmknihy_id);

--changeset tomascejpek:335 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (257, 'PALMKNIHY', 'https://www.palmknihy.cz/', '', null, 'ebook');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (457, 257, 200, 'palmknihy', 11, false, true, true, false, 'U');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format) VALUES (457,null,null,null);

--changeset tomascejpek:336 context:cpk
UPDATE import_conf SET id_prefix='mkcaslav' WHERE id=372;
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (46, 372, 'KHG505');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (102, 429, 'VSG001');

--changeset tomascejpek:337
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

--changeset tomascejpek:338 context:cpk
UPDATE import_conf SET generate_dedup_keys=false,generate_biblio_linker_keys=false WHERE id=457;

--changeset tomascejpek:339 context:cpk
UPDATE download_import_conf SET url='http://ereading.cz/xml/xml_rent.xml',import_job_name='importPalmknihyJob',format='palmknihy',reharvest=true WHERE import_conf_id=457;

--changeset tomascejpek:340 context:cpk
UPDATE oai_harvest_conf SET url='https://nacr.kpsys.cz/api/oai' WHERE import_conf_id=367;

--changeset tomascejpek:341 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (259, 'MKJIHLAVA', 'https://jihlava.tritius.cz/tritius/oai-provider', 'https://jihlava.tritius.cz/', 'Jihlava', 'VY');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (459, 259, 200, 'mkjihlava', 11, false, true, true, true, 'U', 'other', 'tritius');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (459,'https://jihlava.tritius.cz/tritius/oai-provider','CPK_1','marc21',NULL);

--changeset tomascejpek:342 context:cpk
UPDATE kramerius_conf SET dnnt_dest_url='https://kramerius.svkhk.cz/uuid/' WHERE import_conf_id=99014;

--changeset tomascejpek:343 context:cpk
UPDATE kramerius_conf SET url='https://kramerius.mjh.cz/search/api/v5.0' WHERE import_conf_id=99034;
UPDATE kramerius_conf SET availability_dest_url='https://k4.kr-karlovarsky.cz/search/handle/' WHERE import_conf_id=99020;
UPDATE kramerius_conf SET availability_dest_url='https://kramerius.techlib.cz/kramerius-web-client/uuid/' WHERE import_conf_id=99016;

--changeset tomascejpek:344 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (116, 446, 'NBG001');

--changeset tomascejpek:345
ALTER TABLE oai_harvest_conf ADD COLUMN ictx VARCHAR(128);
ALTER TABLE oai_harvest_conf ADD COLUMN op VARCHAR(128);

--changeset tomascejpek:346 context:cpk
UPDATE import_conf SET item_id='other',interception_enabled=true WHERE id=359;
UPDATE oai_harvest_conf SET url='https://library.upol.cz/i2/i2.entry.cls',set_spec='UPOLCPK',extract_id_regex='s/[^:]+:[^:]+:[^:]+:(.+)/UpolUsCat*$1/',ictx='upol',op='oai' WHERE import_conf_id=359;

--changeset tomascejpek:347 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (95, 359, 'OLD012');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (120, 459, 'JIG001');

--changeset tomascejpek:348 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (260, 'NACR', 'https://www.nacr.cz/', 'https://knihovna.nacr.cz/', 'Praha', 'PR');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (460, 260, 200, 'nacr', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (460,'https://knihovna.nacr.cz/api/oai','cpk','marc21',NULL);

--changeset tomascejpek:349
ALTER TABLE import_conf ADD COLUMN catalog_serial_link BOOLEAN DEFAULT FALSE;

--changeset tomascejpek:350 context:cpk
UPDATE oai_harvest_conf SET url='https://nacr.kpsys.cz/api/oai' WHERE import_conf_id=366;

--changeset tomascejpek:351 context:cpk
UPDATE import_conf SET catalog_serial_link=TRUE WHERE id IN (308,328,336,343,359,387,392,421,429,439,441,447,460);

--changeset tomascejpek:352 context:cpk
UPDATE oai_harvest_conf SET url='https://biblio.hiu.cas.cz/api/oai' WHERE import_conf_id=367;

--changeset tomascejpek:353 context:cpk
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, generate_dedup_keys, generate_biblio_linker_keys, indexed) VALUES (461, 114, 200, 'svkul', 11, false, true, true, true, 'U', false, false, false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (461,'https://tritius.svkul.cz/tritius/oai-provider','PLM','marc21',NULL);

--changeset tomascejpek:354 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (113, 441, 'JCG001');

--changeset tomascejpek:355 context:cpk
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, generate_dedup_keys, generate_biblio_linker_keys, indexed) VALUES (462, 104, 200, 'nkc-ebook', 11, false, true, true, true, 'U', true, false, false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (462,'https://aleph.nkp.cz/OAI','NKC-EBOOK','marc21',NULL);

--changeset tomascejpek:356 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (256, 'UDUMUKN', 'https://www.udu.cas.cz/cz/knihovny/muzikologicka-knihovna', 'https://aleph.lib.cas.cz/', 'Praha', 'PR');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (456, 256, 200, 'udumukn', 11, false, true, true, true, 'U');
INSERT INTO import_conf_mapping_field (import_conf_id,parent_import_conf_id,mapping) VALUES (456,422,'599$aCPK-UDUMUKN');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (119, 456, 'ABB045');

--changeset tomascejpek:357 context:cpk
UPDATE import_conf SET item_id='aleph',mappings996='aleph' WHERE id=456;

--changeset tomascejpek:358 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (263, 'MKBEN', 'https://www.knihovna-benesov.cz/', 'https://benesov.tritius.cz/', 'Benešov', 'SC');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (463, 263, 200, 'mkber', 11, false, true, true, true, 'U', 'other', 'tritius');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (463,'https://benesov.tritius.cz/tritius/oai-provider','CPK_1','marc21',NULL);

--changeset tomascejpek:359 context:cpk
UPDATE import_conf SET id_prefix='mkben' WHERE id=463;

--changeset tomascejpek:360 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (123, 375, 'OPG503');

--changeset tomascejpek:361
ALTER TABLE caslin_links ADD hardcoded_url VARCHAR (100);

--changeset tomascejpek:362 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (287, 'CNS', 'https://www.narodopisnaspolecnost.cz/', 'https://cns.tritius.cz/', 'Bibliography', 'bibliography');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (487, 287, 200, 'cns', 11, false, true, true, false, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (487,'https://cns.tritius.cz/tritius/oai-provider','CPK_1','marc21',NULL);

--changeset tomascejpek:363 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (288, 'MKTURNOV', 'https://knihovna.turnov.cz/', 'https://turnov-katalog.koha-system.cz/', 'Turnov', 'LI');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (488, 288, 200, 'mkturnov', 11, false, true, true, true, 'U', 'koha', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (488,'https://turnov-opac.koha-system.cz/cgi-bin/koha/oai.pl','cpk','cpk',NULL);

--changeset tomascejpek:364
INSERT INTO format(format, description) VALUES('cpk', 'MARC21 XML');

--changeset tomascejpek:365
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

--changeset tomascejpek:366 context:cpk
UPDATE oai_harvest_conf SET extract_id_regex=null WHERE import_conf_id=363;

--changeset tomascejpek:367 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (94, 363, 'ABE356');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (121, 460, 'ABE343');

--changeset tomascejpek:368 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (148, 363, 'ABA012');

--changeset tomascejpek:369 context:cpk
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, generate_dedup_keys) VALUES (489, 100, 200, 'bookport', 11, false, true, true, true, 'U', false);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format) VALUES (489,'https://bookport.cz/marc21-15554.xml','downloadAndImportRecordsJob','xml');

--changeset tomascejpek:370 context:cpk
UPDATE oai_harvest_conf SET extract_id_regex='SMG506:(.*)' WHERE import_conf_id=488;
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (147, 488, 'SMG506');

--changeset tomascejpek:371 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (290, 'MKNERATOVICE', 'https://www.knihovnaneratovice.cz/', 'https://katalog.knihovnaneratovice.cz/', 'Neratovice', 'SC');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (490, 290, 200, 'mkneratovice', 11, false, true, true, true, 'U', 'koha', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (490,'https://koha.knihovnaneratovice.cz/cgi-bin/koha/oai.pl','CPK','marccpk',NULL,'MEG502:(.*)');

--changeset tomascejpek:372 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (86, 410, 'SMG004');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (122, 463, 'BNG001');

--changeset tomascejpek:373 context:cpk
DELETE FROM download_import_conf WHERE import_conf_id in (351);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (351,'https://aleph.nkp.cz/OAI','ADR','marc21',NULL,'[^:]+:[^:]+:ADR10-(.*)');

--changeset tomascejpek:374
ALTER TABLE import_conf ADD COLUMN ziskej_edd_enabled BOOLEAN DEFAULT FALSE;

--changeset tomascejpek:375 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (286, 'MKRYMAROV', 'https://knihovnarymarov.cz/', 'https://katalog.knihovnarymarov.cz', 'Rýmařov', 'MS');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (486, 286, 200, 'mkrymarov', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (486,'https://katalog.knihovnarymarov.cz/api/oai','cpk','marc21',NULL);

--changeset tomascejpek:376 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (291, 'MKROKYCANY', 'https://www.rokyknih.cz/', 'https://katalog.rokyknih.cz/', 'Rokycany', 'PL');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (491, 291, 200, 'mkrokycany', 11, false, true, true, true, 'U', 'koha', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (491,'https://koha.rokyknih.cz/cgi-bin/koha/oai.pl','CPK','marccpk',NULL,'ROG001:(.*)');

--changeset tomascejpek:377 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (99049, 'KRAM-ROZHLAS', '', '', '','kramerius');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99049,99049,200,'kram-rozhlas',8,false,true,false,false,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99049,'https://kramerius.rozhlas.cz/search/api/v5.0',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'https://kramerius.rozhlas.cz/uuid/');

--changeset tomascejpek:378 context:cpk
UPDATE import_conf SET mapping_script='LocalMzk.groovy,HarvestedRecordBaseMarc.groovy' WHERE id in (300,320,324);

--changeset tomascejpek:379 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (118, 452, 'STG001');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (149, 490, 'MEG502');

--changeset tomascejpek:380 context:cpk
UPDATE import_conf SET item_id='koha',mappings996='koha' WHERE id=371;
UPDATE oai_harvest_conf SET url='https://koha.knihovnabreclav.cz/cgi-bin/koha/oai.pl',metadata_prefix='marccpk',extract_id_regex='BVG001:(.*)' WHERE import_conf_id=371;

--changeset tomascejpek:381 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (293, 'MKKLIMKOVICE', 'https://knihovna.mesto-klimkovice.cz/', 'https://klimkovice.tritius.cz/', 'Klimkovice', 'MS');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (493, 293, 200, 'mkklimkovice', 11, false, true, true, true, 'U', 'other', 'tritius');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (493,'https://klimkovice.tritius.cz/tritius/oai-provider','CPK_1','marc21',NULL);

--changeset tomascejpek:382
ALTER TABLE sigla_all ADD COLUMN ziskej_edd BOOLEAN DEFAULT FALSE;
CREATE INDEX sigla_all_ziskej_edd_idx ON sigla_all(ziskej_edd);

--changeset tomascejpek:383 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (294, 'MKDOBRA', 'https://knihovna-dobra.cz/', 'https://katalog.knihovna-dobra.cz/', 'Dobrá', 'MS');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (494, 294, 200, 'mkdobra', 11, false, true, true, true, 'U', 'koha', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (494,'https://koha.knihovna-dobra.cz/cgi-bin/koha/oai.pl','CPK','marccpk',NULL);

--changeset tomascejpek:384 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (114, 442, 'NJG502');

--changeset tomascejpek:385 context:cpk
DELETE FROM oai_harvest_conf WHERE import_conf_id=351;
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,extract_id_regex) VALUES (351,'local:/data/imports/aleph.ADR','importOaiRecordsJob',null,'[^:]+:(.*)');

--changeset tomascejpek:386 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (99050, 'KRAM-SNK', '', '', '','kramerius');
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled,filtering_enabled,interception_enabled,is_library,harvest_frequency,mapping_script,generate_dedup_keys,mapping_dedup_script,item_id) VALUES (99050,99050,200,'kram-snk',8,false,true,false,false,'U',null,true,null,null);
INSERT INTO kramerius_conf (import_conf_id,url,url_solr,query_rows,metadata_stream,auth_token,fulltext_harvest_type,download_private_fulltexts,harvest_job_name,collection,availability_source_url,availability_dest_url) VALUES (99050,'https://dikda.snk.sk/',null,50,'BIBLIO_MODS',null,'solr',true,'krameriusHarvestJob',null,null,'https://dikda.snk.sk/uuid/');
UPDATE kramerius_conf SET url=REPLACE(url,'search/api/v5.0', '');
UPDATE kramerius_conf SET availability_source_url=REPLACE(availability_source_url,'search/api/v5.0', '');
UPDATE kramerius_conf SET url='https://kramerius.svkpk.cz/',availability_dest_url='https://kramerius.svkpk.cz/uuid/' WHERE import_conf_id=99022;

--changeset tomascejpek:387 context:cpk
UPDATE kramerius_conf SET url='https://kramerius.lib.cas.cz/',collection=null,availability_source_url=null WHERE import_conf_id=99003;
UPDATE kramerius_conf SET url='https://kramerius.svkul.cz/',collection=null,availability_source_url=null WHERE import_conf_id=99011;
UPDATE kramerius_conf SET url='https://kramerius.svkhk.cz/',collection=null,availability_source_url=null WHERE import_conf_id=99014;
UPDATE kramerius_conf SET url='https://kramerius4.mlp.cz/',collection=null,availability_source_url=null WHERE import_conf_id=99015;
UPDATE kramerius_conf SET url='https://kramerius.techlib.cz/',collection=null,availability_source_url=null WHERE import_conf_id=99016;
UPDATE kramerius_conf SET url='https://kramerius.uzei.cz/',collection=null,availability_source_url=null WHERE import_conf_id=99017;
UPDATE kramerius_conf SET url='https://kramerius.svkos.cz/',collection=null,availability_source_url=null WHERE import_conf_id=99019;
UPDATE kramerius_conf SET url='https://library.nfa.cz/',collection=null,availability_source_url=null WHERE import_conf_id=99037;
UPDATE kramerius_conf SET url='https://kramerius.kfbz.cz/',collection=null,availability_source_url=null,availability_dest_url='https://kramerius.kfbz.cz/uuid/' WHERE import_conf_id=99023;

--changeset tomascejpek:388
ALTER TABLE sigla_all ADD COLUMN ziskej_mvs_sigla VARCHAR(10);
CREATE INDEX sigla_all_ziskej_mvs_sigla_idx ON sigla_all(ziskej_mvs_sigla);
ALTER TABLE sigla_all ADD COLUMN ziskej_edd_sigla VARCHAR(10);
CREATE INDEX sigla_all_ziskej_edd_sigla_idx ON sigla_all(ziskej_edd_sigla);

--changeset tomascejpek:389
ALTER TABLE kramerius_conf ADD COLUMN fulltext_version VARCHAR(20);

--changeset tomascejpek:390 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (152, 493, 'NJG508');

--changeset tomascejpek:391 context:cpk
UPDATE library SET region='bibliography/CGS' WHERE id in (235,236);

--changeset tomascejpek:392 context:cpk
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (498, 179, 200, 'mkor', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (498,'https://orlova.knihovny.net/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (499, 156, 200, 'kjdpb', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (499,'https://pribram.tritius.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (501, 112, 200, 'kkvy', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (501,'https://tritius.kkvysociny.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (502, 146, 200, 'mkpr', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (502,'https://tritius.knihovnaprerov.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (503, 183, 200, 'mktri', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (503,'https://tritius.knihovnatrinec.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (504, 184, 200, 'mkzn', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (504,'https://baze.knihovnazn.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (505, 170, 200, 'knep', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (505,'https://brandysnl.tritius.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (506, 174, 200, 'mkhod', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (506,'https://hodonin.tritius.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (507, 186, 200, 'mkml', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (507,'https://knihovnaml.tritius.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (508, 134, 200, 'mkkh', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (508,'https://kutnahora.tritius.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (509, 190, 200, 'mkmost', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (509,'https://most.tritius.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (510, 180, 200, 'mkpel', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (510,'https://tritius.knih-pe.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (511, 188, 200, 'mkostrov', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (511,'https://katalog.mkostrov.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (512, 197, 200, 'mktrut', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (512,'https://trutnov.tritius.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (513, 164, 200, 'mksvit', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (513,'https://tritius.booksy.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (514, 209, 200, 'mkcl', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (514,'https://tritius.knihovna-cl.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (515, 198, 200, 'knihkm', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (515,'https://kromeriz.tritius.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (516, 202, 200, 'mkplzen', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (516,'https://tritius.plzen.eu/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (517, 215, 200, 'mkklat', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (517,'https://klatovy.tritius.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (518, 132, 200, 'kkkv', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (518,'https://kkkv.tritius.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (519, 201, 200, 'mkkolin', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (519,'https://kolin.tritius.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (520, 225, 200, 'mkboskovice', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (520,'https://boskovice.tritius.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (521, 199, 200, 'mkchom', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (521,'https://chomutovskaknihovna.tritius.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (522, 168, 200, 'mkfm', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (522,'https://fmi.tritius.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (523, 223, 200, 'mkricany', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (523,'https://tritius-knihovna.ricany.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (524, 227, 200, 'mktrebic', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (524,'https://trebic.tritius.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (525, 232, 200, 'mkjh', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (525,'https://jh.tritius.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (526, 224, 200, 'mkmilovice', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (526,'https://milovice.tritius.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (527, 172, 200, 'mkcaslav', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (527,'https://online.knihovnacaslav.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (528, 246, 200, 'mknbk', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (528,'https://tritius.knihovnanymburk.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (529, 259, 200, 'mkjihlava', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (529,'https://jihlava.tritius.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (530, 263, 200, 'mkben', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (530,'https://benesov.tritius.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (531, 226, 200, 'kmol', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (531,'https://tritius.kmol.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (532, 150, 200, 'mkchodov', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (532,'https://tritius.knihovnachodov.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (533, 177, 200, 'mkhk', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (533,'https://kmhk.tritius.cz/tritius/oai-provider','PLM','marc21',NULL);

--changeset tomascejpek:393 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (296, 'MKTNV', 'https://knihovnatnv.cz/', 'https://arl4.library.sk/arl-tyn/cs/index/', 'Týn nad Vltavou', 'JC');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, catalog_serial_link) VALUES (496, 296, 200, 'mktnv', 11, false, true, true, true, 'U', 'other', true);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex,harvest_job_name,set_spec_full_harvest) VALUES (496,'https://arl4.library.sk/arl-tyn/cs/oai/','TYNCPK2','oai_marcxml_cpk',NULL,'s/[^:]+:[^:]+:[^:]+:(.+)/TynUsCat*$1/','cosmotronHarvestJob','TYNCPK');

--changeset tomascejpek:394 context:cpk
UPDATE oai_harvest_conf SET extract_id_regex='FMG510:(.*)' WHERE import_conf_id=494;
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (153, 494, 'FMG510');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (156, 496, 'CBG506');

--changeset tomascejpek:395 context:cpk
UPDATE oai_harvest_conf SET url='https://katalog.kjm.cz/i2/i2.ws.oai.cls' WHERE import_conf_id=303;

--changeset tomascejpek:396 context:cpk
UPDATE kramerius_conf SET url_solr='https://solr-export.app.ceskadigitalniknihovna.cz/solr/search_v2/',collection=NULL,fulltext_version='7' WHERE import_conf_id IN (99003,99011,99014,99017,99026,99037);

--changeset tomascejpek:397 context:cpk
UPDATE oai_harvest_conf SET url='https://tritius.knih-pi.cz/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=381;
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (534, 181, 200, 'mkpisek', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (534,'https://tritius.knih-pi.cz/tritius/oai-provider','PLM','marc21',NULL);

--changeset tomascejpek:398 context:cpk
UPDATE kramerius_conf SET url='https://api.kramerius.mzk.cz',url_solr='https://solr-export.k7.mzk.cz/solr/search/',dnnt_dest_url='https://www.digitalniknihovna.cz/mzk/uuid/',fulltext_version='7',metadata_stream='BIBLIO_MODS' WHERE import_conf_id=99001;

--changeset tomascejpek:399 context:cpk
UPDATE kramerius_conf SET url='https://api.kramerius.mzk.cz/' WHERE import_conf_id=99001;

--changeset tomascejpek:400 context:cpk
UPDATE import_conf SET item_id='koha',mappings996='koha' WHERE id=369;
UPDATE oai_harvest_conf SET url='https://koha.rkka.cz/cgi-bin/koha/oai.pl',set_spec='CPK',metadata_prefix='marccpk',extract_id_regex='KAG001:(.*)',harvest_job_name=NULL WHERE import_conf_id=369;

--changeset tomascejpek:401 context:cpk
UPDATE oai_harvest_conf SET url='https://katalog.knihovnauk.cz/tritius/oai-provider' WHERE import_conf_id=314;
UPDATE oai_harvest_conf SET url='https://katalog.knihovnauk.cz/tritius/oai-provider' WHERE import_conf_id=461;

--changeset tomascejpek:402
ALTER TABLE kramerius_conf ADD COLUMN dedup_fulltext BOOLEAN DEFAULT FALSE;

--changeset tomascejpek:403
ALTER TABLE kramerius_conf ADD COLUMN harvest_periodical_fulltext BOOLEAN DEFAULT TRUE;

--changeset tomascejpek:404 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (292, 'CNB', 'https://www.cnb.cz/', 'https://katalog.cnb.cz/', 'Praha', 'PR');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id) VALUES (492, 292, 200, 'cnb', 11, false, true, true, true, 'U', 'other');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (492,'https://katalog.cnb.cz/api/oai','cpk','marc21',NULL);
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (151, 492, 'ABE031');

--changeset tomascejpek:405 context:cpk
UPDATE kramerius_conf SET url='https://kramerius.cbvk.cz/',collection=null,availability_source_url=null,availability_dest_url='https://kramerius.cbvk.cz/uuid/' WHERE import_conf_id=99013;
UPDATE kramerius_conf SET url='https://digitalnistudovna.army.cz/',availability_dest_url='https://digitalnistudovna.army.cz/uuid/' WHERE import_conf_id=99031;

--changeset tomascejpek:406 context:cpk
UPDATE kramerius_conf SET url='https://k7.mlp.cz/' WHERE import_conf_id=99015;

--changeset tomascejpek:407 context:cpk
INSERT INTO library (id, name, url, catalog_url, city) VALUES (339, 'ASARP', NULL, NULL, NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (539, 339, 200, 'asarp', 0, false, true, true, false, 'U');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format) VALUES (539,NULL,NULL,NULL);

--changeset tomascejpek:408 context:cpk
UPDATE oai_harvest_conf SET url='https://orlova.tritius.cz/tritius/oai-provider' WHERE import_conf_id IN (379,498);

--changeset tomascejpek:409 context:cpk
UPDATE download_import_conf SET url='https://bookport.cz/marc21-20237.xml' WHERE import_conf_id=489;

--changeset tomascejpek:410 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (295, 'VSE', 'https://www.vse.cz/', 'https://katalog.vse.cz/', 'Praha', 'PR');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (495, 295, 200, 'vse', 11, false, true, true, true, 'U', 'aleph', 'aleph');
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format) VALUES (495,'local:/data/imports/uep01_cpk_upd','importRecordsJob','xml');

--changeset tomascejpek:411 context:cpk
UPDATE import_conf SET library_id=295 WHERE id=99041;
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (154, 495, 'ABA006');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (155, 495, 'JHD001');

--changeset tomascejpek:412 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (335, 'KBBB', 'https://www.knihovnabbb.cz/', 'https://knihovnabbb.tritius.cz/', 'Uherské Hradiště', 'ZL');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (535, 335, 200, 'kbbb', 11, false, true, true, true, 'U', 'other', 'tritius');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (535,'https://knihovnabbb.tritius.cz/tritius/oai-provider','CPK_1','marc21',NULL);

--changeset tomascejpek:413 context:cpk
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (536, 335, 200, 'kbbb', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (536,'https://knihovnabbb.tritius.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (158, 535, 'UHG001');

--changeset tomascejpek:414 context:cpk
UPDATE import_conf SET mapping_script='LocalMzk.groovy,HarvestedRecordBaseMarc.groovy' WHERE id in (304,321,325,326);

--changeset tomascejpek:415 context:cpk
UPDATE import_conf SET mapping_script='LocalMzk.groovy,HarvestedRecordBaseMarc.groovy' WHERE id in (330,349,358,360,361,420,456,448);

--changeset tomascejpek:416 context:cpk
UPDATE import_conf SET is_library=TRUE WHERE id=99041;
UPDATE kramerius_conf SET availability_dest_url='https://kramerius.vse.cz/uuid/' WHERE import_conf_id=99041;

--changeset tomascejpek:417 context:cpk
UPDATE kramerius_conf SET url='https://kramerius.zcm.cz/',availability_dest_url='https://kramerius.zcm.cz/uuid/' WHERE import_conf_id=99044;

--changeset tomascejpek:418 context:cpk
UPDATE import_conf SET item_id='koha',mappings996='koha' WHERE id=378;
UPDATE oai_harvest_conf SET url='https://milevsko-opac.koha-system.cz/cgi-bin/koha/oai.pl',set_spec='CPK',metadata_prefix='cpk',extract_id_regex='PIG501:(.*)' WHERE import_conf_id=378;

--changeset tomascejpek:419 context:cpk
UPDATE oai_harvest_conf SET set_spec='MZKALL' WHERE import_conf_id=300;

--changeset tomascejpek:420 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (124, 378, 'PIG501');

--changeset tomascejpek:421 context:cpk
UPDATE import_conf SET mapping_script='LocalMzk.groovy,HarvestedRecordBaseMarc.groovy' WHERE id=319;

--changeset tomascejpek:422 context:cpk
UPDATE kramerius_conf SET url='https://kramerius.svkul.cz/',availability_dest_url='https://kramerius.svkul.cz/uuid/' WHERE import_conf_id=99011;

--changeset tomascejpek:423 context:cpk
UPDATE oai_harvest_conf SET url='https://ipac.svkkl.cz/i2/i2.entry.cls',set_spec='CPK',extract_id_regex='s/[^:]+:[^:]+:[^:]+:(.+)/KlUsCat*$1/',set_spec_full_harvest=NULL WHERE import_conf_id=336;

--changeset tomascejpek:424 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (340, 'MKNP', 'https://knihovnanpaka.cz/', 'https://katalog.knihovnanpaka.cz/', 'Nová Paka', 'KH');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (540, 340, 200, 'mknp', 11, false, true, true, true, 'U', 'koha', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (540,'https://koha.knihovnanpaka.cz/cgi-bin/koha/oai.pl','CPK','marccpk',NULL,'JCG502:(.*)');

--changeset tomascejpek:425 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (161, 540, 'JCG502');

--changeset tomascejpek:426 context:cpk
UPDATE kramerius_conf SET url_solr='https://solr-export.app.ceskadigitalniknihovna.cz/solr/search_v2/',collection=NULL,fulltext_version='7' WHERE import_conf_id IN (99013,99023);

--changeset tomascejpek:427 context:cpk
UPDATE kramerius_conf SET dnnt_dest_url='https://kramerius.knihovna-pardubice.cz/uuid/' WHERE import_conf_id=99026;
UPDATE kramerius_conf SET url='https://kramerius.knihovnauk.cz/',availability_dest_url='https://kramerius.knihovnauk.cz/uuid/',dnnt_dest_url='https://kramerius.knihovnauk.cz/uuid/' WHERE import_conf_id=99011;
UPDATE kramerius_conf SET url_solr='https://solr-export.app.ceskadigitalniknihovna.cz/solr/search_v2/',fulltext_version='7' WHERE import_conf_id in (99014,99041);

--changeset tomascejpek:428 context:cpk
UPDATE import_conf SET catalog_serial_link=True WHERE mappings996='tritius';

--changeset tomascejpek:429 context:cpk
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (541, 131, 200, 'bcbt', 8, false, true, true, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (541,'https://bcbt.lib.cas.cz/api/oai','prvotisky','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (542, 131, 200, 'bcbt', 8, false, true, true, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (542,'https://bcbt.lib.cas.cz/api/oai','stol16','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (543, 131, 200, 'bcbt', 8, false, true, true, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (543,'https://bcbt.lib.cas.cz/api/oai','stol17','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (544, 131, 200, 'bcbt', 8, false, true, true, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (544,'https://bcbt.lib.cas.cz/api/oai','stol18','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency) VALUES (545, 131, 200, 'bcbt', 8, false, true, true, true, 'U');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (545,'https://bcbt.lib.cas.cz/api/oai','desiderata','marc21',NULL);

--changeset tomascejpek:430 context:cpk
UPDATE import_conf SET is_library=FALSE WHERE id in (541,542,543,544,545);

--changeset tomascejpek:431 context:cpk
UPDATE oai_harvest_conf SET url='https://tritius.knihovnanymburk.cz/tritius/oai-provider' WHERE import_conf_id=446;
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (554, 246, 200, 'mknbk', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (554,'https://tritius.knihovnanymburk.cz/tritius/oai-provider','PLM','marc21',NULL);

--changeset tomascejpek:432 context:cpk
DELETE FROM oai_harvest_conf WHERE import_conf_id=554;
DELETE FROM import_conf WHERE id=554;
UPDATE oai_harvest_conf SET url='https://katalog.knihovnakv.cz/tritius/oai-provider' WHERE import_conf_id=518;
UPDATE oai_harvest_conf SET url='https://tritius.knihovnakolin.cz/tritius/oai-provider' WHERE import_conf_id=519;

--changeset tomascejpek:433
INSERT INTO harvested_record_format(id, name) VALUES (75, 'MUSICAL_SCORES_PRINTED');
INSERT INTO harvested_record_format(id, name) VALUES (76, 'MUSICAL_SCORES_MANUSCRIPT');

--changeset tomascejpek:434 context:cpk
UPDATE import_conf SET mapping_script='LocalNkp.groovy,HarvestedRecordBaseMarc.groovy' WHERE id in (304,319,321,325,326);

--changeset tomascejpek:435 context:cpk
UPDATE kramerius_conf SET url='https://kramerius.svkkl.cz/',url_solr='https://solr-export.app.ceskadigitalniknihovna.cz/solr/search_v2/',availability_dest_url='https://kramerius.svkkl.cz/uuid/',fulltext_version='7' WHERE import_conf_id=99024;
UPDATE kramerius_conf SET url_solr='https://solr-export.app.ceskadigitalniknihovna.cz/solr/search_v2/',fulltext_version='7' WHERE import_conf_id=99044;

--changeset tomascejpek:436 context:cpk
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, generate_dedup_keys, generate_biblio_linker_keys, mapping_script, item_id, mappings996) VALUES (554, 104, 200, 'nkp', 11, false, true, false, true, 'U', false, false,'LocalNkp.groovy,HarvestedRecordBaseMarc.groovy','aleph', 'aleph');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (554,'https://aleph.nkp.cz/OAI','NKC-CPK-ST','marc21',NULL);
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (169, 554, 'ABA001');

--changeset tomascejpek:437 context:cpk
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (63, 389, 'BEG001');

--changeset tomascejpek:438 context:cpk
UPDATE import_conf SET mapping_script='LocalBookport.groovy,HarvestedRecordBaseMarc.groovy' WHERE id in (413,445,448,449,450,451,454,455,489);

--changeset tomascejpek:439 context:cpk
UPDATE download_import_conf SET url='https://bookport.cz/marc21-24104.xml' WHERE import_conf_id=454;

--changeset tomascejpek:440 context:cpk
UPDATE oai_harvest_conf SET ictx='kl',op='oai' WHERE import_conf_id=336;

--changeset tomascejpek:441 context:cpk
UPDATE import_conf SET item_id='other',mappings996='tritius',catalog_serial_link=TRUE WHERE id=333;
UPDATE oai_harvest_conf SET url='https://kkpce.tritius.cz/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=333;

--changeset tomascejpek:442 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (338, 'KMMB', 'https://www.kmmb.cz/', 'https://www.kmmb.eu/', 'Mladá Boleslav', 'SC');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, catalog_serial_link) VALUES (538, 338, 200, 'kmmb', 11, false, true, true, true, 'U', 'other', true);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (538,'https://www.kmmb.eu/api/oai','cpk','marc21',NULL);
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (160, 538, 'MBG001');

--changeset tomascejpek:443 context:cpk
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996, catalog_serial_link) VALUES (557, 133, 200, 'kkpc', 11, false, true, true, true, 'U', 'other', 'tritius', true);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (557,'https://kkpce.tritius.cz/tritius/oai-provider','CPKPE_1','marc21',NULL);
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (172, 557, 'PAG001');

--changeset tomascejpek:444 context:cpk
UPDATE download_import_conf SET url='https://bookport.cz/marc21-26950.xml' WHERE import_conf_id=489;

--changeset tomascejpek:445 context:cpk
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, mapping_script, generate_dedup_keys) VALUES (558, 295, 200, 'bookport', 11, false, true, true, true, 'U', 'LocalBookport.groovy,HarvestedRecordBaseMarc.groovy', false);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,reharvest) VALUES (558,'https://bookport.cz/marc21-26964.xml','downloadAndImportRecordsJob','xml',true);

--changeset tomascejpek:446 context:cpk
UPDATE download_import_conf SET url='https://bookport.cz/marc21-26943.xml' WHERE import_conf_id=448;
UPDATE download_import_conf SET url='https://bookport.cz/marc21-26952.xml' WHERE import_conf_id=445;

--changeset tomascejpek:447 context:cpk
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, mapping_script, generate_dedup_keys) VALUES (559, 159, 200, 'bookport', 11, false, true, true, true, 'U', 'LocalBookport.groovy,HarvestedRecordBaseMarc.groovy', false);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,reharvest) VALUES (559,'https://bookport.cz/marc21-26954.xml','downloadAndImportRecordsJob','xml',true);

--changeset tomascejpek:448 context:cpk
UPDATE download_import_conf SET url='https://bookport.cz/marc21-26955.xml' WHERE import_conf_id=450;

--changeset tomascejpek:449 context:cpk
UPDATE download_import_conf SET url='https://www.bookport.cz/marc21.xml' WHERE import_conf_id=451;

--changeset tomascejpek:450 context:cpk
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, mapping_script, generate_dedup_keys) VALUES (560, 107, 200, 'bookport', 11, false, true, true, true, 'U', 'LocalBookport.groovy,HarvestedRecordBaseMarc.groovy', false);
INSERT INTO download_import_conf (import_conf_id,url,import_job_name,format,reharvest) VALUES (560,'https://bookport.cz/marc21-26969.xml','downloadAndImportRecordsJob','xml',true);

--changeset tomascejpek:451 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (350, 'MKCHOT', 'https://www.knihovnachotebor.cz/', 'https://chotebor.tritius.cz/', 'Chotěboř', 'VY');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996, catalog_serial_link) VALUES (550, 350, 200, 'mkchot', 11, false, true, true, true, 'U', 'other', 'tritius', true);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (550,'https://chotebor.tritius.cz/tritius/oai-provider','CPK_1','marc21',NULL);
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (165, 550, 'HBG501');

--changeset tomascejpek:452 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (355, 'MKLOVOSICE', 'https://knihovna.lovosice.com/', 'https://lovosice.tritius.cz/', 'Lovosice', 'US');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996, catalog_serial_link) VALUES (555, 355, 200, 'mklovosice', 11, false, true, true, true, 'U', 'other', 'tritius', true);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (555,'https://lovosice.tritius.cz/tritius/oai-provider','CPK_1','marc21',NULL);
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (170, 555, 'LTG503');

--changeset tomascejpek:453 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (346, 'MKJBC', 'https://knihovna.mestojablonec.cz/', 'https://katalog.mkjbc.cz/', 'Jablonec nad Nisou', 'LI');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (546, 346, 200, 'mkjbc', 11, false, true, true, true, 'U', 'koha', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (546,'https://koha.mkjbc.cz/cgi-bin/koha/oai.pl','CPK','marccpk',NULL,'JNG001:(.*)');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (162, 546, 'JNG001');

--changeset tomascejpek:454
INSERT INTO harvested_record_format(id, name) VALUES (77, 'EAUDIOBOOK');

--changeset tomascejpek:455 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (353, 'MKKURIM', 'https://www.kulturakurim.cz/knihovna', 'https://kurim.tritius.cz/', 'Kuřim', 'JM');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996, catalog_serial_link) VALUES (553, 353, 200, 'mkkurim', 11, false, true, true, true, 'U', 'other', 'tritius', true);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (553,'https://kurim.tritius.cz/tritius/oai-provider','CPK_1','marc21',NULL);
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (168, 553, 'BOG505');

--changeset tomascejpek:456 context:cpk
UPDATE kramerius_conf SET url_solr='https://solr-export.app.ceskadigitalniknihovna.cz/solr/search_v2/',fulltext_version='7' WHERE import_conf_id=99048;

--changeset tomascejpek:457 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (337, 'MILIN', 'https://www.knihovnamilin.cz/', 'https://katalog.knihovnamilin.cz/', 'Milín', 'SC');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (537, 337, 200, 'milin', 11, false, true, true, true, 'U', 'koha', 'koha');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity,extract_id_regex) VALUES (537,'https://koha.knihovnamilin.cz/cgi-bin/koha/oai.pl','CPK','marccpk',NULL,'PBG506:(.*)');
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (159, 537, 'PBG506');

--changeset tomascejpek:458
INSERT INTO harvested_record_format(id, name) VALUES (78, 'AUDIO_STREAMING');

--changeset tomascejpek:459 context:cpk
UPDATE kramerius_conf SET url_solr='https://solr-export.app.ceskadigitalniknihovna.cz/solr/search_v2/',fulltext_version='7' WHERE import_conf_id=99040;

--changeset tomascejpek:460 context:cpk
UPDATE oai_harvest_conf SET url='https://katalog.npmk.gov.cz/api/oai' WHERE import_conf_id in (348,363);

--changeset tomascejpek:461 context:cpk
UPDATE kramerius_conf SET url_solr='https://solr-export.app.ceskadigitalniknihovna.cz/solr/search_v2/',fulltext_version='7',dnnt_dest_url='https://kramerius.techlib.cz/kramerius-web-client/uuid/' WHERE import_conf_id=99016;

--changeset tomascejpek:462 context:cpk
UPDATE kramerius_conf SET dnnt_dest_url='https://kramerius.svkkl.cz/uuid/' WHERE import_conf_id=99024;
UPDATE kramerius_conf SET dnnt_dest_url='https://kramerius.cbvk.cz/uuid/' WHERE import_conf_id=99013;

--changeset tomascejpek:463 context:cpk
UPDATE oai_harvest_conf SET url='https://hradecnm.tritius.cz/tritius/oai-provider' WHERE import_conf_id=375;
UPDATE oai_harvest_conf SET url='https://ckrumlov.tritius.cz/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=373;

--changeset tomascejpek:464 context:cpk
UPDATE oai_harvest_conf SET url='https://kpbo.tritius.cz/tritius/oai-provider',set_spec='CPK_1' WHERE import_conf_id=353;

--changeset tomascejpek:465 context:cpk
UPDATE import_conf SET mapping_script='LocalMzk.groovy,HarvestedRecordBaseMarc.groovy' WHERE id=457;

--changeset tomascejpek:466 context:cpk
UPDATE kramerius_conf SET url='https://kramerius.knihovnakv.cz/',availability_dest_url='https://kramerius.knihovnakv.cz/uuid/' WHERE import_conf_id=99020;

--changeset tomascejpek:467 context:cpk
UPDATE oai_harvest_conf SET url='https://koha.knihmil.cz/cgi-bin/koha/oai.pl',metadata_prefix='marccpk' WHERE import_conf_id=378;

--changeset tomascejpek:468 context:cpk
UPDATE kramerius_conf SET url='https://kramerius7.kvkli.cz/',availability_dest_url='https://kramerius7.kvkli.cz/uuid/' WHERE import_conf_id=99021;

--changeset tomascejpek:469 context:cpk
INSERT INTO library (id, name, url, catalog_url, city, region) VALUES (365, 'MKCLK', 'https://knihovna.celakovice.cz/', 'https://celakovice.tritius.cz/', 'Čelákovice', 'SC');
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, item_id, mappings996) VALUES (565, 365, 200, 'mkclk', 11, false, true, true, true, 'U', 'other', 'tritius');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (565,'https://celakovice.tritius.cz/tritius/oai-provider','CPK_1','marc21',NULL);
INSERT INTO import_conf (id, library_id, contact_person_id, id_prefix, base_weight, cluster_id_enabled, filtering_enabled, interception_enabled, is_library, harvest_frequency, indexed) VALUES (566, 365, 200, 'mkclk', 11, false, true, false, true, 'U', false);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (566,'https://celakovice.tritius.cz/tritius/oai-provider','PLM','marc21',NULL);
INSERT INTO sigla (id, import_conf_id, sigla) VALUES (177, 565, 'ABG505');
