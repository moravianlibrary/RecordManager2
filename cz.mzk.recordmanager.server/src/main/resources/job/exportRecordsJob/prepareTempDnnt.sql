DROP TABLE IF EXISTS tmp_dnnt_test;

CREATE TABLE tmp_dnnt_test AS
SELECT record_id FROM harvested_record
WHERE import_conf_id=300
LIMIT 100;