DROP TABLE IF EXISTS tmp_disadvantaged_ismn;

CREATE TABLE tmp_disadvantaged_ismn AS
SELECT
  nextval('tmp_table_id_seq') AS row_id,
  i.ismn,
  hr.pages,
  hr.publication_year,
  array_to_string(array_agg(hr.id), ',')  id_array
FROM harvested_record hr
  INNER JOIN ismn i ON hr.id = i.harvested_record_id
WHERE hr.disadvantaged IS TRUE AND hr.publication_year IS NOT NULL AND hr.pages > 3
GROUP BY i.ismn,hr.pages,hr.publication_year
HAVING COUNT(DISTINCT hr.id) > 1 
  AND COUNT(DISTINCT dedup_record_id) + SUM(CASE WHEN dedup_record_id IS NULL THEN 1 ELSE 0 END) != 1
  AND bool_or(next_dedup_flag) IS TRUE;

CREATE INDEX tmp_disadvantaged_ismn_idx ON tmp_disadvantaged_ismn(row_id);