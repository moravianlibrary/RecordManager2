DROP TABLE IF EXISTS tmp_bls_libraries;

CREATE TABLE tmp_bls_libraries AS
SELECT nextval('tmp_bl_id_seq') AS row_id,
  candidates.biblio_linker_id,
  array_to_string(array_agg(DISTINCT candidates.local_record_id), ',') local_record_id,
  array_to_string(array_agg(DISTINCT candidates.topic_key), ',') topic_key
FROM (
  SELECT
    array_to_string(array_agg(DISTINCT hr.biblio_linker_id ORDER BY hr.biblio_linker_id), ',') biblio_linker_id,
    array_to_string(array_agg(hr.id), ',') local_record_id,
    tk.topic_key
  FROM harvested_record hr
    INNER JOIN bl_topic_key tk ON hr.id=tk.harvested_record_id
  WHERE hr.import_conf_id=351 AND biblio_linker_similar IS TRUE
  GROUP BY tk.topic_key
  HAVING COUNT(DISTINCT biblio_linker_id)>1
    AND BOOL_OR(next_biblio_linker_similar_flag) IS TRUE
  ) AS candidates
GROUP BY candidates.biblio_linker_id
ORDER BY MIN(candidates.topic_key);

CREATE INDEX tmp_bls_libraries_idx ON tmp_bls_libraries(row_id);