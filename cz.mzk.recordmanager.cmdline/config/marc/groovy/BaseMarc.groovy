import static cz.mzk.recordmanager.server.util.MarcCleaningUtils.*;

recordtype = "marc"
allfields = getAllFields()
fullrecord = getFullrecord()

language = translate("mzk_language.map", getLanguages(), null)
country_str = translate("mzk_country.map", getCountry(), null)

author = getFirstField "100abcd"
author_exact = getAuthorExact()
author_fuller = getFirstField "100q"
author2 = getFields "110ab:111ab:700abcd:710ab:711ab"
author_additional = getFields "505r"
author_sort_str = getAuthorForSorting()

title = getFirstField "245abnp"
title_sub = getFirstField "245b"
title_short = getFirstFieldTrim "245a"
title_exact = getFirstFieldTrim "245a"
title_full = getFirstField "245abdefghijklmnopqrstuvwxyz0123456789"
title_alt = getFields "130adfgklnpst:240a:246a:730adfgklnpst:740a"
title_old = getFields "780ast"
title_new = getFields "785ast"
title_sort = getSortableTitle()
title_auto_str = getTitleDisplay()
series = getFields "440ap:800abcdfpqt:830ap"
series2 = getFields "490a"

publisher = getPublisher()
placeOfPublication_txt_mv = getFieldsTrim "260a:264a"
publishDate = getPublishDate()
publishDateSort = getPublishDateForSorting()

dateSpan = getFields "362a"
contents = getFields "505a:505t"

isbn = getFields "020az"
issn = getFields "022ayz:440x:490x:730x:776x:780x:785x"

callnumber_str_mv = getFields "910b"
     
topic = getFields "600:610:630:650"
genre = getFields "655"
geographic = getFields "651"

url = getFields "856u"

titleSeries_search_txt_mv = getTitleSeries()
authorCorporation_search_txt_mv = getFieldsUnique "100abcdq7:110abc7:111aceq7:700abcdq7:710abc7:711aceq7:800abcdq7:810abc7:811aceq7:975abcdq7:976abc7:978abcdg7"
subjectKeywords_search_txt_mv = getFieldsUnique "072x:600abcdfgqklmprstxyz7:610abcklmprstxyz7:611aceqklmprstxyz7:630afpxyz7:648axyz7:650avxyz7:651avxyz7:653a:655avxyz7:964abcdefg:967abc"
issnIsbnIsmn_search_str_mv = getISBNISSNISMN()
sourceTitle_search_txt_mv = getFieldsUnique "773adtkxz9"
callNumber_search_txt_mv = getFieldsUnique "910b:996ch"
publisher_search_txt_mv = getFieldsTrim "260b:264b:928a:978abcdg7"
cnb_search_str = getFirstField "015az"
ipc_search_txt_mv = getInternationalPatentClassfication()

ean_str_mv = getEAN()
source_title_facet_str = getFirstField "773t"
genre_facet_str = getFirstField "655avxyz"
conspectus_facet_str_mv = getFields "072x"
publisher_str_mv = getPublisherStrMv()
author_facet_str_mv = filter("author_facet.txt", getAuthorFacet())
author_autocomplete = getAuthorAutocomplete("100abcdq:110abc:111acdegq:700abcdq:710abc:711acdegq:975abcdq:976abc")
bbox_geo = getBoundingBoxAsPolygon()
bbox_geo_str = getBoundingBox()

// no need for this field?
//statuses = getStatuses()

//published = getFirstField "260a"

availability_id_str_mv = getFieldsUnique "996w"

citation_record_type_str = getCitationRecordType();
author_find = getAuthorFind();
