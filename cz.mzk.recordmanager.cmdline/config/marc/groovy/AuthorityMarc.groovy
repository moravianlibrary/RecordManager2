recordtype = "authority"
record_format = "authority"

fullrecord = getFullrecord()
url = getUrls()

// view
view_txt_mv = getViewType()

// facets
local_region_institution_facet_str_mv = getRegionInstitutionFacet()
subject_facet_str_mv = subject_str_mv = getFields "100abcd"

// display fields
alternative_name_display_mv = getAuthAuthors "400"
authority_id_display = getAuthorityRecordId()
bibliographic_details_display_mv = getFields "678a"
format_display_mv = getFormat()
personal_name_display = getFirstAuthAuthor "100"
pseudonym_ids_display_mv = getAuthorityPseudonymsIds()
pseudonym_name_display_mv = getAuthorityPseudonymsNames()
source_display_mv = getFields "670a"
