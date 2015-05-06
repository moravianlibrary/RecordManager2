INSERT INTO library (id, name, url, catalog_url, city) VALUES (100, 'MZK', 'www.mzk.cz', 'vufind.mzk.cz', 'Brno');
INSERT INTO contact_person (id, library_id, name, email, phone) VALUES (200, 100, 'Jan Novak', 'jan.novak@neexistuje.cz', '728 123 456');

INSERT INTO library (id, name, url, catalog_url, city) VALUES (101, 'NLK', 'medvik.cz', 'medivk.cz', 'Praha');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (102, 'MKP', 'www.mlp.cz', 'search.mlp.cz', 'Praha');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (103, 'KJM', 'kjm.cz', 'http://katalog.kjm.cz:8080/Carmen/', 'Brno');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (104, 'NKP', 'nkp.cz', 'aleph.nkp.cz', 'Praha');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (105, 'VPK', 'vpk.cz', 'vpk.cz', 'Praha');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (106, 'TRE', 'katalogknih.cz', 'vufind.katalogknih.c', 'Česká Třebová');

INSERT INTO format(format, description) VALUES('marc21-xml', 'MARC21 XML');
INSERT INTO format(format, description) VALUES('xml-marc', 'MARC21 XML');

INSERT INTO oai_harvest_conf (id, library_id, url, set_spec, metadata_prefix, contact_person_id) VALUES (300, 100, 'http://aleph.mzk.cz/OAI', NULL, 'marc21', 200);
INSERT INTO oai_harvest_conf (id, library_id, url, set_spec, granularity, metadata_prefix, contact_person_id) VALUES (301, 101, 'http://oai.medvik.cz/medvik2cpk/oai', NULL, 'DAY', 'xml-marc', 200);
INSERT INTO oai_harvest_conf (id, library_id, url, set_spec, granularity, metadata_prefix, contact_person_id) VALUES (302, 102, 'http://web2.mlp.cz/cgi/oai', 'complete', NULL, 'marc21', 200);
INSERT INTO oai_harvest_conf (id, library_id, url, set_spec, granularity, metadata_prefix, contact_person_id) VALUES (303, 103, 'http://katalog.kjm.cz/l.dll', NULL, 'DAY', 'marc21', 200);
INSERT INTO oai_harvest_conf (id, library_id, url, set_spec, granularity, metadata_prefix, contact_person_id) VALUES (304, 104, 'http://aleph.nkp.cz/OAI', 'NKC', 'SECOND', 'marc21', 200);
INSERT INTO oai_harvest_conf (id, library_id, url, set_spec, granularity, metadata_prefix, contact_person_id) VALUES (305, 105, 'http://sc.vpk.cz/cgi-bin/oai2', NULL, NULL, 'marc21', 200);
INSERT INTO oai_harvest_conf (id, library_id, url, set_spec, granularity, metadata_prefix, contact_person_id) VALUES (306, 106, NULL, NULL, NULL, 'marc21', 200);
INSERT INTO oai_harvest_conf (id, library_id, url, set_spec, metadata_prefix, contact_person_id) VALUES (400, 100, 'http://aleph.nkp.cz/OAI', 'AUT', 'marc21', 200);

INSERT INTO harvested_record_format(id, name) VALUES (1, 'BOOKS'); 
INSERT INTO harvested_record_format(id, name) VALUES (2, 'PERIODICALS');
INSERT INTO harvested_record_format(id, name) VALUES (3, 'ARTICLES');
INSERT INTO harvested_record_format(id, name) VALUES (4, 'MAPS');
INSERT INTO harvested_record_format(id, name) VALUES (5, 'MUSICAL_SCORES'); 
INSERT INTO harvested_record_format(id, name) VALUES (6, 'VISUAL_DOCUMENTS'); 
INSERT INTO harvested_record_format(id, name) VALUES (7, 'MANUSCRIPTS');
INSERT INTO harvested_record_format(id, name) VALUES (8, 'MICROFORMS');
INSERT INTO harvested_record_format(id, name) VALUES (9, 'LARGE_PRINTS'); 
INSERT INTO harvested_record_format(id, name) VALUES (10, 'BRAILL');
INSERT INTO harvested_record_format(id, name) VALUES (11, 'ELECTRONIC_SOURCE'); 
INSERT INTO harvested_record_format(id, name) VALUES (12, 'AUDIO_DOCUMENTS'); 
INSERT INTO harvested_record_format(id, name) VALUES (13, 'AUDIO_CD');
INSERT INTO harvested_record_format(id, name) VALUES (14, 'AUDIO_DVD'); 
INSERT INTO harvested_record_format(id, name) VALUES (15, 'AUDIO_LP'); 
INSERT INTO harvested_record_format(id, name) VALUES (16, 'AUDIO_CASSETTE'); 
INSERT INTO harvested_record_format(id, name) VALUES (17, 'AUDIO_OTHER'); 
INSERT INTO harvested_record_format(id, name) VALUES (18, 'VIDEO_DOCUMENTS');
INSERT INTO harvested_record_format(id, name) VALUES (19, 'VIDEO_BLURAY');
INSERT INTO harvested_record_format(id, name) VALUES (20, 'VIDEO_VHS');
INSERT INTO harvested_record_format(id, name) VALUES (21, 'VIDEO_DVD');
INSERT INTO harvested_record_format(id, name) VALUES (22, 'VIDEO_CD');
INSERT INTO harvested_record_format(id, name) VALUES (23, 'VIDEO_OTHER');
INSERT INTO harvested_record_format(id, name) VALUES (24, 'KIT');
INSERT INTO harvested_record_format(id, name) VALUES (25, 'OBJECT');
INSERT INTO harvested_record_format(id, name) VALUES (26, 'MIX_DOCUMENT');
INSERT INTO harvested_record_format(id, name) VALUES (100, 'UNSPECIFIED');
