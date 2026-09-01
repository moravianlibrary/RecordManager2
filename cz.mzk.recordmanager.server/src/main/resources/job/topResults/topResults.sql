UPDATE dedup_record SET updated = localtimestamp WHERE id IN (
  SELECT dedup_record_id FROM harvested_record WHERE id IN (
    SELECT harvested_record_id FROM harvested_record_inspiration WHERE inspiration_id = :inspiration_id));

DELETE FROM harvested_record_inspiration WHERE inspiration_id = :inspiration_id;

DROP TABLE IF EXISTS tmp_top_results_dedup;

-- One pass over books for the year; all three pools reuse these aggregates.
CREATE TABLE tmp_top_results_dedup AS
SELECT
  hr.dedup_record_id,
  COUNT(*) AS cnt,
  BOOL_OR(hr.import_conf_id = 307) AS ntk,
  BOOL_OR(hr.import_conf_id = 301) AS nlk,
  BOOL_OR(hr.import_conf_id = 330) AS knav
FROM harvested_record hr
JOIN harvested_record_format_link hrfl
  ON hr.id = hrfl.harvested_record_id
 AND hrfl.harvested_record_format_id = 1
WHERE hr.publication_year >= :year
GROUP BY hr.dedup_record_id;

CREATE INDEX tmp_top_results_dedup_idx ON tmp_top_results_dedup(dedup_record_id);

INSERT INTO harvested_record_inspiration (harvested_record_id, inspiration_id, updated, last_harvest)
SELECT hr.id, :inspiration_id, localtimestamp, localtimestamp
FROM harvested_record hr
JOIN tmp_top_results_dedup d ON hr.dedup_record_id = d.dedup_record_id
WHERE hr.import_conf_id = 300
  AND d.cnt > :dedupCount
ORDER BY random()
LIMIT :results;

INSERT INTO harvested_record_inspiration (harvested_record_id, inspiration_id, updated, last_harvest)
SELECT hr.id, :inspiration_id, localtimestamp, localtimestamp
FROM harvested_record hr
JOIN tmp_top_results_dedup d ON hr.dedup_record_id = d.dedup_record_id
WHERE hr.import_conf_id = 300
  AND d.cnt > :dedupCount / 2
  AND (d.ntk IS TRUE OR d.nlk IS TRUE)
ORDER BY random()
LIMIT :results;

INSERT INTO harvested_record_inspiration (harvested_record_id, inspiration_id, updated, last_harvest)
SELECT hr.id, :inspiration_id, localtimestamp, localtimestamp
FROM harvested_record hr
JOIN tmp_top_results_dedup d ON hr.dedup_record_id = d.dedup_record_id
WHERE hr.import_conf_id = 300
  AND d.cnt > 15
  AND d.knav IS TRUE
ORDER BY random()
LIMIT :results;

UPDATE dedup_record SET updated = localtimestamp WHERE id IN (
  SELECT dedup_record_id FROM harvested_record WHERE id IN (
    SELECT harvested_record_id FROM harvested_record_inspiration WHERE inspiration_id = :inspiration_id));

DROP TABLE IF EXISTS tmp_top_results_dedup;
