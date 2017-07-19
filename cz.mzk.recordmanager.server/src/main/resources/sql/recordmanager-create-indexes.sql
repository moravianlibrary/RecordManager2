CREATE INDEX harvested_record_updated_index ON harvested_record USING btree (updated);
CREATE INDEX harvested_record_dedup_record_id_updated_idx ON harvested_record(dedup_record_id, updated);
CREATE INDEX harvested_record_dedup_keys_hash_idx ON harvested_record(dedup_keys_hash);
CREATE INDEX harvested_record_import_conf_id ON harvested_record(import_conf_id);
CREATE INDEX harvested_record_next_dedup_flag ON harvested_record(next_dedup_flag);
CREATE INDEX harvested_record_raw_001_id_idx ON harvested_record(raw_001_id);
CREATE INDEX harvested_record_source_info_idx ON harvested_record(source_info);

CREATE INDEX cnb_harvested_record_idx ON cnb(harvested_record_id);
CREATE INDEX title_harvested_record_idx ON title(harvested_record_id);
CREATE INDEX short_title_harvested_record_idx ON short_title(harvested_record_id);
CREATE INDEX isbn_harvested_record_idx ON isbn(harvested_record_id);
CREATE INDEX ean_harvested_record_idx ON ean(harvested_record_id); 
CREATE INDEX ismn_harvested_record_idx ON ismn(harvested_record_id);
CREATE INDEX issn_harvested_record_idx ON issn(harvested_record_id); 
CREATE INDEX cluster_id_harvested_record_idx ON harvested_record(cluster_id);
CREATE INDEX oclc_harvested_record_idx ON oclc(harvested_record_id); 
CREATE INDEX language_harvested_record_idx ON language(harvested_record_id);
CREATE INDEX cosmotron_996_harvested_record_idx ON cosmotron_996(harvested_record_id);
CREATE INDEX cosmotron_996_record_id_import_conf_idx ON cosmotron_996(record_id, import_conf_id);
CREATE INDEX inspiration_harvested_record_idx ON inspiration(harvested_record_id);

CREATE INDEX authority_code_idx ON authority_record(authority_code);

CREATE INDEX fulltext_kramerius_harvested_record_idx ON fulltext_kramerius(harvested_record_id);

CREATE INDEX obalkyknih_toc_book_idx ON obalkyknih_toc(book_id);
CREATE INDEX obalkyknih_toc_oclc_idx ON obalkyknih_toc(oclc);
CREATE INDEX obalkyknih_toc_ean_idx ON obalkyknih_toc(ean);
CREATE INDEX obalkyknih_toc_isbn_idx ON obalkyknih_toc(isbn);
CREATE INDEX obalkyknih_toc_nbn_idx ON obalkyknih_toc(nbn);

CREATE INDEX dedup_record_updated_idx ON dedup_record(updated);

CREATE UNIQUE INDEX sigla_id_key ON sigla(id);
CREATE INDEX sigla_sigla_idx ON sigla(sigla);

CREATE INDEX tezaurus_id_idx ON tezaurus_record(import_conf_id,record_id);
CREATE INDEX tezaurus_name_idx ON tezaurus_record(import_conf_id,source_field,name);
