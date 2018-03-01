recordtype = "authority"

fullrecord = getFullrecord()

url = getUrls()

subject_facet_str_mv = getFields "100abcd"

format_display_mv = getFormat()
local_institution_facet_str_mv = getInstitutionFacet()

authority_id_display = getId001()
bibliographic_details_display_mv = getFields "678a"
personal_name_display = getFirstAuthAuthor "100"
alternative_name_display_mv = getAuthAuthors "400"
source_display_mv = getFields "670a"
pseudonym_name_display_mv = getAuthorityPseudonymsNames()
pseudonym_ids_display_mv = getAuthorityPseudonymsIds()
