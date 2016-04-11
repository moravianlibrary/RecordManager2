CREATE OR REPLACE VIEW oai_harvest_job_stat AS
SELECT
  bje.job_execution_id,
  ohc.import_conf_id,
  l.name library_name,
  ohc.url url,
  ohc.set_spec,
  bje.start_time,
  bje.end_time,
  bje.status,
  from_param.date_val from_param,
  to_param.date_val to_param,
  (SELECT sum(read_count) FROM batch_step_execution bse WHERE bse.job_execution_id = bje.job_execution_id) no_of_records
FROM batch_job_instance bji
  JOIN batch_job_execution bje ON bje.job_instance_id = bji.job_instance_id
  JOIN batch_job_execution_params conf_id_param ON conf_id_param.job_execution_id = bje.job_execution_id AND conf_id_param.key_name = 'configurationId'
  LEFT JOIN batch_job_execution_params to_param ON to_param.job_execution_id = bje.job_execution_id AND to_param.key_name = 'to'
  LEFT JOIN batch_job_execution_params from_param ON from_param.job_execution_id = bje.job_execution_id AND from_param.key_name = 'from'
  JOIN oai_harvest_conf ohc ON ohc.import_conf_id = conf_id_param.long_val
  JOIN import_conf ic ON ic.id = ohc.import_conf_id
  JOIN library l ON l.id = ic.library_id
WHERE bji.job_name IN ('oaiHarvestJob', 'oaiReharvestJob', 'oaiPartitionedHarvestJob', 'cosmotronHarvestJob')
;

CREATE OR REPLACE VIEW oai_harvest_summary AS
WITH last_harvest_date AS (
  SELECT
    import_conf_id,
    COALESCE(MAX(CASE WHEN status = 'COMPLETED' THEN to_param END), MAX(CASE WHEN status = 'COMPLETED' THEN end_time END)) last_successful_harvest_date,
    COALESCE(MAX(CASE WHEN status = 'FAILED' THEN to_param END), MAX(CASE WHEN status = 'FAILED' THEN end_time END)) last_failed_harvest_date,
    COALESCE(MIN(end_time), MIN(to_param)) first_harvest_date,
    COUNT(1) no_of_harvests
  FROM oai_harvest_job_stat
  GROUP BY import_conf_id
)
SELECT l.name, ohc.url, ohc.set_spec, lhd.last_successful_harvest_date, lhd.last_failed_harvest_date, lhd.first_harvest_date, lhd.no_of_harvests
FROM last_harvest_date lhd
  JOIN import_conf ic ON ic.id = lhd.import_conf_id
  JOIN oai_harvest_conf ohc ON ohc.import_conf_id = ic.id
  JOIN library l ON l.id = ic.library_id
;

CREATE OR REPLACE VIEW oai_last_failed_harvests AS
SELECT name, url, set_spec, last_failed_harvest_date
FROM oai_harvest_summary
WHERE last_failed_harvest_date > last_successful_harvest_date OR (last_failed_harvest_date IS NOT NULL AND last_successful_harvest_date IS NULL)
;

CREATE OR REPLACE VIEW solr_index_summary AS
SELECT params1.string_val solr_url, COALESCE(MAX(params2.date_val), MAX(bje.end_time)) last_index_time
FROM batch_job_instance bji
  JOIN batch_job_execution bje ON bje.job_instance_id = bji.job_instance_id
  JOIN batch_job_execution_params params1 ON params1.job_execution_id = bje.job_execution_id AND params1.key_name = 'solrUrl'
  LEFT JOIN batch_job_execution_params params2 ON params2.job_execution_id = bje.job_execution_id AND params2.key_name = 'to'
WHERE bji.job_name IN ('indexRecordsToSolrJob', 'indexAllRecordsToSolrJob')
  AND bje.status = 'COMPLETED'
GROUP BY params1.string_val
;

