DROP TABLE IF EXISTS tmp_cluster_ids;

CREATE TABLE tmp_cluster_ids AS
SELECT
  nextval('tmp_table_id_seq') AS row_id,
  array_to_string(array_agg(DISTINCT id), ',') AS id_array
FROM (
  SELECT hr.id,hr.cluster_id,hr.next_dedup_flag
    FROM harvested_record hr 
      INNER JOIN (SELECT cluster_id FROM harvested_record WHERE cluster_id IS NOT NULL) cli ON hr.cluster_id = cli.cluster_id
) AS sub
GROUP BY sub.cluster_id
HAVING COUNT(sub.id) > 1 AND bool_or(sub.next_dedup_flag) IS TRUE;
  
CREATE INDEX tmp_cluster_idx ON tmp_cluster_ids(row_id);
