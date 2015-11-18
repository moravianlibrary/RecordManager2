CREATE TABLE IF NOT EXISTS last_dedup_time (
  time  timestamp
);

TRUNCATE TABLE last_dedup_time;

INSERT INTO last_dedup_time SELECT COALESCE(MIN(updated),'1900-01-01 00:00:00.000') AS time FROM dedup_record;

DROP SEQUENCE IF EXISTS tmp_table_id_seq;

CREATE SEQUENCE tmp_table_id_seq;

CREATE TABLE tmp_periodicals_ids AS
SELECT hrfl.harvested_record_id AS id FROM harvested_record_format_link hrfl 
  INNER JOIN harvested_record_format hrf ON hrf.id = hrfl.harvested_record_format_id
  WHERE hrf.name = 'PERIODICALS'
  
CREATE INDEX tmp_periodicals_ids_idx ON tmp_periodicals_ids(id)