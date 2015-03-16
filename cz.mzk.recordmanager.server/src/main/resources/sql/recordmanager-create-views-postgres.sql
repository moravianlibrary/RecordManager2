CREATE VIEW harvested_record_view AS
SELECT
  id,
  oai_harvest_conf_id,
  oai_record_id,
  deleted,
  format,
  isbn,
  title,
  encode(raw_record, 'escape') AS raw_record
FROM
  harvested_record
;
