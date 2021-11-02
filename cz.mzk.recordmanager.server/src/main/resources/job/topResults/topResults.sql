UPDATE dedup_record SET updated=localtimestamp WHERE id IN (
  SELECT dedup_record_id FROM harvested_record WHERE id IN (
    SELECT harvested_record_id FROM inspiration WHERE name = 'top_results'));

DELETE FROM inspiration WHERE name = 'top_results';

WITH dedup AS
  (
    SELECT dedup_record_id
      FROM harvested_record hr
      JOIN harvested_record_format_link hrfl ON hr.id = hrfl.harvested_record_id AND harvested_record_format_id = 1
      WHERE publication_year >= :year
      GROUP BY dedup_record_id
      HAVING COUNT(*) > :dedupCount
  ),
candidates AS
  (
    SELECT id
      FROM harvested_record hr
      JOIN dedup d ON hr.dedup_record_id = d.dedup_record_id
      WHERE hr.import_conf_id = 300
      ORDER BY random()
      LIMIT :results
  )
INSERT INTO inspiration (harvested_record_id, name)
  SELECT id AS harvested_record_id, 'top_results' AS name
  FROM harvested_record
  WHERE id IN (SELECT id FROM candidates);

UPDATE dedup_record SET updated=localtimestamp WHERE id IN (
  SELECT dedup_record_id FROM harvested_record WHERE id IN (
    SELECT harvested_record_id FROM inspiration WHERE name = 'top_results'));
