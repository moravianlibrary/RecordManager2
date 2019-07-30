DROP TABLE IF EXISTS tmp_bls_source_info_t_topic_key;

CREATE TABLE tmp_bls_source_info_t_topic_key AS
SELECT nextval('tmp_bl_id_seq') AS row_id,
  array_to_string(array_agg(DISTINCT hr.biblio_linker_id), ',') biblio_linker_id,
  array_to_string(array_agg(hr.id), ',') local_record_id,
  hr.source_info_t,
  hr.bl_topic_key
FROM harvested_record hr
  INNER JOIN harvested_record_format_link hrfl ON hr.id = hrfl.harvested_record_id
WHERE hr.bl_topic_key IS NOT NULL AND hr.source_info_t IS NOT NULL AND hrfl.harvested_record_format_id=3
GROUP BY hr.source_info_t, hr.bl_topic_key
HAVING COUNT(DISTINCT biblio_linker_id)>1
  AND bool_or(next_biblio_linker_flag) IS TRUE;

CREATE INDEX tmp_bls_source_info_t_topic_key_idx ON tmp_bls_source_info_t_topic_key(row_id);