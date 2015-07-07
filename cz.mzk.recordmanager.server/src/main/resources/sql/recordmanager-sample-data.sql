INSERT INTO library (id, name, url, catalog_url, city) VALUES (100, 'MZK', 'www.mzk.cz', 'vufind.mzk.cz', 'Brno');
INSERT INTO contact_person (id, library_id, name, email, phone) VALUES (200, 100, 'Jan Novak', 'jan.novak@neexistuje.cz', '728 123 456');

INSERT INTO library (id, name, url, catalog_url, city) VALUES (101, 'NLK', 'medvik.cz', 'medivk.cz', 'Praha');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (102, 'MKP', 'www.mlp.cz', 'search.mlp.cz', 'Praha');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (103, 'KJM', 'kjm.cz', 'http://katalog.kjm.cz:8080/Carmen/', 'Brno');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (104, 'NKP', 'nkp.cz', 'aleph.nkp.cz', 'Praha');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (105, 'VPK', 'vpk.cz', 'vpk.cz', 'Praha');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (106, 'TRE', 'katalogknih.cz', 'vufind.katalogknih.c', 'Česká Třebová');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (107, 'NTK', 'techlib.cz', 'aleph.techlib.cz', 'Praha');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (108, 'KVKL', 'http://www.kvkli.cz/', 'pac.kvkli.cz/arl-li/', 'Liberec');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (109, 'VPK', 'vpk.cz', 'vpk.cz', 'Liberec');
INSERT INTO library (id, name, url, catalog_url, city) VALUES (110, 'ANTIKVARIATY', 'muj-antikvariat.cz', 'muj-antikvariat.cz', null);


INSERT INTO format(format, description) VALUES('marc21-xml', 'MARC21 XML');
INSERT INTO format(format, description) VALUES('xml-marc', 'MARC21 XML');
INSERT INTO format(format, description) VALUES('dublinCore', 'Dublin Core');


INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled) VALUES (300,100,200,'mzk',13,true);
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled) VALUES (301,101,200,'nlk',14,false);
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled) VALUES (302,102,200,'mkp',8,false);
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled) VALUES (303,103,200,'kjm',0,false);
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled) VALUES (304,104,200,'nkp',14,true);
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled) VALUES (305,105,200,'vpk',14,false);
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled) VALUES (307,107,200,'ntk',14,false);
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled) VALUES (306,106,200,'tre',11,false);
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled) VALUES (308,108,200,'kvkl',14,false);
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled) VALUES (309,109,200,'vpk',14,false);
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled) VALUES (400,104,200,null,null,false);
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled) VALUES (99001,100,200,'kram-mzk',null,false);
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled) VALUES (99002,107,200,'kram-ntk',null,false);
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled) VALUES (99003,100,200,'kram-knav',null,false);
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled) VALUES (99004,104,200,'kram-nkp',null,false);
INSERT INTO import_conf (id,library_id,contact_person_id,id_prefix,base_weight,cluster_id_enabled) VALUES (500,110,200,'antik',null,false);


INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (300,'http://aleph.mzk.cz/OAI',null,'marc21','SECOND');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (301,'http://oai.medvik.cz/medvik2cpk/oai',null,'xml-marc','DAY');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (302,'http://web2.mlp.cz/cgi/oai','complete','marc21',null);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (303,'http://katalog.kjm.cz/l.dll',null,'marc21','DAY');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (304,'http://aleph.nkp.cz/OAI','NKC','marc21','SECOND');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (305,'http://sc.vpk.cz/cgi-bin/oai2',null,'marc21',null);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (306,NULL,NULL,'marc21',NULL);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (307,'http://aleph.techlib.cz/OAI',null,'marc21','SECOND');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (308,NULL,NULL,'marc21',NULL);
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (309,'http://sc.vpk.cz/cgi-bin/oai2',null,'marc21','SECOND');
INSERT INTO oai_harvest_conf (import_conf_id,url,set_spec,metadata_prefix,granularity) VALUES (400,'http://aleph.nkp.cz/OAI','AUT','marc21',null);


INSERT INTO kramerius_conf (import_conf_id,url,model,query_rows,metadata_stream) VALUES (1,99001,'http://kramerius.mzk.cz/search/api/v5.0','monograph',20,'DC');
INSERT INTO kramerius_conf (import_conf_id,url,model,query_rows,metadata_stream) VALUES (2,99002,'http://k4.techlib.cz/search/api/v5.0','monograph',20,'DC');
INSERT INTO kramerius_conf (import_conf_id,url,model,query_rows,metadata_stream) VALUES (3,99003,'http://kramerius.lib.cas.cz/search/api/v5.0','monograph',20,'DC');
INSERT INTO kramerius_conf (import_conf_id,url,model,query_rows,metadata_stream) VALUES (4,99004,'http://kramerius4.nkp.cz/search/api/v5.0','monograph',20,'DC');

INSERT INTO download_import_conf (import_conf_id,url) VALUES (500,'http://muj-antikvariat.cz/oai-all.xml');

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
INSERT INTO harvested_record_format(id, name) VALUES (24, 'OTHER_KIT');
INSERT INTO harvested_record_format(id, name) VALUES (25, 'OTHER_OBJECT');
INSERT INTO harvested_record_format(id, name) VALUES (26, 'OTHER_MIX_DOCUMENT');
INSERT INTO harvested_record_format(id, name) VALUES (100, 'OTHER_UNSPECIFIED');
