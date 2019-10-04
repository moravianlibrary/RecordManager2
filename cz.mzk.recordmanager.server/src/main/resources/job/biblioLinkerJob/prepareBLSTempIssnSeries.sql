DROP TABLE IF EXISTS tmp_bls_issn_series;

CREATE TABLE tmp_bls_issn_series AS
SELECT nextval('tmp_bl_id_seq') AS row_id,
  array_to_string(array_agg(DISTINCT hr.biblio_linker_id), ',') biblio_linker_id,
  array_to_string(array_agg(hr.id), ',') local_record_id,
  hr.issn_series
FROM harvested_record hr
  INNER JOIN harvested_record_format_link hrfl ON hr.id = hrfl.harvested_record_id
WHERE hr.issn_series IS NOT NULL AND hrfl.harvested_record_format_id=1
      AND biblio_linker_similar IS TRUE
GROUP BY hr.issn_series
HAVING COUNT(DISTINCT biblio_linker_id)>1
  AND BOOL_OR(next_biblio_linker_flag) IS TRUE;

CREATE INDEX tmp_bls_issn_series_idx ON tmp_bls_issn_series(row_id);