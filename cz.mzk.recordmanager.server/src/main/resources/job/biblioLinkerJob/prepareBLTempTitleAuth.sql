DROP TABLE IF EXISTS tmp_bl_title_auth;

CREATE TABLE tmp_bl_title_auth AS
SELECT nextval('tmp_bl_id_seq') AS row_id,
  array_to_string(array_agg(DISTINCT hr.dedup_record_id), ',') dedup_record_id,
  array_to_string(array_agg(DISTINCT hr.author_auth_key), ',') author_auth_key,
  array_to_string(array_agg(DISTINCT t.title), ',') title
FROM harvested_record hr
INNER JOIN title t ON hr.id=t.harvested_record_id
WHERE hr.author_auth_key IS NOT NULL AND dedup_record_id IS NOT NULL
GROUP BY hr.author_auth_key, t.title
HAVING COUNT(*)>1
  AND COUNT(DISTINCT biblio_linker_id) + SUM(CASE WHEN biblio_linker_id IS NULL THEN 1 ELSE 0 END) != 1
  AND bool_or(next_biblio_linker_flag) IS TRUE;

CREATE INDEX tmp_bl_title_auth_idx ON tmp_bl_title_auth(row_id);