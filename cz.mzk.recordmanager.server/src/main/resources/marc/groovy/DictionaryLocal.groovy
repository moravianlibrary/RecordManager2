recordtype = "dictionary"

fullrecord = getFullrecord()

title = getFirstField "150a"

english_display = getFirstField "750a"
explanation_display = getFirstField "678a"
relative_display_mv = getFields "550a"
alternative_display_mv = getFields "450a"
source_display = getFirstField "650a"
format_display_mv = getFormat()

url = getUrls()

subject_facet_str_mv = subject_str_mv = Arrays.asList("Knihovnictví", "Informační věda")
local_institution_facet_str_mv = getInstitutionFacet()
