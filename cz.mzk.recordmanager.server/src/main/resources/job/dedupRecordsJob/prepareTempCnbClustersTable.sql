DROP TABLE IF EXISTS tmp_cnb_clusters;

CREATE TABLE tmp_cnb_clusters AS
SELECT 
  cnb,
  array_to_string(array_agg(hr.id), ',')  id_array,
  count(distinct COALESCE(hr.dedup_record_id,1)) dedup_disticnt_count,
  count(hr.dedup_record_id) dedup_count
FROM harvested_record hr
  INNER JOIN cnb c ON hr.id = c.harvested_record_id
GROUP BY c.cnb 
HAVING COUNT(cnb) > 1 AND count(distinct COALESCE(hr.dedup_record_id,1)) >= count(hr.dedup_record_id)