lccn = getFirstField "010a"
ctrlnum = getFirstField "035a"

recordtype = "marc"
allfields = getAllFields()
fullrecord = getFullrecord()

language = translate("mzk_language.map", getLanguages(), null)
country_txt = translate("mzk_country.map", getCountry(), null)

author = getFirstField "100abcd"
author_fuller = getFirstField "100q"
author_letter = getFirstField "100a"
author2 = getFields "110ab:111ab:700abcd:710ab:711ab"
author2_role = getFields "700e:710e"
author_additional = getFields "505r"

title = getFirstField "245abnp"
title_sub = getFirstField "245b"
title_full = getFirstField "245abdefghijklmnopqrstuvwxyz0123456789"
title_auth = getFirstField "245ab"
title_alt = getFields "130adfgklnpst:240a:246a:730adfgklnpst:740a"
title_old = getFields "780ast"
title_new = getFields "785ast"
title_sort = getSortableTitle()
series = getFields "440ap:800abcdfpqt:830ap"
series2 = getFields "490a"

publisher = getFields "260b"
publishDate_display = getFirstField "260c"
placeOfPublication_txt_mv = getFieldsTrim "260a:264a"
publishDate = getPublishDate()
publishDateSort = getPublishDateForSorting()

physical = getFields "300abcefg:530abcd"
dateSpan = getFields "362a"
edition = getFirstField "250a"
contents = getFields "505a:505t"

isbn = getFields "020a"
issn = getFields "022a:440x:490x:730x:776x:780x:785x"

callnumber_str_mv = getFields "910b"

topic = getFields "600:610:630:650"
genre = getFields "655"
geographic = getFields "651"

url = getFields "856u"

ean_str_mv = getEAN()
cpk_detected_format_txtF_mv = getRecordType()
illustrated = isIllustrated()

bbox_geo_str = getBoundingBox()

statuses = getStatuses()

// deprecated
title_display = getFirstField "245abnp"

//published = getFirstField "260a"

availability_id_str_mv = getFieldsUnique "996w"

holdings_996_str_mv = getHoldings996()
