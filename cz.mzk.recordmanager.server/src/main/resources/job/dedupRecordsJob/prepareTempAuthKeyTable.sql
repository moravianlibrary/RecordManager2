DROP TABLE IF EXISTS tmp_auth_keys;

CREATE TABLE tmp_auth_keys AS
SELECT
  nextval('tmp_table_id_seq') as row_id,
  titles.title,
  hrl.harvested_record_format_id,
  array_to_string(array_agg(hr.id), ',') as id_array
FROM (
    SELECT
      harvested_record_id,
      title
    FROM title
    UNION
    SELECT 
      harvested_record_id,
      short_title
    FROM short_title
  ) titles
  INNER JOIN harvested_record hr on hr.id=titles.harvested_record_id
  INNER JOIN harvested_record_format_link hrl ON hrl.harvested_record_id = hr.id
  LEFT OUTER JOIN tmp_periodicals_ids tpi ON hr.id = tpi.id
WHERE tpi.id IS NULL
GROUP BY titles.title,hr.publication_year,hr.author_auth_key,hrl.harvested_record_format_id
HAVING COUNT(DISTINCT hr.id) > 1 
  AND count(DISTINCT dedup_record_id) + sum(case when dedup_record_id is null then 1 else 0 end) != 1
  AND bool_or(next_dedup_flag) IS TRUE;

CREATE INDEX tmp_auth_keys_idx ON tmp_auth_keys(row_id);