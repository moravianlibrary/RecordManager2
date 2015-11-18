DROP TABLE IF EXISTS tmp_periodicals_years;

CREATE TABLE tmp_periodicals_years AS
SELECT hr_limited.id as harvested_record_id,hr_limited.publication_year,t.title,hr_limited.updated FROM (
  SELECT DISTINCT ON (hr2.dedup_record_id) hr2.dedup_record_id,hr2.id,hr2.publication_year,hr2.updated
    FROM (
      SELECT dedup_record_id, MAX(weight) AS w
        FROM harvested_record
        WHERE publication_year IS NOT NULL
        AND id IN (
          SELECT hrfl.harvested_record_id FROM harvested_record_format_link hrfl 
          INNER JOIN harvested_record_format hrf ON hrf.id = hrfl.harvested_record_format_id
          WHERE hrf.name = 'PERIODICALS')
        GROUP BY dedup_record_id
        ) t 
        INNER JOIN harvested_record hr2 ON hr2.dedup_record_id = t.dedup_record_id AND t.w = hr2.weight
) as hr_limited
  INNER JOIN title t on t.harvested_record_id = hr_limited.id
  LEFT OUTER JOIN tmp_periodicals_ids tpi ON hr_limited.id = tpi.id
  WHERE t.order_in_record = 1
    AND tpi.id IS NOT NULL
UNION
SELECT hr_null.id as harvested_record_id,hr_null.publication_year,t.title,hr_null.updated 
FROM harvested_record hr_null
  INNER JOIN title t on hr_null.id = t.harvested_record_id
  LEFT OUTER JOIN tmp_periodicals_ids tpi ON hr_null.id = tpi.id
WHERE tpi.id IS NOT NULL
AND hr_null.dedup_record_id IS NULL 
AND hr_null.publication_year IS NOT NULL;

CREATE INDEX tmp_periodicals_years_idx ON tmp_periodicals_years(publication_year);

DROP TABLE IF EXISTS tmp_periodicals_similarity_ids;

CREATE TABLE tmp_periodicals_similarity_ids (
  row_id     numeric,
  id_array   text
);

CREATE INDEX ON tmp_periodicals_similarity_ids(row_id);