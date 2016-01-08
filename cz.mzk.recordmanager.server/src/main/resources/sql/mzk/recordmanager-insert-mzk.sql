INSERT INTO library(id, name, url, catalog_url, city)
  VALUES(1, 'MZK', 'http://www.mzk.cz', 'https://vufind.mzk.cz/', 'Brno');
INSERT INTO contact_person(id, library_id, name, email, phone)
  VALUES(1, 1, 'Admin', 'catalog@mzk.cz', NULL);
INSERT INTO import_conf(id, library_id, contact_person_id, id_prefix, harvest_frequency)
  VALUES(1, 1, 1, NULL, 'D');
INSERT INTO oai_harvest_conf(import_conf_id, url, set_spec, metadata_prefix, granularity)
  VALUES(1, 'http://aleph.mzk.cz/OAI', 'MZKALL', 'marc21', NULL);
