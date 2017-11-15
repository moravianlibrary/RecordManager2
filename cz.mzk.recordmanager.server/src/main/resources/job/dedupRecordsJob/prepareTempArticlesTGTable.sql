DROP TABLE IF EXISTS tmp_simmilar_articles_tg;

CREATE TABLE tmp_simmilar_articles_tg AS
SELECT
  nextval('tmp_table_id_seq') as row_id,
  t.title,
  hr.publication_year,
  hr.author_string,
  hr.source_info_t,
  hr.source_info_g,
  array_to_string(array_agg(hr.id), ',')  id_array
FROM harvested_record hr 
  INNER JOIN title t ON hr.id = t.harvested_record_id
WHERE t.order_in_record = 1
  AND hr.source_info_t IS NOT NULL
  AND hr.source_info_g IS NOT NULL
GROUP BY t.title,hr.author_string,hr.publication_year,hr.source_info_t,hr.source_info_g
HAVING COUNT(DISTINCT hr.id) > 1 
  AND COUNT(DISTINCT dedup_record_id) + sum(case when dedup_record_id is null then 1 else 0 end) != 1
  AND bool_or(next_dedup_flag) IS TRUE;

CREATE INDEX tmp_articles_tg_idx ON tmp_simmilar_articles_tg(row_id);