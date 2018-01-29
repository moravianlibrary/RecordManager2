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

CREATE OR REPLACE VIEW dedup_record_orphaned AS
SELECT
  dr.id dedup_record_id,
  GREATEST(dr.updated, (SELECT MAX(updated) FROM harvested_record hr WHERE hr.dedup_record_id = dr.id)) orphaned
FROM
  dedup_record dr
WHERE
  NOT EXISTS(SELECT 1 FROM harvested_record hr WHERE hr.dedup_record_id = dr.id and deleted is null)
;

CREATE MATERIALIZED VIEW dedup_record_last_update_mat AS
SELECT
  dedup_record_id,
  last_update
FROM
  dedup_record_last_update
;

CREATE OR REPLACE VIEW cosmotron_periodicals_last_update AS
SELECT
  hr.id harvested_record_id,
  hr.import_conf_id,
  hr.record_id,
  GREATEST(hr.updated, (SELECT MAX(updated) FROM cosmotron_996 c996 WHERE c996.import_conf_id = hr.import_conf_id
    AND c996.parent_record_id = hr.record_id)) last_update
FROM
  harvested_record hr
WHERE
  EXISTS(SELECT 1 FROM cosmotron_996 c996 WHERE c996.import_conf_id = hr.import_conf_id
    AND c996.parent_record_id = hr.record_id)
;
