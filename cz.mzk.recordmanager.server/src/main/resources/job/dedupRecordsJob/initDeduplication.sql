DROP SEQUENCE IF EXISTS tmp_table_id_seq;

CREATE SEQUENCE tmp_table_id_seq;

DROP TABLE IF EXISTS tmp_periodicals_ids;

CREATE TABLE tmp_periodicals_ids AS
SELECT hrfl.harvested_record_id AS id FROM harvested_record_format_link hrfl 
  INNER JOIN harvested_record_format hrf ON hrf.id = hrfl.harvested_record_format_id
  WHERE hrf.name = 'PERIODICALS';

CREATE INDEX tmp_periodicals_ids_idx ON tmp_periodicals_ids(id);
  
DROP TABLE IF EXISTS tmp_audio_ids;
CREATE TABLE tmp_audio_ids AS
SELECT hrfl.harvested_record_id AS id FROM harvested_record_format_link hrfl 
  INNER JOIN harvested_record_format hrf ON hrf.id = hrfl.harvested_record_format_id
  WHERE hrf.name ~* '^AUDIO';
CREATE INDEX tmp_audio_ids_idx ON tmp_audio_ids(id);

UPDATE dedup_record
SET updated=localtimestamp
WHERE id IN (SELECT dedup_record_id FROM harvested_record WHERE next_dedup_flag=TRUE AND dedup_record_id IS NOT NULL GROUP BY dedup_record_id);

UPDATE harvested_record
SET dedup_record_id=NULL,next_dedup_flag=TRUE
WHERE dedup_record_id IN (SELECT DISTINCT dedup_record_id FROM harvested_record WHERE next_dedup_flag=TRUE AND dedup_record_id IS NOT NULL);
