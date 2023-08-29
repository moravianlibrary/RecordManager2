CREATE TABLE IF NOT EXISTS tmp_stats_dedup
(
    id               SERIAL,
    job_execution_id DECIMAL(10),
    name             VARCHAR(100),
    type             VARCHAR(100),
    number           DECIMAL(10),
    time             TIMESTAMP,
    CONSTRAINT tmp_dedup_stats_pk PRIMARY KEY (id)
);

INSERT INTO tmp_stats_dedup (job_execution_id, name, type, number, time)
SELECT :job_execution_id AS job_execution_id,
       :name             AS name,
       :type             AS type,
       count(*)          AS number,
       :time             AS time
FROM harvested_record
WHERE dedup_record_id IS NULL;