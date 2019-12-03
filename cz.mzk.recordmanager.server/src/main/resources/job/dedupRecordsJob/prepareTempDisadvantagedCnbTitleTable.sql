DROP TABLE IF EXISTS tmp_disadvantaged_cnb_title;

CREATE TABLE tmp_disadvantaged_cnb_title AS
SELECT
  nextval('tmp_table_id_seq') AS row_id,
  c.cnb,
  t.title,
  array_to_string(array_agg(hr.id), ',')  id_array
FROM harvested_record hr
  INNER JOIN cnb c ON hr.id = c.harvested_record_id
  INNER JOIN title t ON hr.id = t.harvested_record_id
WHERE hr.disadvantaged IS TRUE
GROUP BY c.cnb,t.title
HAVING COUNT(DISTINCT hr.id) > 1 
  AND COUNT(DISTINCT dedup_record_id) + SUM(CASE WHEN dedup_record_id IS NULL THEN 1 ELSE 0 END) != 1
  AND bool_or(next_dedup_flag) IS TRUE;

CREATE INDEX tmp_disadvantaged_cnb_title_idx ON tmp_disadvantaged_cnb_title(row_id);