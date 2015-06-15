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
ALTER TABLE oai_harvest_conf ADD COLUMN id_prefix VARCHAR(10);

-- 10. 4. 2015 - xrosecky
ALTER TABLE harvested_record ADD COLUMN dedup_record_id DECIMAL(10);
UPDATE harvested_record hr 
  SET dedup_record_id = (
    SELECT
      dedup_record_id
    FROM
      record_link rl
    WHERE
      rl.harvested_record_id = hr.id
    LIMIT 1
  )
;
DROP VIEW dedup_record_orphaned;
DROP VIEW dedup_record_last_update;
DROP TABLE record_link;
DROP VIEW harvested_record_view;
ALTER TABLE harvested_record DROP COLUMN id;
DROP INDEX harvested_record_sec_key;
ALTER TABLE harvested_record ADD CONSTRAINT harvested_record_pk PRIMARY KEY (oai_harvest_conf_id, oai_record_id);
ALTER TABLE harvested_record RENAME oai_record_id TO record_id;
CREATE VIEW dedup_record_last_update AS
SELECT
  dr.id dedup_record_id,
  MAX(CASE WHEN dr.updated > hr.harvested THEN dr.updated ELSE hr.harvested END) last_update
FROM
  dedup_record dr JOIN 
  harvested_record hr ON hr.dedup_record_id = dr.id 
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
  NOT EXISTS(SELECT 1 FROM harvested_record hr WHERE hr.dedup_record_id = dr.id)
;
CREATE VIEW harvested_record_view AS
SELECT
  oai_harvest_conf_id,
  record_id,
  deleted,
  format,
  isbn,
  title,
  convert_from(raw_record, 'UTF8') AS raw_record
FROM
  harvested_record
;

-- 4. 5. 2015 - xrosecky
ALTER TABLE harvested_record ADD COLUMN id DECIMAL(10);
CREATE SEQUENCE harvested_record_id_seq START 1;
UPDATE harvested_record SET id = nextval('harvested_record_id_seq');
ALTER TABLE harvested_record DROP CONSTRAINT harvested_record_pk;
ALTER TABLE harvested_record ADD CONSTRAINT harvested_record_pk PRIMARY KEY (id);

-- 6. 5. 2015 - mertam
ALTER TABLE harvested_record DROP COLUMN title;
ALTER TABLE harvested_record DROP COLUMN isbn;
ALTER TABLE harvested_record DROP COLUMN physical_format; 

-- 7. 5. 2015 - tomascejpek
ALTER TABLE harvested_record ADD COLUMN weight DECIMAL(10);
ALTER TABLE oai_harvest_conf ADD COLUMN base_weight DECIMAL(10);

-- 12. 5. 2015 - tomascejpek
ALTER TABLE harvested_record ADD COLUMN cluster_id VARCHAR(20);
ALTER TABLE oai_harvest_conf ADD COLUMN cluster_id_enabled BOOLEAN DEFAULT(FALSE);

-- 4. 6. 2015 mertam
ALTER TABLE harvested_record ADD COLUMN pages DECIMAL(10);

-- 12. 6. 2015 mertam 
ALTER TABLE oai_harvest_conf ADD COLUMN id_prefix VARCHAR(10);
