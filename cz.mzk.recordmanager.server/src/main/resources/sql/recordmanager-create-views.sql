CREATE VIEW dedup_record_last_update AS
SELECT
  dr.id dedup_record_id,
  CASE
    WHEN dr.updated > (SELECT MAX(updated) FROM harvested_record hr WHERE hr.dedup_record_id = dr.id)
       THEN dr.updated
       ELSE (SELECT MAX(updated) FROM harvested_record hr WHERE hr.dedup_record_id = dr.id)
   END last_update
FROM
  dedup_record dr
;

CREATE VIEW dedup_record_orphaned AS
SELECT
  dr.id dedup_record_id,
  dr.updated AS orphaned
FROM
  dedup_record dr
WHERE
  NOT EXISTS(SELECT 1 FROM harvested_record hr WHERE hr.dedup_record_id = dr.id and deleted is null)
;

CREATE VIEW antikvariaty_url_view AS
SELECT
  hr.dedup_record_id, 
  a.url,
  a.updated,
  a.last_harvest
FROM harvested_record hr 
  INNER JOIN antikvariaty_catids ac on hr.cluster_id = ac.id_from_catalogue 
  INNER JOIN antikvariaty a on ac.antikvariaty_id = a.id 
ORDER BY hr.weight DESC
;

CREATE VIEW cosmotron_periodicals_last_update AS
SELECT
  hr.id harvested_record_id,
  hr.import_conf_id,
  hr.record_id,
  CASE
    WHEN hr.updated > (SELECT MAX(updated) FROM cosmotron_996 c996 WHERE c996.import_conf_id = hr.import_conf_id
    AND c996.parent_record_id = hr.record_id)
      THEN hr.updated
      ELSE (SELECT MAX(updated) FROM cosmotron_996 c996 WHERE c996.import_conf_id = hr.import_conf_id
    AND c996.parent_record_id = hr.record_id)
  END last_update
FROM
  harvested_record hr
WHERE
  EXISTS(SELECT 1 FROM cosmotron_996 c996 WHERE c996.import_conf_id = hr.import_conf_id
    AND c996.parent_record_id = hr.record_id)
;

CREATE VIEW kram_availability_view AS
SELECT
  hr.dedup_record_id,
  ka.import_conf_id,
  ka.uuid,
  ka.type,
  ka.updated,
  ka.last_harvest
FROM harvested_record hr
  INNER JOIN uuid on uuid.harvested_record_id = hr.id
  INNER JOIN kram_availability ka on ka.uuid = uuid.uuid
;
