DROP TABLE IF EXISTS tmp_bls_author_title;

CREATE TABLE tmp_bls_author_title AS
SELECT nextval('tmp_bl_id_seq') AS row_id,
  array_to_string(array_agg(DISTINCT hr.biblio_linker_id), ',') biblio_linker_id,
  array_to_string(array_agg(hr.id), ',') local_record_id,
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
WHERE hr.bl_author IS NOT NULL AND hrfl.harvested_record_format_id in (5,16,13,12,14,15,17)
      AND biblio_linker_similar IS TRUE
GROUP BY hr.bl_author, titles.title
HAVING COUNT(DISTINCT biblio_linker_id)>1
  AND bool_or(next_biblio_linker_similar_flag) IS TRUE;

CREATE INDEX tmp_bls_author_title_idx ON tmp_bls_author_title(row_id);