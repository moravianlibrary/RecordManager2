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

DROP TABLE IF EXISTS tmp_video_ids;
CREATE TABLE tmp_video_ids AS
SELECT hrfl.harvested_record_id AS id FROM harvested_record_format_link hrfl
  INNER JOIN harvested_record_format hrf ON hrf.id = hrfl.harvested_record_format_id
  WHERE hrf.name ~* '^VIDEO';
CREATE INDEX tmp_video_ids_idx ON tmp_video_ids(id);

DROP TABLE IF EXISTS tmp_sfx_conf_ids;
CREATE TABLE tmp_sfx_conf_ids AS
SELECT id AS import_conf_id FROM import_conf
  WHERE id_prefix ~* '^sfxjib' AND generate_dedup_keys IS TRUE;
CREATE INDEX tmp_sfx_conf_ids_idx ON tmp_sfx_conf_ids(import_conf_id);
DROP TABLE IF EXISTS tmp_sfx_ids;
CREATE TABLE tmp_sfx_ids AS
SELECT hr.id AS id FROM harvested_record hr
  INNER JOIN tmp_sfx_conf_ids tsci ON hr.import_conf_id=tsci.import_conf_id AND hr.deleted is null;
CREATE INDEX tmp_sfx_ids_idx ON tmp_sfx_ids(id);

UPDATE dedup_record
SET updated=localtimestamp
WHERE id IN (SELECT dedup_record_id FROM harvested_record WHERE next_dedup_flag=TRUE AND dedup_record_id IS NOT NULL GROUP BY dedup_record_id);

UPDATE harvested_record
SET dedup_record_id=NULL,next_dedup_flag=TRUE
WHERE dedup_record_id IN (SELECT DISTINCT dedup_record_id FROM harvested_record WHERE next_dedup_flag=TRUE AND dedup_record_id IS NOT NULL);
