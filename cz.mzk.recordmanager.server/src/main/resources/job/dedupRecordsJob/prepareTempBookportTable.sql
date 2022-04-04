DROP TABLE IF EXISTS tmp_simmilar_bookport_id;

CREATE TABLE tmp_simmilar_bookport_id AS
SELECT
    nextval('tmp_table_id_seq') AS row_id,
    hr.record_id,
    array_to_string(array_agg(hr.id), ',')  id_array
FROM harvested_record hr
         INNER JOIN import_conf ic ON hr.import_conf_id = ic.id
WHERE ic.id_prefix = 'bookport'
GROUP BY hr.record_id
HAVING COUNT(DISTINCT hr.id) > 1
   AND COUNT(DISTINCT dedup_record_id) + SUM(CASE WHEN dedup_record_id IS NULL THEN 1 ELSE 0 END) != 1
   AND BOOL_OR(next_dedup_flag) IS TRUE;

CREATE INDEX tmp_bookport_id_idx ON tmp_simmilar_bookport_id(row_id);