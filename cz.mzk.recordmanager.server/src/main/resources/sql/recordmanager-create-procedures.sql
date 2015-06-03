/*
 * function creates dedup_record for each harvested_record having dedup_record_id = NULL
 */
CREATE or REPLACE FUNCTION dedup_rest_of_records() RETURNS void AS $$
  DECLARE
    hr_rec RECORD;
    tmp_val numeric;
  BEGIN
    DROP TABLE IF EXISTS tmp_rest_of_ids;
    CREATE TABLE tmp_rest_of_ids AS SELECT id FROM harvested_record WHERE dedup_record_id IS NULL;
    FOR hr_rec IN SELECT * FROM tmp_rest_of_ids LOOP
      SELECT val INTO tmp_val FROM recordmanager_key;
      UPDATE recordmanager_key SET val = tmp_val+1;
      INSERT INTO dedup_record(id,updated) VALUES (tmp_val+1,now());
      UPDATE harvested_record SET dedup_record_id = tmp_val+1 WHERE id = hr_rec.id;
    END LOOP;
    DROP TABLE tmp_rest_of_ids;
  END;
$$ LANGUAGE plpgsql;