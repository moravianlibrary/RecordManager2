CREATE TABLE IF NOT EXISTS tmp_stats_index
(
    id               SERIAL,
    job_execution_id DECIMAL(10),
    import_conf_id   DECIMAL(10),
    prefix           VARCHAR(30),
    updated          DECIMAL(10),
    deleted          DECIMAL(10),
    from_date        TIMESTAMP,
    to_date          TIMESTAMP,
    CONSTRAINT tmp_index_stats_pk PRIMARY KEY (id)
);

INSERT INTO tmp_stats_index (job_execution_id, import_conf_id, prefix, updated, deleted, from_date, to_date)
SELECT :job_execution_id                  AS job_execution_id,
       hr.import_conf_id                  AS import_conf_id,
       ic.id_prefix                       AS prefix,
       count(*)                           AS updated,
       sum((hr.deleted is not null)::int) AS deleted,
       :from                              AS from_date,
       :to                                AS to_date
FROM harvested_record hr
         JOIN import_conf ic ON hr.import_conf_id = ic.id
WHERE hr.updated > :from
  and hr.updated < :to
group by hr.import_conf_id, ic.id_prefix;