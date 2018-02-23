import static cz.mzk.recordmanager.server.util.MarcCleaningUtils.*;

recordtype = "marc"
allfields = getAllFields()
fullrecord = getFullrecord()

author_exact = getAuthorExact()
author_fuller = getFirstField "100q"

url = getFields "856u"

authorCorporation_search_txt_mv = getFieldsUnique "100abcdq7:110abc7:111aceq7:700abcdq7:710abc7:711aceq7:800abcdq7:810abc7:811aceq7:975abcdq7:976abc7:978abcdg7"

author_autocomplete = getAuthorAutocomplete("100abcdq:110abc:111acdegq:700abcdq:710abc:711acdegq:975abcdq:976abc")

citation_record_type_str = getCitationRecordType();
cpk_detected_format_facet_str_mv = getFormat()

subjectKeywords_search_txt_mv = getFields "100abcd:400abcd:500abcd"
people_search_txt_mv = getFields "100abcd:400abcd:500abcd"

id_authority = getId001()

bibliographic_details_display_mv = getFields "678a"
