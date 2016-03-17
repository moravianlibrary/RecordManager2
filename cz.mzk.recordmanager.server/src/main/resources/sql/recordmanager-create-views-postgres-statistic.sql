CREATE OR REPLACE VIEW oai_harvest_job_stat AS
WITH last_harvest_date AS (
  SELECT ohc.import_conf_id import_conf_id, COALESCE(MAX(to_param.date_val), MAX(bje.end_time)) last_harvest_date, COALESCE(MIN(to_param.date_val), MIN(bje.end_time)) first_harvest_date, COUNT(1) no_of_harvests
  FROM batch_job_instance bji
    JOIN batch_job_execution bje ON bje.job_instance_id = bji.job_instance_id
    JOIN batch_job_execution_params conf_id_param ON conf_id_param.job_execution_id = bje.job_execution_id AND conf_id_param.key_name = 'configurationId'
    JOIN oai_harvest_conf ohc ON ohc.import_conf_id = conf_id_param.long_val
    LEFT JOIN batch_job_execution_params to_param ON to_param.job_execution_id = bje.job_execution_id AND to_param.key_name = 'to'
  WHERE bji.job_name IN ('oaiHarvestJob', 'oaiReharvestJob')
    AND bje.status = 'COMPLETED'
  GROUP BY ohc.import_conf_id
)
SELECT l.name, ohc.url, ohc.set_spec, lhd.last_harvest_date, lhd.first_harvest_date, lhd.no_of_harvests
FROM last_harvest_date lhd
  JOIN import_conf ic ON ic.id = lhd.import_conf_id
  JOIN oai_harvest_conf ohc ON ohc.import_conf_id = ic.id
  JOIN library l ON l.id = ic.library_id;

CREATE OR REPLACE VIEW solr_index_job_stat AS
SELECT params1.string_val solr_url, COALESCE(MAX(params2.date_val), MAX(bje.end_time)) last_index_time
FROM batch_job_instance bji
  JOIN batch_job_execution bje ON bje.job_instance_id = bji.job_instance_id
  JOIN batch_job_execution_params params1 ON params1.job_execution_id = bje.job_execution_id AND params1.key_name = 'solrUrl'
  LEFT JOIN batch_job_execution_params params2 ON params2.job_execution_id = bje.job_execution_id AND params2.key_name = 'to'
WHERE bji.job_name IN ('indexRecordsToSolrJob', 'indexAllRecordsToSolrJob')
  AND bje.status = 'COMPLETED'
GROUP BY params1.string_val;
