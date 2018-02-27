recordtype = "authority"

fullrecord = getFullrecord()

source = getFields "670a"
heading = getFirstAuthAuthor "100"
heading_display = getFirstAuthAuthor "100"
use_for = getAuthAuthors "400"
see_also = getFields "500abcd"
scope_note = getFields "678a"
url = getUrls()

id_authority = getId001()

subject_facet_str_mv = getFields "100abcd"

short_note_cs_display = translate("auth_short_note_cs.map", getId001(), null)
short_note_en_display = translate("auth_short_note_en.map", getId001(), null)

authorityId_str = getAuthorityId();
format_display_mv = getFormat()
local_institution_facet_str_mv = getInstitutionFacet()
