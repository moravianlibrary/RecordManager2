CREATE INDEX cluster_id_harvested_record_idx ON harvested_record USING btree (cluster_id);
CREATE INDEX harvested_record_dedup_keys_hash_idx ON harvested_record USING btree (dedup_keys_hash);
CREATE INDEX harvested_record_dedup_record_id_updated_idx ON harvested_record USING btree (dedup_record_id, updated);
CREATE INDEX harvested_record_import_conf_001_id_idx ON harvested_record USING btree (import_conf_id, raw_001_id);
CREATE INDEX harvested_record_import_conf_id ON harvested_record USING btree (import_conf_id);
CREATE INDEX harvested_record_next_dedup_flag ON harvested_record USING btree (next_dedup_flag);
CREATE INDEX harvested_record_raw_001_id_idx ON harvested_record USING btree (raw_001_id);
CREATE INDEX harvested_record_source_info_idx ON harvested_record USING btree (source_info);
CREATE INDEX harvested_record_updated_index ON harvested_record USING btree (updated);

DROP TABLE IF EXISTS tmp_simmilar_books_cnb;

CREATE TABLE tmp_simmilar_books_cnb AS
SELECT
  nextval('tmp_table_id_seq') as row_id,
  c.cnb,
  t.title,
  hr.publication_year,
  hrl.harvested_record_format_id,
  array_to_string(array_agg(hr.id), ',')  id_array
FROM harvested_record hr 
  INNER JOIN cnb c ON hr.id = c.harvested_record_id 
  INNER JOIN title t ON hr.id = t.harvested_record_id
  INNER JOIN harvested_record_format_link hrl on hr.id = hrl.harvested_record_id
  LEFT OUTER JOIN tmp_periodicals_ids tpi ON hr.id = tpi.id
WHERE t.order_in_record = 1
  AND tpi.id IS NULL
GROUP BY c.cnb,t.title,hr.publication_year,hrl.harvested_record_format_id
HAVING COUNT(DISTINCT hr.id) > 1 
  AND count(DISTINCT dedup_record_id) + sum(case when dedup_record_id is null then 1 else 0 end) != 1
  AND bool_or(next_dedup_flag) IS TRUE;
		
CREATE INDEX tmp_cbn_idx ON tmp_simmilar_books_cnb(row_id);