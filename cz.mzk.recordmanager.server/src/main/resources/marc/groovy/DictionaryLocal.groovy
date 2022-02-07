record_format = "dictionary"

fullrecord = getFullrecord()

title = getFirstField "150a"

view_txt_mv = getViewType()

author_sort_display = getAuthorForSorting("AUTa")
english_display = getFirstField "750a"
explanation_display = getFirstField "678a"
relative_display_mv = getFields "550a"
alternative_display_mv = getFields "450a"
source_display = getFirstField "650a"
format_display_mv = getFormat()
author_term_display_mv = getFields "AUTa"

url = getUrls()

// facets
local_institution_view_facet_mv = getInstitutionViewFacet()
local_region_institution_facet_mv = getRegionInstitutionFacet()
subject_facet_mv = subject_str_mv = Arrays.asList("Knihovnictví", "Informační věda")
