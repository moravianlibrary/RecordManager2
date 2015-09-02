DROP TABLE IF EXISTS tmp_oclc_clusters;

CREATE TABLE tmp_oclc_clusters AS
SELECT
  nextval('tmp_table_id_seq') as row_id,
  oclc,
  array_to_string(array_agg(hr.id), ',')  id_array,
  count(distinct COALESCE(hr.dedup_record_id,1)) dedup_disticnt_count,
  count(hr.dedup_record_id) dedup_count
FROM harvested_record hr
  INNER JOIN oclc o ON hr.id = o.harvested_record_id
GROUP BY o.oclc 
HAVING COUNT(oclc) > 1 AND count(distinct COALESCE(hr.dedup_record_id,1)) >= count(hr.dedup_record_id) AND max(hr.updated) > (SELECT time FROM last_dedup_time);

CREATE INDEX tmp_oclc_clusters_idx ON tmp_oclc_clusters(row_id);