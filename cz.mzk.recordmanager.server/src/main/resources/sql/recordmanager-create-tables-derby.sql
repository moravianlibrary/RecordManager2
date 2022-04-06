CREATE TABLE recordmanager_key (
  name                 VARCHAR(128) PRIMARY KEY,
  val                  DECIMAL(10)
);

CREATE TABLE library (
  id                   DECIMAL(10) PRIMARY KEY,
  name                 VARCHAR(128),
  url                  VARCHAR(128),
  catalog_url          VARCHAR(128),
  city                 VARCHAR(60),
  region               VARCHAR(15)
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
  metaproxy_enabled    BOOLEAN DEFAULT FALSE,
  ziskej_enabled       BOOLEAN DEFAULT FALSE,
  generate_biblio_linker_keys BOOLEAN DEFAULT TRUE,
  indexed              BOOLEAN DEFAULT TRUE,
  mappings996          VARCHAR(20),
  CONSTRAINT import_conf_library_id_fk        FOREIGN KEY (library_id)        REFERENCES library(id),
  CONSTRAINT import_conf_contact_person_id_fk FOREIGN KEY (contact_person_id) REFERENCES contact_person(id)
);

CREATE TABLE import_conf_mapping_field (
  import_conf_id         DECIMAL(10) PRIMARY KEY,
  parent_import_conf_id  DECIMAL(10) NOT NULL,
  mapping                VARCHAR(100),
  CONSTRAINT import_conf_mapping_field_import_conf_fk FOREIGN KEY (import_conf_id) REFERENCES import_conf(id),
  CONSTRAINT import_conf_mapping_field_parent_import_conf_fk FOREIGN KEY (parent_import_conf_id) REFERENCES import_conf(id)
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
  url_full_harvest     VARCHAR(128),
  set_spec             VARCHAR(128),
  set_spec_full_harvest VARCHAR(128),
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
  auth_token                  VARCHAR(128),
  fulltext_harvest_type       VARCHAR(128) DEFAULT 'fedora',
  download_private_fulltexts  BOOLEAN DEFAULT FALSE,
  harvest_job_name            VARCHAR(128),
  collection                  VARCHAR(128),
  availability_source_url     VARCHAR(128),
  availability_dest_url       VARCHAR(128),
  dnnt_dest_url               VARCHAR(128),
  availability_harvest_frequency CHAR(1) DEFAULT 'U',
  fulltext_harvest_frequency  CHAR(1) DEFAULT 'U',
  CONSTRAINT kramerius_conf_import_conf_fk FOREIGN KEY (import_conf_id) REFERENCES import_conf(id)
);

CREATE TABLE download_import_conf (
  import_conf_id       DECIMAL(10)  PRIMARY KEY,
  url                  VARCHAR(128),
  format               VARCHAR(128),
  import_job_name      VARCHAR(128),
  extract_id_regex     VARCHAR(128),
  reharvest            BOOLEAN DEFAULT FALSE,
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
  last_harvest         TIMESTAMP,
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
  upv_application_id   VARCHAR(20),
  source_info_t        VARCHAR(255),
  source_info_x        VARCHAR(30),
  source_info_g        VARCHAR(255),
  sigla                VARCHAR(10),
  dedup_keys_hash      CHAR(40),
  next_dedup_flag      BOOLEAN DEFAULT TRUE,
  publisher            VARCHAR(100),
  edition              VARCHAR(10),
  disadvantaged        BOOLEAN DEFAULT TRUE,
  biblio_linker_id     DECIMAL(10),
  biblio_linker_similar BOOLEAN DEFAULT FALSE,
  next_biblio_linker_flag BOOLEAN DEFAULT TRUE,
  next_biblio_linker_similar_flag BOOLEAN DEFAULT TRUE,
  biblio_linker_keys_hash CHAR(40),
  bl_disadvantaged     BOOLEAN DEFAULT TRUE,
  bl_author            VARCHAR(200),
  bl_publisher         VARCHAR(200),
  bl_series            VARCHAR(200),
  loans                DECIMAL(10),
  callnumber           VARCHAR(100),
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
  CONSTRAINT isbn_fk   FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);

CREATE TABLE ean (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  ean                  DECIMAL(13),
  order_in_record      DECIMAL(4),
  note                 VARCHAR(150),
  CONSTRAINT ean_fk    FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);

