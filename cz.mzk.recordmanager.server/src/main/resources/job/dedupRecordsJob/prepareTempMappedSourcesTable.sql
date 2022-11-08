DROP TABLE IF EXISTS tmp_simmilar_mapped_sources;

CREATE TABLE tmp_simmilar_mapped_sources AS
SELECT nextval('tmp_table_id_seq') AS         row_id,
       hr.raw_001_id,
       icmf.parent_import_conf_id,
       ARRAY_TO_STRING(ARRAY_AGG(hr.id), ',') id_array
FROM harvested_record hr
         INNER JOIN import_conf_mapping_field icmf ON hr.import_conf_id = icmf.import_conf_id
WHERE hr.raw_001_id IS NOT NULL
GROUP BY hr.raw_001_id, icmf.parent_import_conf_id
HAVING COUNT(DISTINCT hr.id) > 1
   AND COUNT(DISTINCT dedup_record_id) + SUM(CASE WHEN dedup_record_id IS NULL THEN 1 ELSE 0 END) != 1
   AND BOOL_OR(next_dedup_flag) IS TRUE;

CREATE INDEX tmp_simmilar_mapped_sources_idx ON tmp_simmilar_mapped_sources (row_id);
