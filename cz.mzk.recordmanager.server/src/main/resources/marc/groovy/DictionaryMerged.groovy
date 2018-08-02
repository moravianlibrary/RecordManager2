recordtype = "marc"
allfields = getAllFields()
fullrecord = getFullrecord()

cpk_detected_format_facet_str_mv = getFormat()
title_sort = toLowerCase(getFirstField("150a"))
term_exact = getFirstField "150a"

term_txt_mv = getFields "150a:450a:550a"
