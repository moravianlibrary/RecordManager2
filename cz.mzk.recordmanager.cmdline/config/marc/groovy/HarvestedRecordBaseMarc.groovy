recordtype = "local"
fullrecord = getFullrecord()

title_display = getTitleDisplay()
title_sub_display = getFirstField "245b"

author_display = getAuthorDisplay()
author_authority_display = getAuthorAuthorityDisplay()
author2_display_mv = getAuthor2Display()
authority2_display_mv = getAuthIds("110:111:700:710:711")
corp_author_display = getFirstField "110ab:111aq"

isbn = getIsbnForSearching "020az:902a"
issn = getFields "022ayz:440x:490x:730x:776x:780x:785x"
ismn_isn_mv = getIsmns()
cnb_isn_mv = getFieldsUnique "015az"
ean_isn_mv = getEAN()

publishDate_display = getPublishDateDisplay()

ean_display_mv = getEAN()
isbn_display_mv = getFieldsTrim "020a"
nbn_display = getFirstField "015a"
oclc_display_mv = getOclcs()

url = getUrls()
local_statuses_facet_str_mv = getStatuses()
subject_facet_str_mv = getSubject "600abcdfglnpqstyz:610abcdfgklnpstyz:611abcdefgklnpqstyz:630adfgklnpstyz:648a:651avxyz:964abcdefg:967ab"
subject_str_mv = getSubject "600abcdfglnpqstyz:610abcdfgklnpstyz:611abcefgklpqstyz:630adfgklnpstyz:648a:651avxyz:964abcdefg:967ab"
genre_facet_str_mv = getGenreFacet "655avxyz"

_hidden_viz_dummy_field = getAuthorityIds "auth.1007:1107:1117:6007:6107:6117:6487:6507:6517:6557:7007:7107:7117|mesh.650a:651a:655a|agrovoc.650a"

sfx_links = getSfxIds()

loanRelevance = getLoanRelevance();

id001_search = getControlField("001")

conspectus_str_mv = getConspectus();

link773_str = get773link();
f773_display = get773display();
id001_str = getControlField("001")

metaproxy_boolean = getMetaproxyBool()
barcodes = getBarcodes()
format_display_mv = getFormat()
local_region_institution_facet_str_mv = getRegionInstitutionFacet()
_hidden_index_when_merged_boolean = getIndexWhenMerged()
item_id_txt_mv = getFirstField "996t"
view_txt_mv = getViewType()
author_search_txt_mv = toLowerCase(getFields("100abcd:110ab:111ab:700abcd:710ab:711ab"))
title_search_txt_mv = toLowerCase(getFields("245abnp"))
callNumber_search_txt_mv = getFieldsUnique "910b:996ch"
summary_display_mv = getFields "520a"
publisher_display_mv = getPublisherLocal()
institution_view_facet_str_mv = getInstitutionViewFacet()
monographic_series_txt_mv = getSeriesForSearching()
monographic_series_display_mv = getSeriesForDisplay()
ziskej_boolean = getZiskejBool()
similar_display_mv = getSimilar()
periodical_availability_int_mv = getPeriodicalAvailability()
sigla_display = getSigla()

_hidden_isbn_annotation_obalkyknih = getFields "020a:902a"
