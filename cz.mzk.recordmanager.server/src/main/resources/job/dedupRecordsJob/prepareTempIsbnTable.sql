DROP TABLE IF EXISTS tmp_simmilar_books_isbn;

CREATE TABLE tmp_simmilar_books_isbn AS
SELECT
  i.isbn,
  t.title,
  hr.publication_year,
  hrl.harvested_record_format_id,
  array_to_string(array_agg(hr.id), ',')  id_array,
  count(*) total_count,
  count(hr.dedup_record_id) dedup_count
FROM harvested_record hr 
  INNER JOIN isbn i ON hr.id = i.harvested_record_id 
  INNER JOIN title t ON hr.id = t.harvested_record_id
  INNER JOIN harvested_record_format_link hrl on hr.id = hrl.harvested_record_id
WHERE t.order_in_record = 1 AND i.order_in_record = 1
GROUP BY i.isbn,t.title,hr.publication_year,hrl.harvested_record_format_id
HAVING count(hr.id) > 1 AND count(hr.id) > count(hr.dedup_record_id);
		
CREATE INDEX tmp_isbn_idx ON tmp_simmilar_books_isbn(id_array);