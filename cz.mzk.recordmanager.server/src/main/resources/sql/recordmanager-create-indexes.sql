-- INDEXES used for deduplication
CREATE INDEX harvested_record_isbn_index    ON harvested_record USING btree (isbn);
CREATE INDEX harvested_record_title_index   ON harvested_record USING btree (title varchar_pattern_ops);
CREATE INDEX harvested_record_updated_index ON harvested_record USING btree (updated);
CREATE INDEX harvested_record_dedup_record_idx ON harvested_record(dedup_record_id);
