DROP TABLE IF EXISTS tmp_simmilar_articles;

CREATE TABLE tmp_simmilar_articles AS
SELECT
  nextval('tmp_table_id_seq') as row_id,
  t.title,
  hr.publication_year,
  hr.author_string,
  hr.source_info,
  hrl.harvested_record_format_id,
  array_to_string(array_agg(hr.id), ',')  id_array
FROM harvested_record hr 
  INNER JOIN title t ON hr.id = t.harvested_record_id
  INNER JOIN harvested_record_format_link hrl ON hr.id = hrl.harvested_record_id
  LEFT OUTER JOIN tmp_articles_ids tpi ON hr.id = tpi.id
WHERE t.order_in_record = 1 
  AND tpi.id IS NOT NULL
GROUP BY t.title,hr.author_string,hr.source_info,hr.publication_year,hrl.harvested_record_format_id
HAVING COUNT(DISTINCT hr.id) > 1 
  AND count(DISTINCT dedup_record_id) + sum(case when dedup_record_id is null then 1 else 0 end) != 1
  AND bool_or(next_dedup_flag) IS TRUE;

CREATE INDEX tmp_articles_idx ON tmp_simmilar_articles(row_id);