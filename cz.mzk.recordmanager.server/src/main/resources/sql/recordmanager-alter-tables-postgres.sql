-- 19. 2. 2015 - xrosecky
ALTER TABLE dedup_record ADD updated TIMESTAMP;

DROP VIEW dedup_record_last_update;

CREATE VIEW dedup_record_last_update AS
SELECT
  dr.id dedup_record_id,
  MAX(CASE WHEN dr.updated > hr.harvested THEN dr.updated ELSE hr.harvested END) last_update
FROM
  dedup_record dr JOIN 
  record_link rl ON rl.dedup_record_id = dr.id JOIN
  harvested_record hr ON hr.id = rl.harvested_record_id
GROUP BY
  dr.id
;

CREATE VIEW dedup_record_orphaned AS
SELECT
  dr.id dedup_record_id,
  dr.updated AS orphaned
FROM
  dedup_record dr
WHERE
  NOT EXISTS(SELECT 1 FROM record_link rl WHERE rl.dedup_record_id = dr.id)
;

-- 2. 3. 2015 - xrosecky
ALTER TABLE harvested_record ADD publication_year  DECIMAL(4),
                             ADD physical_format   VARCHAR(255)
;

ALTER TABLE dedup_record ADD publication_year  DECIMAL(4),
                         ADD physical_format   VARCHAR(255)
;

-- 26. 3. 2015 - mertam
ALTER TABLE harvested_record ADD unique_id VARCHAR(100) UNIQUE;

-- 3. 4. 2015 - tomascejpek
ALTER TABLE harvested_record DROP unique_id;
ALTER TABLE harvested_record RENAME oai_record_id TO record_id;

-- 7. 4. 2015 - xrosecky
ALTER TABLE harvested_record ADD COLUMN id_prefix VARCHAR(10);
