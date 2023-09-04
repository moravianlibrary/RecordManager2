DROP TABLE IF EXISTS tmp_bls_topic_key_lang_rest;

CREATE TABLE tmp_bls_topic_key_lang_rest AS
SELECT nextval('tmp_bl_id_seq') AS                                   row_id,
       array_to_string(array_agg(DISTINCT hr.biblio_linker_id), ',') biblio_linker_id,
       array_to_string(array_agg(hr.id), ',')                        local_record_id,
       tk.topic_key
FROM harvested_record hr
         INNER JOIN bl_language l ON l.harvested_record_id = hr.id
         INNER JOIN bl_topic_key tk ON hr.id = tk.harvested_record_id
GROUP BY tk.topic_key, l.lang
HAVING COUNT(DISTINCT biblio_linker_id) > 1
   AND BOOL_OR(next_biblio_linker_similar_flag) IS TRUE
   AND BOOL_OR(bl_disadvantaged) IS TRUE
   AND COUNT(hr.id) < 10000;

CREATE INDEX tmp_bls_topic_key_bl_language_rest_idx ON tmp_bls_topic_key_lang_rest (row_id);