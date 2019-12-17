DROP TABLE IF EXISTS tmp_bls_author_common_title;

CREATE TABLE tmp_bls_author_common_title AS
SELECT nextval('tmp_bl_id_seq') AS row_id,
  array_to_string(array_agg(DISTINCT hr.biblio_linker_id), ',') biblio_linker_id,
  array_to_string(array_agg(hr.id), ',') local_record_id,
  hr.bl_author,
  array_to_string(array_agg(ct.title), ',') common_title,
  l.lang,
  hrfl.harvested_record_format_id
FROM harvested_record hr
  INNER JOIN harvested_record_format_link hrfl ON hr.id = hrfl.harvested_record_id
  INNER JOIN bl_language l ON l.harvested_record_id = hr.id
  INNER JOIN bl_common_title ct ON ct.harvested_record_id = hr.id
WHERE hr.bl_author IS NOT NULL AND hrfl.harvested_record_format_id!=2 AND biblio_linker_similar IS TRUE
GROUP BY hr.bl_author, ct.title, l.lang, hrfl.harvested_record_format_id
HAVING COUNT(DISTINCT biblio_linker_id)>1
  AND bool_or(next_biblio_linker_similar_flag) IS TRUE;

CREATE INDEX tmp_bls_author_common_title_idx ON tmp_bls_author_common_title(row_id);