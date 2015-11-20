DROP TABLE IF EXISTS tmp_similarity_ids;

CREATE TABLE tmp_similarity_ids(
  row_id     numeric,
  id_array   text
);

CREATE INDEX tmp_similarity_ids_idx ON tmp_similarity_ids(row_id);

DROP TABLE IF EXISTS tmp_titles_for_simmilarity_searching;

-- first part of query selects only one record per each deduplicatated record (the one having highest weight)
-- second part adds all records with no dedup_record assigned yet
CREATE TABLE tmp_titles_for_simmilarity_searching AS
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
    (SELECT DISTINCT ON (hr2.dedup_record_id) hr2.dedup_record_id,hr2.id,hr2.publication_year,hr2.pages,hr2.author_string, t.w, hr2.updated
    FROM (
      SELECT dedup_record_id, MAX(weight) AS w
        FROM harvested_record
        GROUP BY dedup_record_id
        ) t INNER JOIN harvested_record hr2 ON hr2.dedup_record_id = t.dedup_record_id AND t.w = hr2.weight
    ) AS unique_ids
  INNER JOIN title ON unique_ids.id = title.harvested_record_id
  INNER JOIN language ON unique_ids.id = language.harvested_record_id
  LEFT OUTER JOIN isbn i ON unique_ids.id = i.harvested_record_id
  LEFT OUTER JOIN cnb c ON unique_ids.id = c.harvested_record_id
  LEFT OUTER JOIN tmp_periodicals_ids tpi ON unique_ids.id = tpi.id
  WHERE unique_ids.updated > ALL(select time from last_dedup_time)
    AND tpi.id is NULL
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
  INNER JOIN title ON nhr.id = title.harvested_record_id
  INNER JOIN language ON nhr.id = language.harvested_record_id
  LEFT OUTER JOIN isbn i ON nhr.id = i.harvested_record_id
  LEFT OUTER JOIN cnb c ON nhr.id = c.harvested_record_id
  LEFT OUTER JOIN tmp_periodicals_ids tpi ON nhr.id = tpi.id
  WHERE dedup_record_id IS NULL
    AND tpi.id IS NULL;
    
 CREATE INDEX tmp_titles_for_simmilarity_searching_idx ON tmp_titles_for_simmilarity_searching(id);