CREATE TABLE ismn (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  ismn                 DECIMAL(13),
  order_in_record      DECIMAL(4),
  note                 VARCHAR(150),
  CONSTRAINT ismn_fk   FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);

CREATE TABLE issn (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  issn                 VARCHAR(9),
  order_in_record      DECIMAL(4),
  note                 VARCHAR(100),
  CONSTRAINT issn_fk   FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);

CREATE TABLE cnb (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  cnb                  VARCHAR(100),
  CONSTRAINT cnb_fk    FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);

CREATE TABLE title (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  title                VARCHAR(255),
  order_in_record      DECIMAL(4),
  similarity_enabled   BOOLEAN DEFAULT FALSE,
  CONSTRAINT title_fk  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);

CREATE TABLE short_title (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  short_title          VARCHAR(255),
  order_in_record      DECIMAL(4),
  similarity_enabled   BOOLEAN DEFAULT FALSE,
  CONSTRAINT short_title_fk  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);

CREATE TABLE oclc (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  oclc                 VARCHAR(20),
  CONSTRAINT oclc_fk   FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);

CREATE TABLE language (
  harvested_record_id  DECIMAL(10),
  lang                 VARCHAR(5),
  CONSTRAINT language_pk PRIMARY KEY (harvested_record_id, lang),
  CONSTRAINT language_fk  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);

CREATE TABLE harvested_record_format (
  id                   DECIMAL(10) PRIMARY KEY,
  name                 VARCHAR(50) UNIQUE
);

CREATE TABLE harvested_record_format_link (
  harvested_record_id            DECIMAL(10),
  harvested_record_format_id     DECIMAL(10),
  CONSTRAINT record_link_pk           PRIMARY KEY (harvested_record_id, harvested_record_format_id), 
  CONSTRAINT format_link_hr_id_fk     FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE,
  CONSTRAINT format_link_hr_format_fk FOREIGN KEY (harvested_record_format_id) REFERENCES harvested_record_format(id)
);

CREATE TABLE antikvariaty (
  id                   DECIMAL(10) PRIMARY KEY,
  updated              TIMESTAMP,
  url                  VARCHAR(500),
  title                VARCHAR(255),
  pub_year             DECIMAL(5),
  last_harvest         TIMESTAMP,
  updated_original     TIMESTAMP
);

CREATE TABLE antikvariaty_catids (
  id_from_catalogue   VARCHAR(100), 
  antikvariaty_id     DECIMAL(10),
  CONSTRAINT antikvariaty_catids_pk PRIMARY KEY (id_from_catalogue, antikvariaty_id),
  CONSTRAINT antikvariaty_catids_fk FOREIGN KEY (antikvariaty_id) REFERENCES antikvariaty(id) ON DELETE CASCADE
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
  import_conf_id       DECIMAL(10),
  record_id            VARCHAR(128),
  parent_record_id     VARCHAR(128),
  harvested            TIMESTAMP,
  updated              TIMESTAMP,
  deleted              TIMESTAMP,
  last_harvest         TIMESTAMP,
  raw_record           BLOB
);

CREATE TABLE obalkyknih_toc (
  id                   DECIMAL(10) PRIMARY KEY,
  book_id              DECIMAL(10),
  nbn                  VARCHAR(32),
  oclc                 VARCHAR(32),
  ean                  VARCHAR(32),
  isbn                 DECIMAL(13),
  updated              TIMESTAMP,
  last_harvest         TIMESTAMP,
  toc                  VARCHAR(32672)
);

CREATE TABLE inspiration (
  id                   INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  harvested_record_id  DECIMAL(10),
  name                 VARCHAR(128),
  CONSTRAINT inspiration_fk FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
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
  CONSTRAINT publisher_number_fk FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);

CREATE TABLE authority (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  authority_id         VARCHAR(20),
  CONSTRAINT authority_fk FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);

CREATE TABLE obalkyknih_annotation (
  id                   DECIMAL(10) PRIMARY KEY,
  book_id              DECIMAL(10),
  cnb                  VARCHAR(32),
  oclc                 VARCHAR(32),
  isbn                 DECIMAL(13),
  updated              TIMESTAMP,
  last_harvest         TIMESTAMP,
  annotation           VARCHAR(32672)
);

CREATE TABLE anp_title (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  anp_title            VARCHAR(255),
  similarity_enabled   BOOLEAN DEFAULT FALSE,
  CONSTRAINT anp_title_fk FOREIGN KEY (harvested_record_id) REFERENCES harvested_record (id) ON DELETE CASCADE
);

