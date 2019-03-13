DROP SEQUENCE IF EXISTS tmp_bl_id_seq;
CREATE SEQUENCE tmp_bl_id_seq;

UPDATE dedup_record
SET updated=localtimestamp
WHERE id IN (SELECT DISTINCT dedup_record_id FROM harvested_record WHERE next_biblio_linker_flag=TRUE AND biblio_linker_id IS NOT NULL);

UPDATE harvested_record
SET biblio_linker_id=NULL,next_biblio_linker_flag=TRUE
WHERE biblio_linker_id IN (SELECT DISTINCT biblio_linker_id FROM harvested_record WHERE next_biblio_linker_flag=TRUE AND biblio_linker_id IS NOT NULL);
