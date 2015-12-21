DROP TABLE IF EXISTS tmp_periodicals_title_clusters;

CREATE TABLE tmp_periodicals_title_clusters AS

WITH 
  format_periodicals AS (select id from harvested_record_format where name = 'PERIODICALS')
SELECT
  nextval('tmp_table_id_seq') as row_id,
  title,
  array_to_string(array_agg(hr.id), ',') as id_array
FROM harvested_record hr
  INNER JOIN title t ON t.harvested_record_id = hr.id
  INNER JOIN harvested_record_format_link hrl ON hrl.harvested_record_id = hr.id
  LEFT OUTER JOIN tmp_periodicals_ids tpi ON hr.id = tpi.id
WHERE tpi.id IS NOT NULL
GROUP BY t.title,hrl.harvested_record_format_id
HAVING COUNT(DISTINCT hr.id) > 1 
  AND count(DISTINCT dedup_record_id) + sum(case when dedup_record_id is null then 1 else 0 end) != 1
  AND bool_or(next_dedup_flag) IS TRUE;
  
CREATE INDEX tmp_periodicals_title_clusters_idx ON tmp_periodicals_title_clusters(row_id);