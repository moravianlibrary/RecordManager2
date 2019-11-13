DROP TABLE IF EXISTS tmp_bls_rest;

CREATE TABLE tmp_bls_rest AS
SELECT nextval('tmp_bl_id_seq') AS row_id,
  array_to_string(array_agg(DISTINCT hr.biblio_linker_id), ',') biblio_linker_id,
  array_to_string(array_agg(hr.id), ',') local_record_id
FROM harvested_record hr
WHERE hr.biblio_linker_id IS NULL AND hr.deleted IS NULL
GROUP BY hr.biblio_linker_id
HAVING COUNT(DISTINCT hr.id)>1
  AND BOOL_OR(next_biblio_linker_similar_flag) IS TRUE
  AND BOOL_OR(bl_disadvantaged) IS TRUE;

CREATE INDEX tmp_bls_rest_idx ON tmp_bls_rest(row_id);