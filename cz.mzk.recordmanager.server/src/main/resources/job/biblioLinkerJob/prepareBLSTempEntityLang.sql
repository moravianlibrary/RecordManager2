DROP TABLE IF EXISTS tmp_bls_entity;

CREATE TABLE tmp_bls_entity AS
SELECT nextval('tmp_bl_id_seq') AS row_id,
  array_to_string(array_agg(DISTINCT hr.biblio_linker_id), ',') biblio_linker_id,
  array_to_string(array_agg(hr.id), ',') local_record_id,
  e.entity
FROM harvested_record hr
  INNER JOIN harvested_record_format_link hrfl ON hr.id = hrfl.harvested_record_id
  INNER JOIN language l ON l.harvested_record_id = hr.id
  INNER JOIN bl_entity e ON e.harvested_record_id = hr.id
WHERE l.lang='cze' AND hrfl.harvested_record_format_id IN (1,3,4,5,12,13,14,15,16,17,18,19,20,21,22,23)
GROUP BY e.entity
HAVING COUNT(DISTINCT biblio_linker_id)>1
  AND bool_or(next_biblio_linker_flag) IS TRUE;

CREATE INDEX tmp_bls_entity_idx ON tmp_bls_entity(row_id);