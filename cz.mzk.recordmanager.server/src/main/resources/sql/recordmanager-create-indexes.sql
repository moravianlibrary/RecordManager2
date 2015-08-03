CREATE INDEX harvested_record_updated_index ON harvested_record USING btree (updated);
CREATE INDEX harvested_record_dedup_record_id_updated_idx ON harvested_record(dedup_record_id, updated);

CREATE INDEX cnb_harvested_record_idx ON cnb(harvested_record_id);
CREATE INDEX title_harvested_record_idx ON title(harvested_record_id);
CREATE INDEX isbn_harvested_record_idx ON isbn(harvested_record_id);
CREATE INDEX issn_harvested_record_idx ON issn(harvested_record_id); 
CREATE INDEX cluster_id_harvested_record_idx ON harvested_record(cluster_id);
CREATE INDEX oclc_harvested_record_idx ON oclc(harvested_record_id); 
CREATE INDEX language_harvested_record_idx ON language(harvested_record_id); 

