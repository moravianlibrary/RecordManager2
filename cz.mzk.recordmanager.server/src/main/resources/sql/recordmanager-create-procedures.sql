/*
 * function creates table tmp_rest_of_ids from ids of harvested_record without dedup_record_id
 */
CREATE or REPLACE FUNCTION prepare_rest_of_ids_table() RETURNS numeric AS $$
  DECLARE
    tmp_count numeric;
  BEGIN
    DROP TABLE IF EXISTS tmp_rest_of_ids;
    CREATE TABLE tmp_rest_of_ids AS SELECT id FROM harvested_record WHERE dedup_record_id IS NULL;
    SELECT COUNT(*) INTO tmp_count FROM tmp_rest_of_ids;
    RETURN tmp_count;
  END;
$$ LANGUAGE plpgsql;


/*
 * function creates table 'tmp_rest_of_ids_intervals' which contains intervals of commits for table 'tmp_rest_of_ids'.
 * Intervals from this table are then used in function 'dedup_rest_of_records'
 * Point of this approrach is division generating dedup_records into separated transactions due to perfomance issues
 */
CREATE or REPLACE FUNCTION dedup_rest_of_records_offset(in_commit_interval numeric) RETURNS void AS $$
 DECLARE
    tmp_total numeric(10);
    tmp_count numeric(10);
  BEGIN
    tmp_count = 0;
    DROP TABLE IF EXISTS tmp_rest_of_ids_intervals;
    CREATE TABLE tmp_rest_of_ids_intervals (
       interval numeric(10) PRIMARY KEY
    );
    INSERT INTO tmp_rest_of_ids_intervals(interval) VALUES (0);
    SELECT COUNT(*) INTO tmp_total FROM tmp_rest_of_ids;
    FOR i IN 1..tmp_total LOOP
      tmp_count = tmp_count + 1;
      IF tmp_count % in_commit_interval = 0 THEN
          INSERT INTO tmp_rest_of_ids_intervals(interval) VALUES (tmp_count);
      END IF;
    END LOOP;
  END;
$$ LANGUAGE plpgsql;

/*
 * function assignes unique dedup_record for first 'in_limit' harvested rocord from offset 
 */
CREATE or REPLACE FUNCTION dedup_rest_of_records(in_limit numeric, in_offset numeric) RETURNS numeric AS $$
 DECLARE
    hr_rec RECORD;
    tmp_val numeric;
  BEGIN
    FOR hr_rec IN SELECT * FROM tmp_rest_of_ids LIMIT in_limit OFFSET in_offset LOOP
      SELECT val INTO tmp_val FROM recordmanager_key;
      INSERT INTO dedup_record(id,updated) VALUES (tmp_val+1,now());
      UPDATE recordmanager_key SET val = tmp_val+1;
      UPDATE harvested_record SET dedup_record_id = tmp_val+1 WHERE id = hr_rec.id;
    END LOOP;
    RETURN 1;
  END;
$$ LANGUAGE plpgsql;