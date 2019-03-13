DROP TABLE IF EXISTS tmp_bl_rest_dedup;

CREATE TABLE tmp_bl_rest_dedup AS
SELECT nextval('tmp_bl_id_seq') AS row_id,
  array_to_string(array_agg(DISTINCT hr.dedup_record_id), ',') dedup_record_id
FROM harvested_record hr
WHERE hr.biblio_linker_id IS NULL AND dedup_record_id IS NOT NULL
GROUP BY hr.dedup_record_id
HAVING COUNT(*)>1
  AND bool_or(next_biblio_linker_flag) IS TRUE;

CREATE INDEX tmp_bl_rest_dedup_idx ON tmp_bl_rest_dedup(row_id);