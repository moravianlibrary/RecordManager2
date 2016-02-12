SELECT MAX(bje.end_time)
FROM
  batch_job_instance bji JOIN
  batch_job_execution bje ON bje.job_instance_id = bji.job_instance_id JOIN
  batch_job_execution_params param_solr_url ON param_solr_url.job_execution_id = bje.job_execution_id AND param_solr_url.key_name = 'solrUrl' LEFT JOIN
  batch_job_execution_params param_from ON param_from.job_execution_id = bje.job_execution_id AND param_from.key_name = 'from'
WHERE
  job_name = :jobName AND
  bje.status = 'COMPLETED' AND
  param_solr_url.string_val = :solrUrl AND
  param_from.date_val IS NULL
