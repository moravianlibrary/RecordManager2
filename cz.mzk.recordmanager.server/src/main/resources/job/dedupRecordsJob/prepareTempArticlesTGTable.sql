DROP TABLE IF EXISTS tmp_simmilar_articles_tg;
DROP TABLE IF EXISTS tmp_articles_tg_keys;

-- Keys of groups that contain at least one record scheduled for dedup.
-- Incremental runs then join only these keys instead of aggregating every article.
CREATE TABLE tmp_articles_tg_keys AS
SELECT DISTINCT
  t.title,
  hr.publication_year,
  hr.author_string,
  hr.source_info_t,
  hr.source_info_g
FROM harvested_record hr
INNER JOIN title t ON hr.id = t.harvested_record_id
WHERE t.order_in_record = 1
  AND hr.next_dedup_flag IS TRUE
  AND hr.source_info_t IS NOT NULL
  AND hr.source_info_g IS NOT NULL;

CREATE INDEX tmp_articles_tg_keys_idx ON tmp_articles_tg_keys(source_info_t, source_info_g);

CREATE TABLE tmp_simmilar_articles_tg AS
SELECT
  nextval('tmp_table_id_seq') AS row_id,
  t.title,
  hr.publication_year,
  hr.author_string,
  hr.source_info_t,
  hr.source_info_g,
  array_to_string(array_agg(hr.id), ',') AS id_array
FROM harvested_record hr
INNER JOIN title t ON hr.id = t.harvested_record_id
INNER JOIN tmp_articles_tg_keys k
   ON k.source_info_t = hr.source_info_t
  AND k.source_info_g = hr.source_info_g
  AND k.title IS NOT DISTINCT FROM t.title
  AND k.publication_year IS NOT DISTINCT FROM hr.publication_year
  AND k.author_string IS NOT DISTINCT FROM hr.author_string
WHERE t.order_in_record = 1
GROUP BY t.title, hr.author_string, hr.publication_year, hr.source_info_t, hr.source_info_g
HAVING COUNT(DISTINCT hr.id) > 1
  AND COUNT(DISTINCT hr.dedup_record_id)
      + SUM(CASE WHEN hr.dedup_record_id IS NULL THEN 1 ELSE 0 END) != 1;

DROP TABLE tmp_articles_tg_keys;

CREATE INDEX tmp_articles_tg_idx ON tmp_simmilar_articles_tg(row_id);