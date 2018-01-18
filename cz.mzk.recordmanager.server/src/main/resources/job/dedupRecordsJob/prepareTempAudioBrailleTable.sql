DROP TABLE IF EXISTS tmp_simmilar_audio_braille;

CREATE TABLE tmp_simmilar_audio_braille AS
SELECT
  nextval('tmp_table_id_seq') AS row_id,
  t.title,
  hr.publication_year,
  hr.author_auth_key,
  array_to_string(array_agg(hr.id), ',')  id_array
FROM harvested_record hr
  INNER JOIN title t ON hr.id = t.harvested_record_id
  INNER JOIN harvested_record_format_link hrl ON hr.id = hrl.harvested_record_id
WHERE t.order_in_record = 1
  AND hrl.harvested_record_format_id=65 -- 65 = AUDIO_BRAILLE
GROUP BY t.title,hr.author_auth_key,hr.publication_year
HAVING COUNT(DISTINCT hr.id) > 1 
  AND COUNT(DISTINCT dedup_record_id) + SUM(CASE WHEN dedup_record_id IS NULL THEN 1 ELSE 0 END) != 1
  AND bool_or(next_dedup_flag) IS TRUE;

CREATE INDEX tmp_audio_braille_idx ON tmp_simmilar_audio_braille(row_id);