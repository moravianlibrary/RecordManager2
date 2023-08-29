CREATE TABLE IF NOT EXISTS tmp_stats_bibliolinker
(
    id               SERIAL,
    job_execution_id DECIMAL(10),
    name             VARCHAR(100),
    type             VARCHAR(100),
    number           DECIMAL(10),
    time             TIMESTAMP,
    CONSTRAINT tmp_stats_bibliolinker_pk PRIMARY KEY (id)
);

INSERT INTO tmp_stats_bibliolinker (job_execution_id, name, type, number, time)
SELECT :job_execution_id AS job_execution_id,
       :name             AS name,
       :type             AS type,
       count(*)          AS number,
       :time             AS time
FROM harvested_record
WHERE biblio_linker_id IS NULL;