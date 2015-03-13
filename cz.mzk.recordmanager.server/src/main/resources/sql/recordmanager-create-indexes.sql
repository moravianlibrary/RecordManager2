CREATE INDEX harvested_record_sec_key ON harvested_record(oai_harvest_conf_id, oai_record_id);

-- INDEXES used for deduplication
CREATE INDEX record_link_dedup_record_id    ON record_link USING btree (dedup_record_id);
CREATE INDEX harvested_record_isbn_index    ON harvested_record USING btree (isbn);
CREATE INDEX harvested_record_title_index   ON harvested_record USING btree (title varchar_pattern_ops);
CREATE INDEX harvested_record_updated_index ON harvested_record USING btree (updated);