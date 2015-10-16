WITH records AS (
  SELECT id harvested_record_id, nextval('dedup_record_id_seq') dedup_record_id, NOW() updated
  FROM harvested_record
  WHERE dedup_record_id IS NULL AND id > ?
  ORDER BY id
  LIMIT 2000
),
ins AS (INSERT INTO dedup_record(id, updated) SELECT dedup_record_id, updated FROM records),
upd AS (UPDATE harvested_record
  SET dedup_record_id = (SELECT dedup_record_id FROM records WHERE records.harvested_record_id = id)
  WHERE id IN (SELECT harvested_record_id FROM records))
SELECT MAX(harvested_record_id) FROM records;
