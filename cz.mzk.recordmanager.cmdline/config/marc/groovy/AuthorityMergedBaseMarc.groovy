recordtype = "marc"
allfields = getAllFields()
fullrecord = getFullrecord()
citation_record_type_str = getCitationRecordType()

// autocompete
author_autocomplete = getAuthorAutocomplete("100abcdq:110abc:111acegq:700abcdq:710abc:711acegq:975abcdq:976abc")

// facets
cpk_detected_format_facet_str_mv = getFormat()

// search
author_exact = getAuthorExact()
author_fuller = getFirstField "100q"
authorCorporation_search_txt_mv = getFieldsUnique "100abcdq7:110abc7:111aceq7:700abcdq7:710abc7:711aceq7:800abcdq7:810abc7:811aceq7:975abcdq7:976abc7:978abcdg7"
id_authority = getControlField("001")
people_search_txt_mv = getFields "100abcd:400abcd:500abcd"
subjectKeywords_search_txt_mv = getFields "100abcd:400abcd:500abcd"

// display
bibliographic_details_display_mv = getFields "678a"
