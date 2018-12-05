DROP TABLE IF EXISTS tmp_bls_auth;

CREATE TABLE tmp_bls_auth AS
SELECT nextval('tmp_bl_id_seq') AS row_id,
  array_to_string(array_agg(hr.biblio_linker_id), ',') biblio_linker_id,
  array_to_string(array_agg(ic.id_prefix || '.' || hr.record_id), ',') local_record_id,
  array_to_string(array_agg(DISTINCT hr.author_auth_key), ',') author_auth_key
FROM harvested_record hr
JOIN import_conf ic ON hr.import_conf_id=ic.id
WHERE hr.author_auth_key IS NOT NULL AND hr.biblio_linker_similar IS TRUE
GROUP BY hr.author_auth_key
HAVING count(*)>1;

CREATE INDEX tmp_bls_auth_idx ON tmp_bls_auth(row_id);