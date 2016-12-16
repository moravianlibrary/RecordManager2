DROP TABLE IF EXISTS tmp_simmilar_ean;

CREATE TABLE tmp_simmilar_ean AS
SELECT
  nextval('tmp_table_id_seq') as row_id,
  e.ean,
  t.title,
  hr.publication_year,
  hrl.harvested_record_format_id,
  array_to_string(array_agg(hr.id), ',')  id_array
FROM harvested_record hr 
  INNER JOIN ean e ON hr.id = e.harvested_record_id 
  INNER JOIN title t ON hr.id = t.harvested_record_id
  INNER JOIN harvested_record_format_link hrl ON hr.id = hrl.harvested_record_id
  LEFT OUTER JOIN tmp_audio_ids tpi ON hr.id = tpi.id
WHERE t.order_in_record = 1 
  AND e.order_in_record = 1
  AND tpi.id IS NOT NULL
GROUP BY e.ean,t.title,hr.publication_year,hrl.harvested_record_format_id
HAVING COUNT(DISTINCT hr.id) > 1 
  AND count(DISTINCT dedup_record_id) + sum(case when dedup_record_id is null then 1 else 0 end) != 1
  AND bool_or(next_dedup_flag) IS TRUE;

CREATE INDEX tmp_ean_idx ON tmp_simmilar_ean(row_id);