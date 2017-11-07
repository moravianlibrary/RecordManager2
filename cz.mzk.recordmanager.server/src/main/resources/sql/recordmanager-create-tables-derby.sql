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
  generate_dedup_keys  BOOLEAN DEFAULT TRUE,
  item_id              VARCHAR(15),
  CONSTRAINT import_conf_library_id_fk        FOREIGN KEY (library_id)        REFERENCES library(id),
  CONSTRAINT import_conf_contact_person_id_fk FOREIGN KEY (contact_person_id) REFERENCES contact_person(id)
);

CREATE TABLE sigla (
  id                   DECIMAL(10),
  import_conf_id       DECIMAL(10),
  sigla                VARCHAR(20),
  CONSTRAINT sigla_pk PRIMARY KEY(import_conf_id,sigla),
  CONSTRAINT sigla_import_conf_fk FOREIGN KEY (import_conf_id) REFERENCES import_conf(id)
);

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

CREATE TABLE kramerius_conf (
  import_conf_id              DECIMAL(10) PRIMARY KEY,
  url                         VARCHAR(128),
  url_solr                    VARCHAR(128),
  query_rows                  DECIMAL(10),
  metadata_stream             VARCHAR(128),
  auth_token	              VARCHAR(128),
  fulltext_harvest_type       VARCHAR(128) DEFAULT 'fedora',
  download_private_fulltexts  BOOLEAN DEFAULT FALSE,
  harvest_job_name            VARCHAR(128),
  CONSTRAINT kramerius_conf_import_conf_fk FOREIGN KEY (import_conf_id) REFERENCES import_conf(id)
);

CREATE TABLE download_import_conf (
  import_conf_id       DECIMAL(10)  PRIMARY KEY,
  url                  VARCHAR(128),
  format               VARCHAR(128),
  import_job_name      VARCHAR(128),
  extract_id_regex     VARCHAR(128),
  CONSTRAINT download_conf_import_conf_fk FOREIGN KEY (import_conf_id) REFERENCES import_conf(id)
);

CREATE TABLE file_import_conf (
  id                   DECIMAL(10) PRIMARY KEY,
  import_conf_id       DECIMAL(10),
  CONSTRAINT oai_harvest_conf_import_conf_id_fk FOREIGN KEY (import_conf_id) REFERENCES import_conf(id)
);

CREATE TABLE format (
  format               VARCHAR(15) PRIMARY KEY,
  description          VARCHAR(255)
);

CREATE TABLE dedup_record (
  id                   DECIMAL(10) PRIMARY KEY,
  updated              TIMESTAMP
);

CREATE TABLE harvested_record (
  id                   DECIMAL(10),
  import_conf_id       DECIMAL(10),
  record_id            VARCHAR(128),
  raw_001_id           VARCHAR(128),
  harvested            TIMESTAMP,
  updated              TIMESTAMP,
  deleted              TIMESTAMP,
  oai_timestamp        TIMESTAMP,
  format               VARCHAR(15) NOT NULL,
  dedup_record_id      DECIMAL(10),
  publication_year     DECIMAL(4),
  author_auth_key      VARCHAR(50),
  author_string        VARCHAR(200),
  issn_series          VARCHAR(100),
  issn_series_order    VARCHAR(100),
  uuid                 VARCHAR(100),
  scale                DECIMAL(10),
  weight               DECIMAL(10),
  cluster_id           VARCHAR(20),
  pages                DECIMAL(10),
  source_info          VARCHAR(255),
  source_info_t        VARCHAR(255),
  source_info_x        VARCHAR(30),
  source_info_g        VARCHAR(255),
  dedup_keys_hash      CHAR(40),
  next_dedup_flag      BOOLEAN DEFAULT TRUE,
  raw_record           BLOB,
  CONSTRAINT harvested_record_pk                     PRIMARY KEY (id),
  CONSTRAINT harvester_record_unique_id              UNIQUE (import_conf_id, record_id),
  CONSTRAINT harvested_record_import_conf_id         FOREIGN KEY (import_conf_id) REFERENCES import_conf(id),
  CONSTRAINT harvested_record_format_fk              FOREIGN KEY (format)         REFERENCES format(format)
);

CREATE TABLE isbn (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  isbn                 DECIMAL(13),
  order_in_record      DECIMAL(4),
  note                 VARCHAR(150),
  CONSTRAINT isbn_fk   FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id)
);

CREATE TABLE ean (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  ean                  DECIMAL(13),
  order_in_record      DECIMAL(4),
  note                 VARCHAR(150),
  CONSTRAINT ean_fk    FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id)
);

CREATE TABLE ismn (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  ismn                 DECIMAL(13),
  order_in_record      DECIMAL(4),
  note                 VARCHAR(150),
  CONSTRAINT ismn_fk   FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id)
);

