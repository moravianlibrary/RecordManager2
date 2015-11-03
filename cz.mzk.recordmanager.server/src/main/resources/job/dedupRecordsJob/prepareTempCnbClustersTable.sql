DROP TABLE IF EXISTS tmp_cnb_clusters;

CREATE TABLE tmp_cnb_clusters AS
SELECT
  nextval('tmp_table_id_seq') as row_id,
  cnb,
  array_to_string(array_agg(hr.id), ',')  id_array
FROM harvested_record hr
  INNER JOIN cnb c ON hr.id = c.harvested_record_id
WHERE hr.id NOT IN (
  SELECT hrfl.harvested_record_id FROM harvested_record_format_link hrfl 
  INNER JOIN harvested_record_format hrf ON hrf.id = hrfl.harvested_record_format_id
  WHERE hrf.name = 'PERIODICALS')
GROUP BY c.cnb
HAVING COUNT(DISTINCT hr.id) > 1 
  AND count(DISTINCT dedup_record_id) + sum(case when dedup_record_id is null then 1 else 0 end) != 1
  AND max(hr.updated) > ALL(SELECT time FROM last_dedup_time);


CREATE INDEX tmp_cnb_clusters_idx ON tmp_cnb_clusters(row_id);