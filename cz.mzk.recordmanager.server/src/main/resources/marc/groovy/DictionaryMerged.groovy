record_format = "marc"

allfields = getAllFields()
fullrecord = getFullrecord()

term_exact = getFirstField "150a"

term_txt_mv = getFields "150a:450a:550a"

// facets
record_format_facet_mv = getFormat()

// sort
title_sort = toLowerCase(getFirstField("150a"))
