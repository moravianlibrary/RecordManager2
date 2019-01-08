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
  city                 VARCHAR(60),
  region               VARCHAR(15)
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
  id_prefix            VARCHAR(15),
  base_weight          DECIMAL(10),
  cluster_id_enabled   BOOLEAN DEFAULT FALSE,
  filtering_enabled    BOOLEAN DEFAULT FALSE,
  interception_enabled BOOLEAN DEFAULT FALSE,
  is_library           BOOLEAN DEFAULT FALSE,
  harvest_frequency    CHAR(1) DEFAULT 'U',
  mapping_script       VARCHAR(256),
  mapping_dedup_script VARCHAR(256),
  generate_dedup_keys  BOOLEAN DEFAULT(TRUE),
  item_id              VARCHAR(15),
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
  url_solr                    VARCHAR(128),
  query_rows                  DECIMAL(10),
  metadata_stream             VARCHAR(128),
  auth_token                  VARCHAR(128),
  fulltext_harvest_type       VARCHAR(128) DEFAULT 'fedora',
  download_private_fulltexts  BOOLEAN DEFAULT FALSE,
  harvest_job_name            VARCHAR(128),
  collection                  VARCHAR(128),
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
  format               VARCHAR(128),
  import_job_name      VARCHAR(128),
  extract_id_regex     VARCHAR(128),
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
  last_harvest         TIMESTAMP,
  oai_timestamp        TIMESTAMP,
  format               VARCHAR(15) NOT NULL,
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
  upv_application_id   VARCHAR(20),
  source_info_t        VARCHAR(255),
  source_info_x        VARCHAR(30),
  source_info_g        VARCHAR(255),
  sigla                VARCHAR(10),
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

CREATE TABLE ismn (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  ismn                 DECIMAL(13),
  order_in_record      DECIMAL(4),
  note                 VARCHAR(300),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);

COMMENT ON TABLE ismn IS 'dedup_keys: table contatining ISMNs';

CREATE TABLE issn (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  issn                 VARCHAR(9),
  order_in_record      DECIMAL(4),
  note                 VARCHAR(100),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);

COMMENT ON TABLE issn IS 'dedup_keys: table contatining ISSNs';

CREATE TABLE ean (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  ean                  DECIMAL(13),
  order_in_record      DECIMAL(4),
  note                 VARCHAR(300),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);

COMMENT ON TABLE ean IS 'dedup_keys: table contatining EANs';

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
  similarity_enabled   BOOLEAN DEFAULT FALSE,
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);

CREATE TABLE short_title (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  short_title          VARCHAR(255),
  order_in_record      DECIMAL(4),
  similarity_enabled   BOOLEAN DEFAULT FALSE,
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
  CONSTRAINT record_link_pk           PRIMARY KEY (harvested_record_id, harvested_record_format_id), 
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE,
  FOREIGN KEY (harvested_record_format_id) REFERENCES harvested_record_format(id)
);

COMMENT ON TABLE harvested_record_format_link IS 'link table';

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
  import_conf_id       DECIMAL(10),
  record_id            VARCHAR(128),
  parent_record_id     VARCHAR(128),
  harvested            TIMESTAMP,
  updated              TIMESTAMP,
  deleted              TIMESTAMP,
  raw_record           BYTEA
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
  name					VARCHAR(128),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);

CREATE TABLE tezaurus_record (
  id                   DECIMAL(10) PRIMARY KEY,
  import_conf_id       DECIMAL(10),
  record_id            VARCHAR(128),
  source_field         VARCHAR(15),
  name                 VARCHAR(255),
  raw_record           BYTEA,
  FOREIGN KEY (import_conf_id) REFERENCES import_conf(id)
);

CREATE TABLE publisher_number (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  publisher_number     VARCHAR(255),
  order_in_record      DECIMAL(4),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);
COMMENT ON TABLE publisher_number IS 'dedup_keys: table contatining publisher numbers';

CREATE TABLE authority (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  authority_id         VARCHAR(20),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);
COMMENT ON TABLE authority IS 'dedup_keys: table contatining authority ids';

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
