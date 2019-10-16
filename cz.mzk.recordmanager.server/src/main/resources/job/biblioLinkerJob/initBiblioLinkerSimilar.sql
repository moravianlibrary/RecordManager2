DROP SEQUENCE IF EXISTS tmp_bl_id_seq;
CREATE SEQUENCE tmp_bl_id_seq;

DROP TABLE IF EXISTS tmp_bl_flag_ids;
CREATE TABLE tmp_bl_flag_ids AS
SELECT id FROM harvested_record WHERE next_biblio_linker_similar_flag IS TRUE;

DELETE FROM biblio_linker_similar bls WHERE
  EXISTS (SELECT 1 FROM tmp_bl_flag_ids tmp WHERE tmp.id=bls.harvested_record_id);

UPDATE harvested_record hr SET next_biblio_linker_similar_flag=TRUE WHERE id IN (
  SELECT harvested_record_id FROM biblio_linker_similar bls WHERE
  EXISTS (SELECT 1 FROM tmp_bl_flag_ids tmp WHERE tmp.id=bls.harvested_record_similar_id));

DELETE FROM biblio_linker_similar bls WHERE
  EXISTS (SELECT 1 FROM tmp_bl_flag_ids tmp WHERE tmp.id=bls.harvested_record_similar_id);

DROP TABLE IF EXISTS tmp_bl_flag_ids;