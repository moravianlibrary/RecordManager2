DROP TABLE IF EXISTS tmp_auth_keys;

CREATE TABLE tmp_auth_keys AS
	SELECT 
	     title,
	     array_to_string(array_agg(harvested_record.id), ',') as id_array,
		count(*) AS total_count,
		count(harvested_record.dedup_record_id) as dedup_count 
	FROM harvested_record
				INNER JOIN title ON title.harvested_record_id = harvested_record.id
				INNER JOIN harvested_record_format_link ON harvested_record_format_link.harvested_record_id = harvested_record.id
				INNER JOIN harvested_record_format ON harvested_record_format_link.harvested_record_format_id = harvested_record_format.id
	WHERE harvested_record_format_link.harvested_record_format_id = 1
	GROUP BY publication_year,title,author_auth_key
	HAVING 
		count(harvested_record.id) > 1 
		AND count(harvested_record.id) > count(harvested_record.dedup_record_id);