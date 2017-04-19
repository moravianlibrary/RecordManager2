recordtype = "authority"

fullrecord = getFullrecord()

source = getFields "670a"
heading = getFirstAuthAuthor "100"
heading_display = getFirstAuthAuthor "100"
use_for = getAuthAuthors "400"
see_also = getFields "500abcd"
scope_note = getFields "678a"
url = getAuthorityUrl "856u"

id_authority = getId001()

subjectKeywords_search_txt_mv = getFields "100abcd:400abcd:500abcd"
people_search_txt_mv = getFields "100abcd:400abcd:500abcd"
subject_facet_str_mv = getFields "100abcd"

short_note_cs_display = translate("auth_short_note_cs.map", getId001(), null)
short_note_en_display = translate("auth_short_note_en.map", getId001(), null)

format_display_mv = getFormat()
