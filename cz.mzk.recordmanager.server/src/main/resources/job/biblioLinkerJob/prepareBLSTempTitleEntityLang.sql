DROP TABLE IF EXISTS tmp_bls_title_entity_lang;

CREATE TABLE tmp_bls_title_entity_lang AS
SELECT nextval('tmp_bl_id_seq') AS row_id,
  array_to_string(array_agg(DISTINCT hr.biblio_linker_id), ',') biblio_linker_id,
  array_to_string(array_agg(hr.id), ',') local_record_id,
  e.entity,
  titles.title,
  l.lang
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
  INNER JOIN harvested_record_format_link hrfl ON hr.id = hrfl.harvested_record_id
  INNER JOIN language l ON l.harvested_record_id = hr.id
  INNER JOIN bl_entity e ON e.harvested_record_id = hr.id
WHERE hrfl.harvested_record_format_id in (1,18,19,20,21,22,23)
GROUP BY e.entity, titles.title, l.lang
HAVING COUNT(DISTINCT biblio_linker_id)>1
  AND bool_or(next_biblio_linker_flag) IS TRUE;

CREATE INDEX tmp_bls_title_entity_lang_idx ON tmp_bls_title_entity_lang(row_id);