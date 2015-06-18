DROP TABLE IF EXISTS tmp_cluster_ids;

CREATE TABLE tmp_cluster_ids AS
	SELECT array_to_string(array_agg(harvested_record.id), ',') AS id_array 
		FROM harvested_record WHERE cluster_id IN 
			(SELECT cluster_id FROM harvested_record WHERE dedup_record_id IS NULL GROUP BY cluster_id HAVING count(cluster_id) > 1)
		GROUP BY cluster_id;

CREATE INDEX tmp_cluster_idx ON tmp_cluster_ids(id_array);
