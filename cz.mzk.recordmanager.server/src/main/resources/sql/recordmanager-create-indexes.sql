CREATE INDEX harvested_record_updated_index ON harvested_record USING btree (updated);
CREATE INDEX harvested_record_dedup_record_id_updated_idx ON harvested_record(dedup_record_id, updated);
CREATE INDEX harvested_record_dedup_keys_hash_idx ON harvested_record(dedup_keys_hash);
CREATE INDEX harvested_record_import_conf_id ON harvested_record(import_conf_id);
CREATE INDEX harvested_record_next_dedup_flag ON harvested_record(next_dedup_flag);
CREATE INDEX harvested_record_raw_001_id_idx ON harvested_record(raw_001_id);
CREATE INDEX harvested_record_upv_appl_dx ON harvested_record(upv_application_id);
CREATE INDEX harvested_record_source_info_t_idx ON harvested_record(source_info_t);
CREATE INDEX harvested_record_source_info_x_idx ON harvested_record(source_info_x);
CREATE INDEX harvested_record_source_info_g_idx ON harvested_record(source_info_g);
CREATE INDEX harvested_record_sigla_idx ON harvested_record(sigla);
CREATE INDEX harvested_record_last_harvest_idx ON harvested_record(last_harvest);
CREATE INDEX harvested_record_disadvantaged_idx ON harvested_record(disadvantaged);
CREATE INDEX hr_biblilinker_dedup_record_id_idx ON harvested_record(biblio_linker_id,dedup_record_id);
CREATE INDEX hr_next_biblio_linker_flag_ids ON harvested_record(next_biblio_linker_flag);
CREATE INDEX hr_next_biblio_linker_similar_flag_ids ON harvested_record(next_biblio_linker_similar_flag);
CREATE INDEX harvested_record_palmknihy_id_idx ON harvested_record(palmknihy_id);

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
CREATE INDEX cosmotron_996_conf_id_parent_id_idx ON cosmotron_996 (parent_record_id,import_conf_id);
CREATE INDEX cosmotron_996_record_id_import_conf_idx ON cosmotron_996(record_id, import_conf_id);
CREATE INDEX publisher_number_harvested_record_idx ON publisher_number(harvested_record_id);
CREATE INDEX authority_harvested_record_idx ON authority(harvested_record_id);
CREATE INDEX authority_idx ON authority(authority_id);
CREATE INDEX anp_title_harvested_record_idx ON anp_title(harvested_record_id);
CREATE INDEX bls_harvested_record_id_idx ON biblio_linker_similar(harvested_record_id);
CREATE INDEX bls_harvested_record_similar_id_idx ON biblio_linker_similar(harvested_record_similar_id);
CREATE INDEX bl_title_harvested_record_idx ON bl_title(harvested_record_id);
CREATE INDEX bl_common_title_harvested_record_idx ON bl_common_title(harvested_record_id);
CREATE INDEX bl_entity_harvested_record_idx ON bl_entity(harvested_record_id);
CREATE INDEX bl_topic_key_harvested_record_idx ON bl_topic_key(harvested_record_id);
CREATE INDEX bl_language_harvested_record_idx ON bl_language(harvested_record_id);
CREATE INDEX fulltext_kramerius_harvested_record_idx ON fulltext_kramerius(harvested_record_id);
CREATE INDEX uuid_harvested_record_idx ON uuid(harvested_record_id);
CREATE INDEX title_old_spelling_key_idx ON title_old_spelling(key);
CREATE INDEX kram_dnnt_label_availability_id_idx ON kram_dnnt_label(kram_availability_id);
CREATE INDEX import_conf_mapping_field_parent_id_idx ON import_conf_mapping_field(parent_import_conf_id);

CREATE INDEX obalkyknih_toc_book_idx ON obalkyknih_toc(book_id);
CREATE INDEX obalkyknih_toc_oclc_idx ON obalkyknih_toc(oclc);
CREATE INDEX obalkyknih_toc_ean_idx ON obalkyknih_toc(ean);
CREATE INDEX obalkyknih_toc_isbn_idx ON obalkyknih_toc(isbn);
CREATE INDEX obalkyknih_toc_nbn_idx ON obalkyknih_toc(nbn);

CREATE INDEX obalkyknih_annotation_oclc_idx ON obalkyknih_annotation(oclc);
CREATE INDEX obalkyknih_annotation_isbn_idx ON obalkyknih_annotation(isbn);
CREATE INDEX obalkyknih_annotation_cnb_idx ON obalkyknih_annotation(cnb);
CREATE INDEX obalkyknih_annotation_bookid_idx ON obalkyknih_annotation(book_id);

CREATE INDEX antik_catids_ids ON antikvariaty_catids(antikvariaty_id);

CREATE INDEX dedup_record_updated_idx ON dedup_record(updated);

CREATE UNIQUE INDEX sigla_id_key ON sigla(id);
CREATE INDEX sigla_sigla_idx ON sigla(sigla);

CREATE INDEX tezaurus_id_idx ON tezaurus_record(import_conf_id,record_id);
CREATE INDEX tezaurus_name_idx ON tezaurus_record(import_conf_id,source_field,name);

CREATE INDEX isbn_idx ON isbn(isbn);
CREATE INDEX cnb_idx ON cnb(cnb);
CREATE INDEX oclc_idx ON oclc(oclc);
CREATE INDEX ean_idx ON ean(ean);

CREATE INDEX cosmotron_996_last_harvest_idx ON cosmotron_996(last_harvest);
CREATE INDEX kram_availability_conf_uuid_idx ON kram_availability(import_conf_id, uuid);

CREATE INDEX fit_projects_knowledge_base_idx ON fit_project_link(fit_knowledge_base_id);
CREATE INDEX fit_project_link_harvested_record_idx ON fit_project_link(harvested_record_id);
CREATE INDEX fit_project_link_idx ON fit_project_link(fit_project_id);

CREATE INDEX caslin_links_sigla_idx ON caslin_links(sigla);

CREATE INDEX sigla_all_sigla_idx ON sigla_all(sigla);
CREATE INDEX sigla_all_import_conf_id_idx ON sigla_all(import_conf_id);
CREATE INDEX sigla_all_cpk_idx ON sigla_all(cpk);
CREATE INDEX sigla_all_dnnt_idx ON sigla_all(dnnt);
CREATE INDEX sigla_all_ziskej_idx ON sigla_all(ziskej);
CREATE INDEX sigla_all_ziskej_edd_idx ON sigla_all(ziskej_edd);
CREATE INDEX sigla_all_ziskej_mvs_sigla_idx ON sigla_all(ziskej_mvs_sigla);
CREATE INDEX sigla_all_ziskej_edd_sigla_idx ON sigla_all(ziskej_edd_sigla);

CREATE INDEX inspiration_name_idx ON inspiration(name);
CREATE INDEX inspiration_type_idx ON inspiration(type);
CREATE INDEX harvested_record_inspiration_id_idx ON harvested_record_inspiration(harvested_record_id);
CREATE INDEX harvested_record_inspiration_inspiration_id_idx ON harvested_record_inspiration(inspiration_id);
CREATE INDEX harvested_record_inspiration_updated_idx ON harvested_record_inspiration(updated);
CREATE INDEX harvested_record_inspiration_last_harvest_idx ON harvested_record_inspiration(last_harvest);
