DROP TABLE IF EXISTS tmp_bls_similarities_max_count;

CREATE TABLE tmp_bls_similarities_max_count AS
SELECT harvested_record_id
FROM biblio_linker_similar
GROUP BY harvested_record_id
HAVING COUNT(*) >= 5;

CREATE INDEX tmp_bls_similarities_max_count_idx ON tmp_bls_similarities_max_count (harvested_record_id);