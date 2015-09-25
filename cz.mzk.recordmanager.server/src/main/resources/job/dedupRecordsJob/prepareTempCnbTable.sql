DROP TABLE IF EXISTS tmp_simmilar_books_cnb;

CREATE TABLE tmp_simmilar_books_cnb AS
SELECT
  nextval('tmp_table_id_seq') as row_id,
  c.cnb,
  t.title,
  hr.publication_year,
  hrl.harvested_record_format_id,
  array_to_string(array_agg(hr.id), ',')  id_array,
  count(*) total_count,
  count(hr.dedup_record_id) dedup_count
FROM harvested_record hr 
  INNER JOIN cnb c ON hr.id = c.harvested_record_id 
  INNER JOIN title t ON hr.id = t.harvested_record_id
  INNER JOIN harvested_record_format_link hrl on hr.id = hrl.harvested_record_id
WHERE t.order_in_record = 1 AND
  hr.id NOT IN (
    SELECT hrfl.harvested_record_id FROM harvested_record_format_link hrfl 
    INNER JOIN harvested_record_format hrf ON hrf.id = hrfl.harvested_record_format_id
    WHERE hrf.name = 'PERIODICALS')
GROUP BY c.cnb,t.title,hr.publication_year,hrl.harvested_record_format_id
HAVING count(hr.id) > 1 AND count(hr.id) > count(hr.dedup_record_id) AND max(hr.updated) > (SELECT time FROM last_dedup_time);
		
CREATE INDEX tmp_cbn_idx ON tmp_simmilar_books_cnb(row_id);