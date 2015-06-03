DROP TABLE IF EXISTS tmp_simmilar_books_isbn;

CREATE TABLE tmp_simmilar_books_isbn AS
	SELECT 
		isbn.isbn,
		title.title,
		harvested_record.publication_year,
		array_to_string(array_agg(harvested_record.id), ',') as id_array,
		count(*) AS total_count,
		count(harvested_record.dedup_record_id) as dedup_count
		FROM harvested_record
			INNER JOIN isbn ON isbn.harvested_record_id = harvested_record.id 
			INNER JOIN title ON title.harvested_record_id = harvested_record.id
			INNER JOIN harvested_record_format_link ON harvested_record_format_link.harvested_record_id = harvested_record.id
			INNER JOIN harvested_record_format ON harvested_record_format_link.harvested_record_format_id = harvested_record_format.id
		WHERE
			harvested_record_format.name = 'BOOKS' AND
			title.order_in_record = 1 AND
			isbn.order_in_record = 1
		GROUP BY isbn.isbn,title.title,harvested_record.publication_year
		HAVING 
			count(harvested_record.id) > 1 AND 
		 	count(harvested_record.id) > count(harvested_record.dedup_record_id)
		ORDER BY total_count DESC;
		
CREATE INDEX tmp_isbn_idx ON tmp_simmilar_books_isbn(id_array);