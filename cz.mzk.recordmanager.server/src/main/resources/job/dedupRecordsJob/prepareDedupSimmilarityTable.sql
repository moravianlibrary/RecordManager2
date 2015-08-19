REFRESH MATERIALIZED VIEW titles_for_simmilarity_searching_view;

DROP TABLE IF EXISTS tmp_similarity_ids;

CREATE TABLE tmp_similarity_ids(
  row_id     numeric,
  id_array   text
);