DROP TABLE IF EXISTS tmp_bl_title_auth;

CREATE TABLE tmp_bl_title_auth AS
SELECT nextval('tmp_bl_id_seq') AS row_id,
  array_to_string(array_agg(DISTINCT hr.dedup_record_id), ',') dedup_record_id,
  array_to_string(array_agg(DISTINCT hr.author_auth_key), ',') author_auth_key,
  array_to_string(array_agg(DISTINCT t.title), ',') title
FROM harvested_record hr
INNER JOIN title t ON hr.id=t.harvested_record_id
WHERE hr.author_auth_key IS NOT NULL
GROUP BY hr.author_auth_key, t.title;

CREATE INDEX tmp_bl_title_auth_idx ON tmp_bl_title_auth(row_id);