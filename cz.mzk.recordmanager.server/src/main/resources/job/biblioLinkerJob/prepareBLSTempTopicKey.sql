DROP TABLE IF EXISTS tmp_bls_topic_key;

CREATE TABLE tmp_bls_topic_key AS
SELECT nextval('tmp_bl_id_seq') AS row_id,
  array_to_string(array_agg(DISTINCT hr.biblio_linker_id), ',') biblio_linker_id,
  array_to_string(array_agg(hr.id), ',') local_record_id,
  hr.bl_topic_key
FROM harvested_record hr
  INNER JOIN harvested_record_format_link hrfl ON hr.id = hrfl.harvested_record_id
  INNER JOIN language l ON l.harvested_record_id = hr.id
WHERE hr.bl_topic_key IS NOT NULL
      AND l.lang='cze' AND hrfl.harvested_record_format_id IN (1,2,3,4,5,12,13,14,15,16,17,18,19,20,21,22,23)
      AND biblio_linker_similar IS TRUE
GROUP BY hr.bl_topic_key
HAVING COUNT(DISTINCT biblio_linker_id)>1
  AND bool_or(next_biblio_linker_flag) IS TRUE
  AND COUNT(hr.id)<10000
  AND COUNT(DISTINCT biblio_linker_id)<500;


CREATE INDEX tmp_bls_topic_key_idx ON tmp_bls_topic_key(row_id);