DROP TABLE IF EXISTS tmp_bls_topic_key_lang;

CREATE TABLE tmp_bls_topic_key_lang AS
SELECT nextval('tmp_bl_id_seq') AS row_id,
  array_to_string(array_agg(DISTINCT hr.biblio_linker_id), ',') biblio_linker_id,
  array_to_string(array_agg(hr.id), ',') local_record_id,
  hr.bl_topic_key,
  l.lang
FROM harvested_record hr
  INNER JOIN harvested_record_format_link hrfl ON hr.id = hrfl.harvested_record_id
  INNER JOIN language l ON l.harvested_record_id = hr.id
WHERE hr.bl_topic_key IS NOT NULL AND hrfl.harvested_record_format_id!=2
GROUP BY hr.bl_topic_key, l.lang
HAVING COUNT(DISTINCT biblio_linker_id)>1
  AND bool_or(next_biblio_linker_flag) IS TRUE;

CREATE INDEX tmp_bls_topic_key_lang_idx ON tmp_bls_topic_key_lang(row_id);