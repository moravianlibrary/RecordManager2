SELECT COALESCE(MAX(params1.date_val), MAX(bje.end_time))
FROM
  batch_job_instance bji JOIN
  batch_job_execution bje ON bje.job_instance_id = bji.job_instance_id JOIN
  batch_job_execution_params params1 ON params1.job_execution_id = bje.job_execution_id AND params1.key_name = 'to'
WHERE
  job_name = :jobName AND
  bje.status = 'COMPLETED'