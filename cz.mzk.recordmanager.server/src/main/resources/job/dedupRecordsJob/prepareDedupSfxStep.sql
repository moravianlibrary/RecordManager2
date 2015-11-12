DROP TABLE IF EXISTS tmp_periodicals_sfx;

CREATE TABLE tmp_periodicals_sfx AS
SELECT 
  nextval('tmp_table_id_seq') as row_id,
  array_to_string(array_agg(safe_titles.harvested_record_id), ',')  id_array
FROM (
  SELECT harvested_record_id,title FROM title 
  WHERE title IN (SELECT t.title FROM harvested_record hr INNER JOIN title t ON hr.id = t.harvested_record_id WHERE import_conf_id = 1302)
) AS safe_titles
INNER JOIN harvested_record hr2 ON hr2.id = safe_titles.harvested_record_id
WHERE hr2.id IN (
  SELECT hrfl.harvested_record_id FROM harvested_record_format_link hrfl 
  INNER JOIN harvested_record_format hrf ON hrf.id = hrfl.harvested_record_format_id
  WHERE hrf.name = 'PERIODICALS')
GROUP BY safe_titles.title
HAVING count(safe_titles.title) > 1
  AND count(DISTINCT hr2.dedup_record_id) + sum(case when hr2.dedup_record_id IS NULL THEN 1 else 0 END) != 1
  
CREATE INDEX tmp_periodicals_sfx ON tmp_oclc_clusters(row_id);