DROP TABLE IF EXISTS tmp_cluster_ids;
DROP TABLE IF EXISTS tmp_cluster_id_keys;

-- Cluster IDs that contain at least one record scheduled for dedup.
-- Incremental runs then join only these keys instead of aggregating every clustered record.
CREATE UNLOGGED TABLE tmp_cluster_id_keys AS
SELECT DISTINCT hr.cluster_id
FROM harvested_record hr
WHERE hr.next_dedup_flag IS TRUE
  AND hr.cluster_id IS NOT NULL;

CREATE INDEX tmp_cluster_id_keys_idx ON tmp_cluster_id_keys(cluster_id);

CREATE UNLOGGED TABLE tmp_cluster_ids AS
SELECT
  nextval('tmp_table_id_seq') AS row_id,
  array_to_string(array_agg(hr.id), ',') AS id_array
FROM harvested_record hr
INNER JOIN tmp_cluster_id_keys k ON k.cluster_id = hr.cluster_id
GROUP BY hr.cluster_id
HAVING COUNT(hr.id) > 1;

DROP TABLE tmp_cluster_id_keys;

CREATE INDEX tmp_cluster_idx ON tmp_cluster_ids(row_id);
