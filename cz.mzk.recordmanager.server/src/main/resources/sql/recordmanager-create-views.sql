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
  NOT EXISTS(SELECT 1 FROM harvested_record hr WHERE hr.dedup_record_id = dr.id)
;


CREATE VIEW antikvariaty_url_view AS 
SELECT
  hr.dedup_record_id, 
  a.url
FROM harvested_record hr 
  INNER JOIN antikvariaty_catids ac on hr.cluster_id = ac.id_from_catalogue 
  INNER JOIN antikvariaty a on ac.antikvariaty_id = a.id 
ORDER BY hr.weight DESC
;