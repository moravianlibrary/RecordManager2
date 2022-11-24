import static cz.mzk.recordmanager.server.marc.SubfieldExtractionMethod.SEPARATED

recordtype = "authority"
record_format = "authority"

fullrecord = getFullrecord()
url = getUrls()

// view
view_txt_mv = getViewType()

// facets
local_region_institution_facet_mv = local_region_institution_facet_str_mv = getRegionInstitutionFacet()
subject_facet_mv = subject_facet_str_mv = subject_str_mv = getFields "100abcd"

// display fields
alternative_name_display_mv = getAuthAuthors "400"
authority_id_display = getAuthorityRecordId()
author_sort_display = getAuthorForSorting("100abcd:110abcd:111abcd:700abcd:710abcd:711abcd")
bibliographic_details_display_mv = getFields "678a"
format_display_mv = getFormat()
occupation_display_mv = getFields "374a", SEPARATED
personal_name_display = getFirstAuthAuthor "100"
pseudonym_ids_display_mv = getAuthorityPseudonymsIds()
pseudonym_name_display_mv = getAuthorityPseudonymsNames()
source_display_mv = getFields "670a"
