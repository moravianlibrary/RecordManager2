DROP TABLE IF EXISTS tmp_simmilar_books_cnb;

CREATE TABLE tmp_simmilar_books_cnb AS
SELECT
  nextval('tmp_table_id_seq') as row_id,
  c.cnb,
  t.title,
  hr.publication_year,
  hrl.harvested_record_format_id,
  array_to_string(array_agg(hr.id), ',')  id_array
FROM harvested_record hr 
  INNER JOIN cnb c ON hr.id = c.harvested_record_id 
  INNER JOIN title t ON hr.id = t.harvested_record_id
  INNER JOIN harvested_record_format_link hrl on hr.id = hrl.harvested_record_id
  LEFT OUTER JOIN tmp_periodicals_ids tpi ON hr.id = tpi.id
WHERE t.order_in_record = 1
  AND tpi.id IS NULL
GROUP BY c.cnb,t.title,hr.publication_year,hrl.harvested_record_format_id
HAVING COUNT(DISTINCT hr.id) > 1 
  AND count(DISTINCT dedup_record_id) + sum(case when dedup_record_id is null then 1 else 0 end) != 1
  AND max(hr.updated) > ALL(SELECT time FROM last_dedup_time);
		
CREATE INDEX tmp_cbn_idx ON tmp_simmilar_books_cnb(row_id);