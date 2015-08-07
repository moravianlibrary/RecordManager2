-- materialized view for simmilarity searching purposes
-- first part of view selects only one record per each deduplicatated record (the one having highest weight)
-- second part adds all records with no dedup_record assigned yet
CREATE MATERIALIZED VIEW titles_for_simmilarity_searching_view AS
SELECT unique_ids.id, 
    title.harvested_record_id,
    unique_ids.publication_year,
    unique_ids.pages,
    unique_ids.author_string,
    title.title,
    i.isbn,
    c.cnb,
    language.lang
  FROM
    (SELECT DISTINCT ON (hr2.dedup_record_id) hr2.dedup_record_id,hr2.id,hr2.publication_year,hr2.pages,hr2.author_string, t.w
    FROM (
      SELECT dedup_record_id, MAX(weight) AS w
        FROM harvested_record
        GROUP BY dedup_record_id
        ) t INNER JOIN harvested_record hr2 ON hr2.dedup_record_id = t.dedup_record_id AND t.w = hr2.weight
    ) AS unique_ids
  INNER JOIN title on unique_ids.id = title.harvested_record_id
  INNER JOIN language on unique_ids.id = language.harvested_record_id
  LEFT OUTER JOIN isbn i on unique_ids.id = i.harvested_record_id
  LEFT OUTER JOIN cnb c on unique_ids.id = c.harvested_record_id
UNION
SELECT 
    nhr.id,
    title.harvested_record_id,
    nhr.publication_year,
    nhr.pages,
    nhr.author_string,
    title.title,
    i.isbn,
    c.cnb,
    language.lang
  FROM harvested_record nhr
  INNER JOIN title on nhr.id = title.harvested_record_id
  INNER JOIN language on nhr.id = language.harvested_record_id
  LEFT OUTER JOIN isbn i on nhr.id = i.harvested_record_id
  LEFT OUTER JOIN cnb c on nhr.id = c.harvested_record_id
  WHERE dedup_record_id IS NULL
