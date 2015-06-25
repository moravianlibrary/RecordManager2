DROP TABLE IF EXISTS tmp_uuid_clusters;

CREATE TABLE tmp_uuid_clusters AS
SELECT 
  uuid,
  array_to_string(array_agg(hr.id), ',')  id_array,
  count(distinct COALESCE(hr.dedup_record_id,1)) dedup_disticnt_count,
  count(hr.dedup_record_id) dedup_count
FROM harvested_record hr
GROUP BY hr.uuid
HAVING COUNT(uuid) > 1 AND count(distinct COALESCE(hr.dedup_record_id,1)) >= count(hr.dedup_record_id)