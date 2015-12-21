DROP TABLE IF EXISTS tmp_periodicals_issn_clusters;

CREATE TABLE tmp_periodicals_issn_clusters AS
SELECT
  nextval('tmp_table_id_seq') as row_id,
  issn,
  array_to_string(array_agg(hr.id), ',')  id_array
FROM harvested_record hr
  INNER JOIN issn i ON hr.id = i.harvested_record_id
  LEFT OUTER JOIN tmp_periodicals_ids tpi ON hr.id = tpi.id
WHERE tpi.id IS NOT NULL
GROUP BY i.issn
HAVING COUNT(DISTINCT hr.id) > 1 
  AND count(DISTINCT dedup_record_id) + sum(case when dedup_record_id is null then 1 else 0 end) != 1
  AND bool_or(next_dedup_flag) IS TRUE;
  
CREATE INDEX tmp_periodicals_issn_clusters_idx ON tmp_periodicals_issn_clusters(row_id);