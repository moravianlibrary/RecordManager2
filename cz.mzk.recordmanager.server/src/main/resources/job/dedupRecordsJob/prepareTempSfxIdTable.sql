DROP TABLE IF EXISTS tmp_simmilar_sfx_id;

CREATE TABLE tmp_simmilar_sfx_id AS
SELECT
  nextval('tmp_table_id_seq') as row_id,
  hr.raw_001_id,
  array_to_string(array_agg(hr.id), ',')  id_array
FROM harvested_record hr
INNER JOIN tmp_sfx_ids tsil ON tsil.id = hr.id
GROUP BY hr.raw_001_id
HAVING COUNT(DISTINCT hr.id) > 1 
  AND COUNT(DISTINCT dedup_record_id) + SUM(CASE WHEN dedup_record_id IS NULL THEN 1 ELSE 0 END) != 1
  AND bool_or(next_dedup_flag) IS TRUE;

CREATE INDEX tmp_simmilar_sfx_id_idx ON tmp_simmilar_sfx_id(row_id);