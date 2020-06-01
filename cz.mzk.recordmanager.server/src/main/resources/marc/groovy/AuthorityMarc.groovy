recordtype = "authority"
record_format = "authority"

fullrecord = getFullrecord()

source = getFields "670a"
heading = getFirstAuthAuthor "100"
heading_display = getFirstAuthAuthor "100"
use_for = getAuthAuthors "400"
see_also = getFields "500abcd"
scope_note = getFields "678a"
url = getUrls()

id_authority = getControlField("001")

subject_facet_str_mv = getFields "100abcd"

authorityId_str = getAuthorityId();
format_display_mv = getFormat()
