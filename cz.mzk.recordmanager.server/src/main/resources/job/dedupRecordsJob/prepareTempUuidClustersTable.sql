DROP TABLE IF EXISTS tmp_uuid_clusters;

CREATE TABLE tmp_uuid_clusters AS
SELECT
  nextval('tmp_table_id_seq') as row_id,
  uuid,
  array_to_string(array_agg(hr.id), ',')  id_array
FROM harvested_record hr
WHERE hr.uuid IS NOT NULL
GROUP BY hr.uuid
HAVING COUNT(DISTINCT hr.id) > 1 
  AND count(DISTINCT dedup_record_id) + sum(case when dedup_record_id is null then 1 else 0 end) != 1
  AND bool_or(next_dedup_flag) IS TRUE;

CREATE INDEX tmp_uuid_clusters_idx ON tmp_uuid_clusters(row_id);