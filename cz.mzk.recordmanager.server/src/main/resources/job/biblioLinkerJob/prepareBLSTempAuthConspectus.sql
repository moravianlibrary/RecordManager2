DROP TABLE IF EXISTS tmp_bls_auth_conspectus;

CREATE TABLE tmp_bls_auth_conspectus AS
SELECT nextval('tmp_bl_id_seq') AS row_id,
  array_to_string(array_agg(hr.biblio_linker_id), ',') biblio_linker_id,
  array_to_string(array_agg(hr.id), ',') local_record_id,
  array_to_string(array_agg(DISTINCT hr.author_auth_key), ',') author_auth_key,
  array_to_string(array_agg(DISTINCT hr.bl_conspectus), ',') bl_conspectus
FROM harvested_record hr
JOIN import_conf ic ON hr.import_conf_id=ic.id
WHERE hr.author_auth_key IS NOT NULL AND hr.bl_conspectus IS NOT NULL
GROUP BY hr.author_auth_key, hr.bl_conspectus, ic.id_prefix
HAVING count(DISTINCT biblio_linker_id)>1;

CREATE INDEX tmp_bls_auth_conspectus_idx ON tmp_bls_auth_conspectus(row_id);