CREATE TABLE biblio_linker (
  id                   DECIMAL(10) PRIMARY KEY,
  updated              TIMESTAMP
);

CREATE TABLE biblio_linker_similar (
  id                   INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  harvested_record_id  DECIMAL(10),
  harvested_record_similar_id DECIMAL(10),
  url_id               BLOB,
  type                 VARCHAR(20),
  CONSTRAINT biblio_linker_similar_pk PRIMARY KEY(id),
  CONSTRAINT bls_fk FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);

CREATE TABLE bl_title (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  title                VARCHAR(255),
  CONSTRAINT bl_title_fk FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);

CREATE TABLE bl_common_title (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  title                VARCHAR(255),
  CONSTRAINT bl_common_title_fk FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);

CREATE TABLE bl_entity (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  entity               VARCHAR(200),
  CONSTRAINT bl_entity_fk FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);

CREATE TABLE bl_topic_key (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  topic_key            VARCHAR(20),
  CONSTRAINT bl_topic_key_fk FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);

CREATE TABLE bl_language (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  lang                 VARCHAR(5),
  CONSTRAINT bl_language_fk FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);

CREATE TABLE uuid (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  uuid                 VARCHAR(100),
  CONSTRAINT uuid_fk FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);

CREATE TABLE kram_availability (
  id                INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  import_conf_id    DECIMAL(10) NOT NULL,
  uuid              VARCHAR(100) NOT NULL,
  availability      VARCHAR(20) NOT NULL,
  dnnt              BOOLEAN DEFAULT FALSE,
  level             DECIMAL(10),
  updated           TIMESTAMP NOT NULL,
  last_harvest      TIMESTAMP NOT NULL,
  CONSTRAINT kram_availability_pk PRIMARY KEY(id),
  FOREIGN KEY (import_conf_id) REFERENCES import_conf(id) ON DELETE CASCADE
);

CREATE TABLE kram_dnnt_label (
  id                    INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  kram_availability_id INTEGER,
  label                 VARCHAR(100) NOT NULL,
  CONSTRAINT kram_dnnt_labels_pk PRIMARY KEY(id),
  FOREIGN KEY (kram_availability_id) REFERENCES kram_availability(id) ON DELETE CASCADE
);

CREATE TABLE ziskej_library (
  id                INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  sigla             VARCHAR(10) NOT NULL,
  updated           TIMESTAMP NOT NULL,
  last_harvest      TIMESTAMP NOT NULL,
  CONSTRAINT ziskej_libraries_pk PRIMARY KEY(id)
);

CREATE TABLE fit_project (
  id                   DECIMAL(10) PRIMARY KEY,
  name                 VARCHAR(50) UNIQUE
);

CREATE TABLE fit_knowledge_base (
  id                   DECIMAL(10) PRIMARY KEY,
  data                 BLOB
);

CREATE TABLE fit_project_link (
  id                     INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  harvested_record_id    DECIMAL(10),
  fit_project_id         DECIMAL(10),
  fit_knowledge_base_id  DECIMAL(10),
  data                   BLOB,
  CONSTRAINT fit_projects_pk PRIMARY KEY(id),
  CONSTRAINT fit_hr_fk FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE,
  CONSTRAINT fit_project_fk FOREIGN KEY (fit_project_id) REFERENCES fit_project(id) ON DELETE CASCADE,
  CONSTRAINT fit_kb_fk FOREIGN KEY (fit_knowledge_base_id) REFERENCES fit_knowledge_base(id) ON DELETE CASCADE
);

CREATE TABLE title_old_spelling (
  id                   INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  "key"                VARCHAR(128),
  value                VARCHAR(128)
);

CREATE TABLE caslin_links (
  id                INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  sigla             VARCHAR(10) NOT NULL,
  url               VARCHAR (100) NOT NULL,
  updated           TIMESTAMP NOT NULL,
  last_harvest      TIMESTAMP NOT NULL,
  CONSTRAINT caslin_links_pk PRIMARY KEY(id)
);

CREATE TABLE loc (
  id                   DECIMAL(10) PRIMARY KEY,
  harvested_record_id  DECIMAL(10),
  loc                  VARCHAR(20),
  FOREIGN KEY (harvested_record_id) REFERENCES harvested_record(id) ON DELETE CASCADE
);
