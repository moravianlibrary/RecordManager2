recordtype = "dictionary"
record_format = "dictionary"

fullrecord = getFullrecord()

title = getFirstField "150a"

view_txt_mv = getViewType()

english_display = getFirstField "750a"
explanation_display = getFirstField "678a"
relative_display_mv = getFields "550a"
alternative_display_mv = getFields "450a"
source_display = getFirstField "650a"
format_display_mv = getFormat()
author_term_display_mv = getFields "AUTa"

url = getUrls()

subject_facet_str_mv = subject_str_mv = Arrays.asList("Knihovnictví", "Informační věda")
local_region_institution_facet_str_mv = getRegionInstitutionFacet()
institution_view_facet_str_mv = getInstitutionViewFacet()
