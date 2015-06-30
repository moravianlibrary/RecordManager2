DROP TABLE IF EXISTS last_dedup_time; 

CREATE TABLE last_dedup_time AS
  SELECT COALESCE(MIN(updated),'1900-01-01 00:00:00.000') AS time FROM dedup_record
