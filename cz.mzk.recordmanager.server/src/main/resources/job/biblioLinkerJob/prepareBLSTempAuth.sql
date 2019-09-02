DROP TABLE IF EXISTS tmp_bls_auth;

CREATE TABLE tmp_bls_auth AS
SELECT nextval('tmp_bl_id_seq') AS row_id,
  array_to_string(array_agg(DISTINCT hr.biblio_linker_id), ',') biblio_linker_id,
  array_to_string(array_agg(hr.id), ',') local_record_id,
  array_to_string(array_agg(DISTINCT hr.author_auth_key), ',') author_auth_key
FROM harvested_record hr
WHERE hr.author_auth_key IS NOT NULL
GROUP BY hr.author_auth_key
HAVING COUNT(DISTINCT biblio_linker_id)>1
  AND bool_or(next_biblio_linker_flag) IS TRUE;

CREATE INDEX tmp_bls_auth_idx ON tmp_bls_auth(row_id);