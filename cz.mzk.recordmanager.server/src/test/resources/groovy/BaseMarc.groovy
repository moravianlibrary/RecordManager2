lccn = getFirstField "010a"
ctrlnum = getFirstField "035a"

recordtype = "marc"
allfields = getAllFields()

language = translate("mzk_language.map", getLanguages() , null)
cpk_detected_format_txtF_mv = getRecordType()

author = getFirstField "100abcd"
author_fuller = getFirstField "100q"
author_letter = getFirstField "100a"
author2 = getFields "110ab:111ab:700abcd:710ab:711ab"
author2_role = getFields "700e:710e"
author_additional = getFields "505r"

title = getFirstField "245abnp"
title_sub = getFirstField "245b"
title_full = getFields "245abdefghijklmnopqrstuvwxyz0123456789"
title_auth = getFirstField "245ab"
title_alt = getFields "130adfgklnpst:240a:246a:730adfgklnpst:740a"
title_old = getFields "780ast"
title_new = getFields "785ast"
title_sort = getSortableTitle()
series = getFields "440ap:800abcdfpqt:830ap"
series2 = getFields "490a"

publisher = getFields "260b"
publishDate_display = getFirstField "260c"
placeOfPublication_txt_mv = getFields "260a"
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

topic_facet = getFields "600x:610x:611x:630x:648x:650a:650x:651x:655x"
genre_facet = getFields "600v:610v:611v:630v:648v:650v:651v:655a:655v"
geographic_facet = getFields "600z:610z:611z:630z:648z:650z:651a:651z:655z"

url = getFields "856u"

ean_str_mv = getEAN()

illustrated = isIllustrated()

bbox_geo = getBoundingBox()
bbox_geo_str = getBoundingBox()

// deprecated
title_display = getFields "245abnp"

published = getFirstField "260a"
