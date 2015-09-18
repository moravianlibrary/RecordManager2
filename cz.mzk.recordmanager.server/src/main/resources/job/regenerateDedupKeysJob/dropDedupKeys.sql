UPDATE harvested_record SET cluster_id = NULL, uuid = NULL, issn_series = NULL, issn_series_order = NULL, publication_year = NULL, scale = NULL, author_string = NULL, author_auth_key = NULL, raw_001_id = NULL;
DELETE FROM title;
DELETE FROM isbn;
DELETE FROM issn;
DELETE FROM cnb;
DELETE FROM language;
DELETE FROM oclc;