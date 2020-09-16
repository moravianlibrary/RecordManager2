recordtype = "marc"
record_format = "marc"

allfields = getAllFields()
fullrecord = getFullrecord()

title_sort = toLowerCase(getFirstField("150a"))
term_exact = getFirstField "150a"

term_txt_mv = getFields "150a:450a:550a"

// facets
record_format_facet_mv = cpk_detected_format_facet_str_mv = getFormat()
