CREATE VIEW harvested_record_view AS
SELECT
  oai_harvest_conf_id,
  oai_record_id,
  deleted,
  format,
  isbn,
  title,
  convert_from(raw_record, 'UTF8') AS raw_record
FROM
  harvested_record
;
