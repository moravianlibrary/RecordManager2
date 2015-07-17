CREATE VIEW harvested_record_view AS
SELECT
  import_conf_id,
  record_id,
  deleted,
  format,
  convert_from(raw_record, 'UTF8') AS raw_record
FROM
  harvested_record
;

CREATE MATERIALIZED VIEW dedup_record_last_update_mat AS
SELECT
  dedup_record_id,
  last_update
FROM
  dedup_record_last_update
;
