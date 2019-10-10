DROP TABLE IF EXISTS tmp_bl_title_author;

CREATE TABLE tmp_bl_title_author AS
SELECT nextval('tmp_bl_id_seq') AS row_id,
  array_to_string(array_agg(DISTINCT hr.dedup_record_id), ',') dedup_record_id,
  hr.bl_author,
  titles.title
FROM (
       SELECT
         harvested_record_id,
         title
       FROM title
       UNION
       SELECT
         harvested_record_id,
         title
       FROM bl_title
     ) titles
  INNER JOIN harvested_record hr ON hr.id=titles.harvested_record_id
  INNER JOIN harvested_record_format_link hrfl ON hrfl.harvested_record_id = hr.id
  INNER JOIN bl_language l ON l.harvested_record_id = hr.id
WHERE hr.bl_author IS NOT NULL AND dedup_record_id IS NOT NULL AND hrfl.harvested_record_format_id IN (1,4)
GROUP BY titles.title, hr.bl_author, l.lang, hrfl.harvested_record_format_id
HAVING COUNT(*)>1
  AND COUNT(DISTINCT biblio_linker_id) + SUM(CASE WHEN biblio_linker_id IS NULL THEN 1 ELSE 0 END) != 1
  AND BOOL_OR(next_biblio_linker_flag) IS TRUE;

CREATE INDEX tmp_bl_title_author_idx ON tmp_bl_title_author(row_id);