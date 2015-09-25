DROP TABLE IF EXISTS tmp_uuid_clusters;

CREATE TABLE tmp_uuid_clusters AS
SELECT
  nextval('tmp_table_id_seq') as row_id,
  uuid,
  array_to_string(array_agg(hr.id), ',')  id_array,
  count(distinct COALESCE(hr.dedup_record_id,1)) dedup_disticnt_count,
  count(hr.dedup_record_id) dedup_count
FROM harvested_record hr
WHERE hr.id NOT IN (
  SELECT hrfl.harvested_record_id FROM harvested_record_format_link hrfl 
  INNER JOIN harvested_record_format hrf ON hrf.id = hrfl.harvested_record_format_id
  WHERE hrf.name = 'PERIODICALS')
GROUP BY hr.uuid
HAVING COUNT(uuid) > 1 AND count(distinct COALESCE(hr.dedup_record_id,1)) >= count(hr.dedup_record_id) AND max(hr.updated) > (SELECT time FROM last_dedup_time);

CREATE INDEX tmp_uuid_clusters_idx ON tmp_uuid_clusters(row_id);