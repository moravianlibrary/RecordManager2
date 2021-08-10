DROP SEQUENCE IF EXISTS tmp_bl_id_seq;
CREATE SEQUENCE tmp_bl_id_seq;

DROP TABLE IF EXISTS tmp_bl_flag_ids;
CREATE TABLE tmp_bl_flag_ids AS
SELECT hr.id FROM harvested_record hr
  JOIN import_conf ic ON hr.import_conf_id=ic.id
  WHERE next_biblio_linker_similar_flag IS TRUE AND ic.generate_biblio_linker_keys IS TRUE;

-- UPDATE dedup_record
-- SET updated=localtimestamp
-- WHERE id IN (SELECT DISTINCT dedup_record_id FROM harvested_record WHERE next_biblio_linker_similar_flag=TRUE AND biblio_linker_id IS NOT NULL);

DELETE FROM biblio_linker_similar bls WHERE
  EXISTS (SELECT 1 FROM tmp_bl_flag_ids tmp WHERE tmp.id=bls.harvested_record_id);

DELETE FROM biblio_linker_similar bls WHERE
  EXISTS (SELECT 1 FROM tmp_bl_flag_ids tmp WHERE tmp.id=bls.harvested_record_similar_id);

DROP TABLE IF EXISTS tmp_bl_flag_ids;