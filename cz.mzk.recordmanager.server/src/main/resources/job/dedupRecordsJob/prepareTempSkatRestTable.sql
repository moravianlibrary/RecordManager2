DROP TABLE IF EXISTS tmp_skat_keys_rest;

-- order of ids in field 'id_array' matters, first expected is Skat record
CREATE TABLE tmp_skat_keys_rest AS
SELECT 
  nextval('tmp_table_id_seq') as row_id,
  skat_record_id,
  array_to_string(array_prepend(skat_record_id,array_agg(hr.id)), ',')  id_array
FROM skat_keys sk 
  INNER JOIN sigla s ON sk.sigla = s.sigla
  INNER JOIN harvested_record hr ON hr.import_conf_id = s.import_conf_id AND hr.raw_001_id = sk.local_record_id
GROUP BY sk.skat_record_id
HAVING bool_or(next_dedup_flag) IS TRUE AND bool_or(sk.manually_merged) IS NOT TRUE AND count(hr.id) > 1;

CREATE INDEX tmp_skat_keys_rest_idx ON tmp_skat_keys_rest(row_id);