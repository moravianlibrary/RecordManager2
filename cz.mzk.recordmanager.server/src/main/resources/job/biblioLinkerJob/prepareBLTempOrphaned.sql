DROP TABLE IF EXISTS tmp_bl_orphaned;

CREATE TABLE tmp_bl_orphaned AS
SELECT nextval('tmp_bl_id_seq') AS row_id,
  dedup_record_id
FROM harvested_record hr
JOIN import_conf ic ON ic.id=hr.import_conf_id
WHERE hr.biblio_linker_id IS NULL AND hr.deleted IS NULL AND dedup_record_id IS NOT NULL AND ic.generate_biblio_linker_keys IS TRUE;

CREATE INDEX tmp_bl_orphaned_idx ON tmp_bl_orphaned(row_id);