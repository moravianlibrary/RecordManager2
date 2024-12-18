DROP SEQUENCE IF EXISTS tmp_bl_id_seq;
CREATE SEQUENCE tmp_bl_id_seq;

UPDATE harvested_record
SET biblio_linker_id=NULL,next_biblio_linker_flag=TRUE,next_biblio_linker_similar_flag=TRUE
WHERE biblio_linker_id IN (SELECT DISTINCT biblio_linker_id FROM harvested_record WHERE next_biblio_linker_flag=TRUE AND biblio_linker_id IS NOT NULL);

DROP TABLE IF EXISTS tmp_video_ids;
CREATE TABLE tmp_video_ids AS
  SELECT hrfl.harvested_record_id AS id FROM harvested_record_format_link hrfl
    INNER JOIN harvested_record_format hrf ON hrf.id = hrfl.harvested_record_format_id
  WHERE hrf.name ~* '^VIDEO';
CREATE INDEX tmp_video_ids_idx ON tmp_video_ids(id);

DROP TABLE IF EXISTS tmp_audio_ids;
CREATE TABLE tmp_audio_ids AS
  SELECT hrfl.harvested_record_id AS id FROM harvested_record_format_link hrfl
    INNER JOIN harvested_record_format hrf ON hrf.id = hrfl.harvested_record_format_id
  WHERE hrf.name ~* '^AUDIO';
CREATE INDEX tmp_audio_ids_idx ON tmp_audio_ids(id);