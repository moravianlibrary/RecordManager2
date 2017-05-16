import static cz.mzk.recordmanager.server.util.MarcCleaningUtils.*;

recordtype = "marc"
allfields = getAllFields()
fullrecord = getFullrecord()

author_exact = getAuthorExact()
author_fuller = getFirstField "100q"

url = getFields "856u"

authorCorporation_search_txt_mv = getFieldsUnique "100abcdq7:110abc7:111aceq7:700abcdq7:710abc7:711aceq7:800abcdq7:810abc7:811aceq7:975abcdq7:976abc7:978abcdg7"
issnIsbnIsmn_search_str_mv = getISBNISSNISMN()
sourceTitle_search_txt_mv = getFieldsUnique "773adtkxz9"
callNumber_search_txt_mv = getFieldsUnique "910b:996ch"
publisher_search_txt_mv = getFieldsTrim "260b:264b:928a:978abcdg7"
cnb_search_str = getFirstField "015az"

ean_str_mv = getEAN()
source_title_facet_str = getFirstField "773t"
genre_facet_str = getFirstField "655avxyz"
conspectus_facet_str_mv = getFields "072x"
publisher_str_mv = getPublisherStrMv()
author_autocomplete = getAuthorAutocomplete("100abcdq:110abc:111acdegq:700abcdq:710abc:711acdegq:975abcdq:976abc")
bbox_geo = getBoundingBoxAsPolygon()
bbox_geo_str = getBoundingBox()

citation_record_type_str = getCitationRecordType();
cpk_detected_format_facet_str_mv = getFormat()

subjectKeywords_search_txt_mv = getFields "100abcd:400abcd:500abcd"
people_search_txt_mv = getFields "100abcd:400abcd:500abcd"

id_authority = getId001()
