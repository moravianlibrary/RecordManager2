DROP TABLE IF EXISTS tmp_bls_entity_auth_key_lang;

CREATE TABLE tmp_bls_entity_auth_key_lang AS
SELECT nextval('tmp_bl_id_seq') AS row_id,
  array_to_string(array_agg(DISTINCT hr.biblio_linker_id), ',') biblio_linker_id,
  array_to_string(array_agg(hr.id), ',') local_record_id,
  e.entity_auth_key,
  l.lang
FROM harvested_record hr
  INNER JOIN language l ON l.harvested_record_id = hr.id
  INNER JOIN bl_entity_auth_key e ON e.harvested_record_id = hr.id
  WHERE biblio_linker_similar IS TRUE
GROUP BY e.entity_auth_key, l.lang
HAVING COUNT(DISTINCT biblio_linker_id)>1
  AND bool_or(next_biblio_linker_flag) IS TRUE;

CREATE INDEX tmp_bls_entity_auth_key_lang_idx ON tmp_bls_entity_auth_key_lang(row_id);