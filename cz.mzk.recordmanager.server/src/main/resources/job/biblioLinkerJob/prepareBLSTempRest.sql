update harvested_record hr set next_biblio_linker_similar_flag=false WHERE next_biblio_linker_similar_flag=TRUE
    AND EXISTS (SELECT 1 FROM biblio_linker_similar bls WHERE hr.id=bls.harvested_record_id);

DROP TABLE IF EXISTS tmp_bls_rest;

CREATE TABLE tmp_bls_rest AS
SELECT nextval('tmp_bl_id_seq') AS row_id,
  array_to_string(array_agg(DISTINCT hr.biblio_linker_id), ',') biblio_linker_id,
  array_to_string(array_agg(hr.id), ',') local_record_id
FROM harvested_record hr
WHERE hr.deleted IS NULL
GROUP BY hr.biblio_linker_id
HAVING COUNT(DISTINCT hr.id)>1
  AND bool_or(next_biblio_linker_similar_flag) IS TRUE;

CREATE INDEX tmp_bls_rest_idx ON tmp_bls_rest(row_id);