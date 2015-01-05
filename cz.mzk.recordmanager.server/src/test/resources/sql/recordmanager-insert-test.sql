INSERT INTO library (id, name, url, catalog_url, city) VALUES (100, 'MZK', 'www.mzk.cz', 'vufind.mzk.cz', 'Brno');
INSERT INTO contact_person (id, library_id, name, email, phone) VALUES (200, 100, 'Jan Novak', 'jan.novak@neexistuje.cz', '728 123 456');
INSERT INTO oai_harvest_conf (id, library_id, url, set_spec, metadata_prefix, contact_person_id) VALUES (300, 100, 'http://aleph.mzk.cz/OAI', NULL, 'marc21', 200);
