INSERT INTO library (id, name, url, catalog_url, city) VALUES (100, 'MZK', 'www.mzk.cz', 'vufind.mzk.cz', 'Brno');
INSERT INTO contact_person (id, library_id, name, email, phone) VALUES (200, 100, 'Jan Novak', 'jan.novak@neexistuje.cz', '728 123 456');

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
INSERT INTO harvested_record_format(id, name) VALUES (59, 'OTHER_COMPUTER_CARRIER');
INSERT INTO harvested_record_format(id, name) VALUES (60, 'OTHER_OTHER');
INSERT INTO harvested_record_format(id, name) VALUES (100, 'UNSPECIFIED');
