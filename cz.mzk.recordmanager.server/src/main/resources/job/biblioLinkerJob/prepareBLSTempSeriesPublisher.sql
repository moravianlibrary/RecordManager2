DROP TABLE IF EXISTS tmp_bls_series_publisher;

CREATE TABLE tmp_bls_series_publisher AS
SELECT nextval('tmp_bl_id_seq') AS                                  row_id,
       array_to_string(array_agg(DISTINCT hr.dedup_record_id), ',') dedup_record_id,
       array_to_string(array_agg(hr.id), ',')                       local_record_id,
       hr.bl_series,
       hr.bl_publisher
FROM harvested_record hr
         INNER JOIN harvested_record_format_link hrfl ON hr.id = hrfl.harvested_record_id
WHERE hr.bl_series IS NOT NULL
  AND hr.bl_publisher IS NOT NULL
  AND hrfl.harvested_record_format_id NOT IN (2, 3)
  AND biblio_linker_similar IS TRUE
GROUP BY hr.bl_series, hr.bl_publisher
HAVING COUNT(DISTINCT dedup_record_id) > 1
   AND bool_or(next_biblio_linker_similar_flag) IS TRUE;

CREATE INDEX tmp_bls_series_publisher_idx ON tmp_bls_series_publisher (row_id);