import org.apache.commons.collections4.ListUtils;
import static cz.mzk.recordmanager.server.util.MarcCleaningUtils.*;

record_format = "marc"

allfields = getAllFields()
fullrecord = getFullrecord()

author = getFirstField "100abcd"
author_exact = getAuthorExact()
author_fuller = getFields "100q"
author2 = getFields "110ab:111ab:700abcd:710ab:711ab"
author_additional = getFields "505r"

title = getFirstField "245abnp"
title_sub = getFirstField "245b"
title_short = getFirstFieldTrim "245a"
title_exact = getFirstFieldTrim "245a"
title_full = getFirstField "245abdefghijklmnopqrstuvwxyz0123456789"
title_alt = getFields "130adfgklnpst:240a:246a:730adfgklnpst:740a"
title_old = getFields "780ast"
title_new = getFields "785ast"
title_auto_str = getTitleDisplay()
series = getFields "440ap:800abcdfpqt:830ap"
series2 = getFields "490a"

publisher = getPublisher()
placeOfPublication_txt_mv = getFieldsTrim "260a:264a"
publishDate_int_mv = getPublishDateForTimeline()

dateSpan = getFields "362a"
contents = getFields "505a:505t"

callnumber_str_mv = getFields "910b"

topic = getAllSubfields "600:610:611:630:650:653:656"
genre = getAllSubfields "655"
geographic = getAllSubfields "651"
era = getAllSubfields "648"

url = getFields "856u"

titleSeries_search_txt_mv = getTitleSeries()
authorCorporation_search_txt_mv = getFieldsUnique "100abcdq7:110abc7:111aceq7:700abcdq7:710abc7:711aceq7:800abcdq7:810abc7:811aceq7:975abcdq7:976abc7:978abcdg7"
subjectKeywords_search_txt_mv = getFieldsUnique "072x:600abcdfgqklmprstxyz7:610abcklmprstxyz7:611aceqklmprstxyz7:630afpxyz7:648axyz7:650avxyz7:651avxyz7:653a:655avxyz7:964abcdefg:967abc"
issnIsbnIsmn_search_str_mv = getISBNISSNISMN()
sourceTitle_search_txt_mv = getFieldsUnique "773adtkxz9"
publisher_search_txt_mv = getFieldsTrim "260b:264b:928a:978abcdg7"
cnb_search_str = getFirstField "015az"
upv_ipc_search = getInternationalPatentClassfication()
upv_txt_mv = getInternationalPatentClassfication()
country_search_txt_mv = ListUtils.union(translate("mzk_country.map", getCountries(), null), translate("country_cs.map", getCountries(), null))
language_search_txt_mv = ListUtils.union(translate("mzk_language.map", getLanguages(), null), translate("language_cs.map", getLanguages(), null))
format_search_txt_mv = getFormatForSearching()
publishDate_txt_mv = getPublishDate()

author_str_mv = getAuthorAutocomplete("100abcdq:110abc:111acegq:700abcdq:710abc:711acegq:975abcdq:976abc")
bbox_geo = getBoundingBoxAsPolygon()
bbox_geo_str = getBoundingBox()

// no need for this field?
//statuses = getStatuses()

//published = getFirstField "260a"

availability_id_str_mv = getFieldsUnique "996w"

citation_record_type_str = getCitationRecordType();
author_find = getAuthorFind();

// facets
author_facet_mv = author_facet_str_mv = filter("author_facet.txt", getAuthorFacet())
country_facet_mv = country_str_mv = translate("mzk_country.map", getCountries(), null)
language_facet_mv = language = translate("mzk_language.map", getLanguages(), null)
publishDate_facet_mv = publishDate = getPublishDate()
publisher_facet_mv = publisher_str_mv = getPublisherStrMv()
record_format_facet_mv = getFormat()
source_title_facet = source_title_facet_str = getFirstField "773t"

// sort
author_sort = author_sort_cz = getAuthorForSorting()
publishDate_sort = publishDateSort = getPublishDateForSorting()
title_sort = title_sort_cz = getSortableTitle()

// hidden
_hidden_potential_dnnt = potentialDnnt()
