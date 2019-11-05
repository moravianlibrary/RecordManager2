DROP TABLE IF EXISTS tmp_disadvantaged_pages;

CREATE TABLE tmp_disadvantaged_pages AS
SELECT
  nextval('tmp_table_id_seq') AS row_id,
  t.short_title,
  hr.author_string,
  hr.pages,
  hr.publication_year,
  array_to_string(array_agg(hr.id), ',')  id_array
FROM harvested_record hr
  INNER JOIN short_title t ON hr.id = t.harvested_record_id
WHERE hr.disadvantaged IS TRUE AND hr.author_string IS NOT NULL AND hr.pages IS NOT NULL AND hr.publication_year IS NOT NULL
GROUP BY t.short_title,hr.author_string,hr.pages,hr.publication_year
HAVING COUNT(DISTINCT hr.id) > 1 
  AND COUNT(DISTINCT dedup_record_id) + SUM(CASE WHEN dedup_record_id IS NULL THEN 1 ELSE 0 END) != 1
  AND bool_or(next_dedup_flag) IS TRUE;

CREATE INDEX tmp_disadvantaged_pages_idx ON tmp_disadvantaged_pages(row_id);