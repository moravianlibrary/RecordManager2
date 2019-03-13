DROP TABLE IF EXISTS tmp_bls_conspectus;

CREATE TABLE tmp_bls_conspectus AS
SELECT nextval('tmp_bl_id_seq') AS row_id,
  array_to_string(array_agg(DISTINCT hr.biblio_linker_id), ',') biblio_linker_id,
  array_to_string(array_agg(hr.id), ',') local_record_id,
  array_to_string(array_agg(DISTINCT hr.bl_conspectus), ',') bl_conspectus
FROM harvested_record hr
JOIN import_conf ic ON hr.import_conf_id=ic.id
WHERE hr.bl_conspectus IS NOT NULL
GROUP BY hr.bl_conspectus
HAVING COUNT(DISTINCT biblio_linker_id)>1
  AND COUNT(*)<30000
  AND bool_or(next_biblio_linker_flag) IS TRUE;

CREATE INDEX tmp_bls_conspectus_idx ON tmp_bls_conspectus(row_id);