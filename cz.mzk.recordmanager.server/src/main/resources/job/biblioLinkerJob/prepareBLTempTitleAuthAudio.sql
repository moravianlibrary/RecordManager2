DROP TABLE IF EXISTS tmp_bl_title_auth_audio;

CREATE TABLE tmp_bl_title_auth_audio AS
SELECT nextval('tmp_bl_id_seq') AS row_id,
  array_to_string(array_agg(DISTINCT hr.dedup_record_id), ',') dedup_record_id,
  hr.bl_author_auth_key,
  titles.title
FROM (
       SELECT
         harvested_record_id,
         title
       FROM title
       UNION
       SELECT
         harvested_record_id,
         short_title
       FROM short_title
       UNION
       SELECT
         harvested_record_id,
         title
       FROM bl_title
     ) titles
  INNER JOIN harvested_record hr ON hr.id=titles.harvested_record_id
  INNER JOIN tmp_audio_ids tai ON hr.id = tai.id
WHERE hr.bl_author_auth_key IS NOT NULL AND dedup_record_id IS NOT NULL
GROUP BY titles.title, hr.bl_author_auth_key
HAVING COUNT(*)>1
  AND COUNT(DISTINCT biblio_linker_id) + SUM(CASE WHEN biblio_linker_id IS NULL THEN 1 ELSE 0 END) != 1
  AND bool_or(next_biblio_linker_flag) IS TRUE;

CREATE INDEX tmp_bl_title_auth_audio_idx ON tmp_bl_title_auth_audio(row_id);