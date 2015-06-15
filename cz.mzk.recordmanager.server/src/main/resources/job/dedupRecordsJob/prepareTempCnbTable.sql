DROP TABLE IF EXISTS tmp_simmilar_books_cnb;

CREATE TABLE tmp_simmilar_books_cnb AS
	SELECT 
		cnb.cnb,
		title.title,
		harvested_record.publication_year,
		array_to_string(array_agg(harvested_record.id), ',') as id_array,
		count(harvested_record.id) as total_count,
		count(harvested_record.dedup_record_id) as dedup_count  
		FROM harvested_record
			INNER JOIN cnb on cnb.harvested_record_id = harvested_record.id 
			INNER JOIN title on title.harvested_record_id = harvested_record.id
			INNER JOIN harvested_record_format_link on harvested_record_format_link.harvested_record_id = harvested_record.id
			INNER JOIN harvested_record_format on harvested_record_format_link.harvested_record_format_id = harvested_record_format.id
		WHERE
			harvested_record_format.name = 'BOOKS' AND
			title.order_in_record = 1 
		GROUP BY cnb.cnb,title.title,harvested_record.publication_year
		HAVING 
			count(harvested_record.id) > 1 AND
		 	count(harvested_record.id) > count(harvested_record.dedup_record_id)
		ORDER BY total_count DESC;
		
CREATE INDEX tmp_cbn_idx ON tmp_simmilar_books_cnb(id_array);