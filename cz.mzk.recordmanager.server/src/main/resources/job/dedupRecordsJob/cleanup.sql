DELETE FROM dedup_record WHERE id IN (SELECT id FROM dedup_record EXCEPT (SELECT dedup_record_id FROM harvested_record));

UPDATE harvested_record SET next_dedup_flag = false WHERE next_dedup_flag = true;