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

--changeset tomascejpek:90
INSERT INTO harvested_record_format(id, name) VALUES (65, 'BLIND_AUDIO');
INSERT INTO harvested_record_format(id, name) VALUES (66, 'BLIND_BRAILLE');
