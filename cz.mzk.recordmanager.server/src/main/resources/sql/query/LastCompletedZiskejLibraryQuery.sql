SELECT MAX(bje.end_time)
FROM
  batch_job_instance bji JOIN
  batch_job_execution bje ON bje.job_instance_id = bji.job_instance_id
WHERE
  job_name = :jobName AND
  bje.status = 'COMPLETED'