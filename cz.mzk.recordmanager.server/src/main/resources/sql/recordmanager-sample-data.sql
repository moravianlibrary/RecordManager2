INSERT INTO library (id, name, url, catalog_url, city) VALUES (100, 'MZK', 'www.mzk.cz', 'vufind.mzk.cz', 'Brno');
INSERT INTO contact_person (id, library_id, name, email, phone) VALUES (200, 100, 'Jan Novak', 'jan.novak@neexistuje.cz', '728 123 456');

INSERT INTO library (id, name, url, catalog_url, city) VALUES (101, 'NLK', 'medvik.cz', 'medivk.cz', 'Praha');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (102, 'MKP', 'www.mlp.cz', 'search.mlp.cz', 'Praha');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (103, 'KJM', 'kjm.cz', 'http://katalog.kjm.cz:8080/Carmen/', 'Brno');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (104, 'NKP', 'nkp.cz', 'aleph.nkp.cz', 'Praha');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (105, 'VPK', 'vpk.cz', 'vpk.cz', 'Praha');

INSERT INTO format(format, description) VALUES('marc21-xml', 'MARC21 XML');
INSERT INTO format(format, description) VALUES('xml-marc', 'MARC21 XML');

INSERT INTO oai_harvest_conf (id, library_id, url, set_spec, metadata_prefix, contact_person_id) VALUES (300, 100, 'http://aleph.mzk.cz/OAI', NULL, 'marc21', 200);
INSERT INTO oai_harvest_conf (id, library_id, url, set_spec, granularity, metadata_prefix, contact_person_id) VALUES (301, 101, 'http://oai.medvik.cz/medvik2cpk/oai', NULL, 'DAY', 'xml-marc', 200);
INSERT INTO oai_harvest_conf (id, library_id, url, set_spec, granularity, metadata_prefix, contact_person_id) VALUES (302, 102, 'http://web2.mlp.cz/cgi/oai', 'complete', NULL, 'marc21', 200);
INSERT INTO oai_harvest_conf (id, library_id, url, set_spec, granularity, metadata_prefix, contact_person_id) VALUES (303, 103, 'http://katalog.kjm.cz/l.dll', NULL, 'DAY', 'marc21', 200);
INSERT INTO oai_harvest_conf (id, library_id, url, set_spec, granularity, metadata_prefix, contact_person_id) VALUES (304, 104, 'http://aleph.nkp.cz/OAI', 'NKC', 'SECOND', 'marc21', 200);
INSERT INTO oai_harvest_conf (id, library_id, url, set_spec, granularity, metadata_prefix, contact_person_id) VALUES (305, 105, 'http://sc.vpk.cz/cgi-bin/oai2', NULL, NULL, 'marc21', 200);
INSERT INTO oai_harvest_conf (id, library_id, url, set_spec, metadata_prefix, contact_person_id) VALUES (400, 100, 'http://aleph.nkp.cz/OAI', 'AUT', 'marc21', 200);
