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

subject_facet_str_mv = getFields "100abcd"

short_note_cs_display = translate("auth_short_note_cs.map", getId001(), null)
short_note_en_display = translate("auth_short_note_en.map", getId001(), null)

authorityId_str = getAuthorityId();
format_display_mv = getFormat()
local_institution_facet_str_mv = getInstitutionFacet()

authority_id_display = getId001()
bibliographic_details_display_mv = getFields "678a"
personal_name_display = getFirstField "100abd"
alternative_name_display_mv = getFields "400abd"
source_display_mv = getFields "670a"
