DROP TABLE IF EXISTS tmp_disadvantaged_isbn;

CREATE TABLE tmp_disadvantaged_isbn AS
SELECT
  nextval('tmp_table_id_seq') AS row_id,
  i.isbn,
  t.anp_title,
  hr.pages,
  hr.publication_year,
  array_to_string(array_agg(hr.id), ',')  id_array
FROM harvested_record hr
  INNER JOIN anp_title t ON hr.id = t.harvested_record_id
  INNER JOIN isbn i ON hr.id = i.harvested_record_id
WHERE hr.disadvantaged IS TRUE AND hr.publication_year IS NOT NULL AND hr.pages IS NOT NULL
GROUP BY i.isbn,t.anp_title,hr.pages,hr.publication_year
HAVING COUNT(DISTINCT hr.id) > 1 
  AND COUNT(DISTINCT dedup_record_id) + SUM(CASE WHEN dedup_record_id IS NULL THEN 1 ELSE 0 END) != 1
  AND bool_or(next_dedup_flag) IS TRUE;

CREATE INDEX tmp_disadvantaged_isbn_idx ON tmp_disadvantaged_isbn(row_id);