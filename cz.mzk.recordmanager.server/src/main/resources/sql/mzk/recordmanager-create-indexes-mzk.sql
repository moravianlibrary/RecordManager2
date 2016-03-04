CREATE INDEX harvested_record_updated_id_deleted_idx ON harvested_record(updated, id) WHERE deleted IS NULL;
