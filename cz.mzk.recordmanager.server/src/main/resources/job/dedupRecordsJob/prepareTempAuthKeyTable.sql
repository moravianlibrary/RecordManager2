DROP TABLE IF EXISTS tmp_auth_keys;

CREATE TABLE tmp_auth_keys AS
SELECT 
  title,
  hrl.harvested_record_format_id,
  array_to_string(array_agg(hr.id), ',') as id_array,
  count(*) AS total_count,
  count(hr.dedup_record_id) as dedup_count 
FROM harvested_record hr
  INNER JOIN title t ON t.harvested_record_id = hr.id
  INNER JOIN harvested_record_format_link hrl ON hrl.harvested_record_id = hr.id
GROUP BY t.title,hr.publication_year,hr.author_auth_key,hrl.harvested_record_format_id
HAVING count(hr.id) > 1 AND count(hr.id) > count(hr.dedup_record_id);

CREATE INDEX tmp_auth_keys_idx ON tmp_auth_keys(id_array);