CREATE TABLE issn (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  issn                 VARCHAR(9),
  order_in_record      DECIMAL(4),
  note                 VARCHAR(100),
  CONSTRAINT issn_fk   FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id)
);

CREATE TABLE cnb (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  cnb                  VARCHAR(100),
  CONSTRAINT cnb_fk    FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id)
);

CREATE TABLE title (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  title                VARCHAR(255),
  order_in_record      DECIMAL(4),
  similarity_enabled   BOOLEAN DEFAULT FALSE,
  CONSTRAINT title_fk  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id)
);

CREATE TABLE short_title (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  short_title          VARCHAR(255),
  order_in_record      DECIMAL(4),
  similarity_enabled   BOOLEAN DEFAULT FALSE,
  CONSTRAINT short_title_fk  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id)
);

CREATE TABLE oclc (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  oclc                 VARCHAR(20),
  CONSTRAINT oclc_fk   FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id)
);

CREATE TABLE language (
  harvested_record_id  DECIMAL(10),
  lang                 VARCHAR(5),
  CONSTRAINT language_pk PRIMARY KEY (harvested_record_id, lang),
  CONSTRAINT language_fk  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id)
);

CREATE TABLE harvested_record_format (
  id                   DECIMAL(10) PRIMARY KEY,
  name                 VARCHAR(50) UNIQUE
);

CREATE TABLE harvested_record_format_link (
  harvested_record_id            DECIMAL(10),
  harvested_record_format_id     DECIMAL(10),
  CONSTRAINT record_link_pk           PRIMARY KEY (harvested_record_id, harvested_record_format_id), 
  CONSTRAINT format_link_hr_id_fk     FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id),
  CONSTRAINT format_link_hr_format_fk FOREIGN KEY (harvested_record_format_id) REFERENCES harvested_record_format(id)
);

CREATE TABLE authority_record (
  id                   DECIMAL(10) PRIMARY KEY,
  import_conf_id       DECIMAL(10),
  oai_record_id        VARCHAR(128),
  authority_code       VARCHAR(128),
  harvested            TIMESTAMP,
  updated              TIMESTAMP,
  deleted              TIMESTAMP,
  format               VARCHAR(15) NOT NULL,
  raw_record           BLOB,
  FOREIGN KEY (import_conf_id)      REFERENCES import_conf(id),
  FOREIGN KEY (format)              REFERENCES format(format),
  CONSTRAINT authority_code_unique UNIQUE(authority_code)
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

CREATE TABLE fulltext_kramerius (
  id                  DECIMAL(10) PRIMARY KEY,
  harvested_record_id DECIMAL(10),
  uuid_page           VARCHAR(50),
  is_private          BOOLEAN, 
  order_in_document   DECIMAL(10),
  page                VARCHAR(50),
  fulltext            BLOB,
  CONSTRAINT fulltext_kramerius_harvested_record_id_fk FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id)
); 

CREATE TABLE skat_keys (
  skat_record_id      DECIMAL(10),
  sigla               VARCHAR(20),
  local_record_id     VARCHAR(128),
  manually_merged     BOOLEAN DEFAULT FALSE,
  CONSTRAINT skat_keys_pk PRIMARY KEY(skat_record_id,sigla,local_record_id)
);

CREATE TABLE cosmotron_996 (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  record_id            VARCHAR(128),
  import_conf_id       DECIMAL(10),
  harvested            TIMESTAMP,
  updated              TIMESTAMP,
  deleted              TIMESTAMP,
  raw_record           BLOB,
  CONSTRAINT cosmotron_996_fk FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id),
  CONSTRAINT cosmotron_996_import_conf_id FOREIGN KEY (import_conf_id) REFERENCES import_conf(id)
);

CREATE TABLE obalkyknih_toc (
  id                   DECIMAL(10) PRIMARY KEY,
  book_id              DECIMAL(10),
  nbn                  VARCHAR(32),
  oclc                 VARCHAR(32),
  ean                  VARCHAR(32),
  isbn                 DECIMAL(13),
  toc                  VARCHAR(32672)
);

CREATE TABLE inspiration (
  id					DECIMAL(10) PRIMARY KEY,
  harvested_record_id	DECIMAL(10),
  name					VARCHAR(128),
  CONSTRAINT inspiration_fk FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id)
);

CREATE TABLE tezaurus_record (
  id                   DECIMAL(10) PRIMARY KEY,
  import_conf_id       DECIMAL(10),
  record_id            VARCHAR(128),
  source_field         VARCHAR(15),
  name                 VARCHAR(255),
  raw_record           BLOB,
  CONSTRAINT tezaurus_import_conf_fk FOREIGN KEY (import_conf_id) REFERENCES import_conf(id)
);

CREATE TABLE publisher_number (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  publisher_number     VARCHAR(255),
  order_in_record      DECIMAL(4),
  CONSTRAINT publisher_number_fk FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id)
);
