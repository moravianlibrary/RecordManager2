CREATE OR REPLACE VIEW harvested_record_view AS
SELECT
  import_conf_id,
  record_id,
  deleted,
  format,
  convert_from(raw_record, 'UTF8') AS raw_record
FROM
  harvested_record
;

CREATE OR REPLACE VIEW dedup_record_last_update AS
SELECT
  dr.id dedup_record_id,
  GREATEST(dr.updated, (SELECT MAX(updated) FROM harvested_record hr WHERE hr.dedup_record_id = dr.id)) last_update
FROM
  dedup_record dr
;

CREATE MATERIALIZED VIEW dedup_record_last_update_mat AS
SELECT
  dedup_record_id,
  last_update
FROM
  dedup_record_last_update
;
