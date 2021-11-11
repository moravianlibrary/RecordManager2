import static cz.mzk.recordmanager.server.util.MarcCleaningUtils.*;
import static cz.mzk.recordmanager.server.marc.SubfieldExtractionMethod.*;

recordtype = "ebsco"
allfields = getAllFields()
fullrecord = getFullrecord()
lccn = getFields "010a"
ctrlnum = getFields "035a"

mzk_visible_str = getMZKVisible()

format = translate("mzk_format.map", "Electronic", null)
statuses = "available_online"

language = translate("mzk_language.map", getMZKLanguages(), null)
language_display_mv = translate("mzk_language.map", getLanguages(), null)

country_txt = translate("mzk_country.map", getCountry(), null)
country_display_mv = translate("mzk_country.map", getCountry(), null)

author = getFirstField "100abcd"
author_fuller = getFirstField "100q"
author2 = getFields "110ab:111ab:700abcd:710ab:711ab"
author_additional = getFields "505r"
author_display = getFirstField "100abcd"
author2_display_mv = getFields "110ab:111ab:700abcd:710ab:711ab"

title = getFirstField "245abnp"
title_sub = getFirstField "245b"
title_short = getFirstField "245a" // FIXME: getShortTitle()
title_full = getFirstField "245abdefghijklmnopqrstuvwxyz0123456789"
title_auth = getFirstField "245ab"
title_alt = getFields "130adfgklnpst:240a:246a:730adfgklnpst:740a"
title_old = getFields "780ast"
title_new = getFields "785ast"
title_sort = getSortableTitle()
title_display = getFirstField "245abnp"
title_sub_display = getFirstField "245b"

series = getFields "440ap:800abcdfpqt:830ap"
series2 = getFields "490a"

publisher = record.getFields("260", 'b' as char) ?: record.getFields("264", { field -> field.getIndicator2() == '1' }, 'b' as char)
publishDate_display = record.getField("260", 'c' as char) ?: record.getFields("264", { field -> field.getIndicator2() == '1' }, 'c' as char)?.find { true }
placeOfPublication_txt_mv = getMZKPlaceOfPublication()
publishDate = getPublishDate()
publishDate_txt_mv = getPublishDate()
publishDateSort = getPublishDateForSortingForMzk()

physical = getFields "300abcefg:530abcd"
dateSpan = getFields "362a"
edition = getFirstField "250a"
contents = getFields "505a:505t"

isbn = getFields "020a:902a"
isbn_display_mv = getFields "020a"
issn = getFields "022a:440x:490x:730x:776x:780x:785x"
ismn_isn_mv = record.getFields("024", { field -> field.getIndicator1() == '2' }, 'a' as char)

topic = getFields "600:610:630:650"
genre = getFields "655"
geographic = getFields "651"

topic_cs_str_mv = record.getFields("650", { field -> field.getIndicator2() == '7' }, 'a' as char)
topic_en_str_mv = record.getFields("650", { field -> field.getIndicator2() == '9' }, 'a' as char)
topic_facet = getMZKTopicFacets()
genre_facet = getFields "655avxyz"
genre_cs_str_mv = getMZKGenreFacets('7' as char)
genre_en_str_mv = getMZKGenreFacets('9' as char)
geographic_facet = getFields "600z:610z:611z:630z:648z:650z:651a:651z:655z"
geographic_cs_str_mv = getMZKGeographicFacets('7' as char)
geographic_en_str_mv = getMZKGeographicFacets('9' as char)

url = getFields "856u"

illustrated = "FIXME"

bbox_geo = getBoundingBoxAsPolygon()
bbox_geo_str = getBoundingBox()

availability_id_str = getFirstField "996w"

fulltext = "" // custom, getFullText()
topic = getMZKKeywords()
relevancy_str = getMZKRelevancy()

callnumber_str_mv = getFields "910b", SEPARATED
callnumber_second_str_mv = getFields("996h").collect{it -> it.replace(' ', '|')}

// source = "MZK"
nbn = getFirstField "015a"
acq_int = getMZKAcquisitionDate()
category_txtF = translate("conspectus_category.map",
        getRecord().getDataFields("072").findAll{ df -> df?.getSubfield('2' as char)?.getData() == 'Konspekt' }
                .collect{ df -> df?.getSubfield('9' as char)?.getData() }.find{ true }, null);
subcategory_txtF = getRecord().getDataFields("072").findAll{ df -> df?.getSubfield('2' as char)?.getData() == 'Konspekt' }
        .collect{ df -> df?.getSubfield('x' as char)?.getData() }.find{ true };
base_txtF_mv = Collections.singletonList("eperiodicals");
barcode_str_mv = getFields "996b"
sysno_str = getMZKSysno()
author_title_str = getMZKAuthorAndTitle()
udc_str_mv = getFields "080a"

barcodes = getBarcodes()

_hidden_viz_dummy_field = getAuthorityIds "auth.1007:1107:1117:6007:6107:6117:6487:6507:6517:6557:7007:7107:7117|mesh.650a:651a:655a|agrovoc.650a"
