import static cz.mzk.recordmanager.server.scripting.marc.function.BoundingBoxMarcFunctions.LongLatFormat.*

recordtype = "local"
record_format = "local"

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
subject_str_mv = getSubject "600abcdfglnpqstyz:610abcdfgklnpstyz:611abcefgklpqstyz:630adfgklnpstyz:648a:651avxyz:964abcdefg:967ab"

_hidden_viz_dummy_field = getAuthorityIds "auth.1007:1107:1117:1307:6007:6107:6117:6307:6487:6507:6517:6557:7007:7107:7117:7307|mesh.650a:651a:655a|agrovoc.650a"

sfx_links = getSfxIds()

loanRelevance = getLoanRelevance();

id001_search = getControlField("001")

link773_str = get773link();
f773_display = get773display();
id001_str = getControlField("001")

metaproxy_boolean = getMetaproxyBool()
barcodes = getBarcodes()
format_display_mv = getFormat()
_hidden_index_when_merged_boolean = getIndexWhenMerged()
item_id_txt_mv = getFirstField "996t"
view_txt_mv = getViewType()
author_search_txt_mv = toLowerCase(getFields("100abcd:110ab:111ab:700abcd:710ab:711ab"))
_hidden_title_search_txt_mv = toLowerCase(getFields("245abnp"))
callNumber_search_txt_mv = getFieldsUnique "910b:996ch"
summary_display_mv = getFields "520ab"
publisher_display_mv = getPublisherLocal()
monographic_series_txt_mv = getSeriesForSearching()
monographic_series_display_mv = getSeriesForDisplay()
ziskej_boolean = getZiskejBool()
edd_boolean = getEddBool()
similar_display_mv = getSimilar()
fulltext_analyser_txt_mv = getFulltextAnalyser()
semantic_enrichment_txt_mv = getSemanticEnrichment()
auto_conspectus_txt_mv = getAutoConspectus()
periodical_availability_int_mv = getPeriodicalAvailability()
sigla_display = getSigla()
aleph_adm_id_txt_mv = getAlephAdmId()
mappings996_display_mv = getMappings996()
uuid_str_mv = getUuidForObalkyKnih()

_hidden_isbn_annotation_obalkyknih = getFields "020a:902a"

// facets
conspectus_facet_mv = conspectus_str_mv = getConspectus();
genre_facet_mv = genre_facet_str_mv = getGenreFacet "655avxyz"
local_institution_view_facet_mv = institution_view_facet_str_mv = getInstitutionViewFacet()
local_region_institution_facet_mv = local_region_institution_facet_str_mv = getRegionInstitutionFacet()
local_statuses_facet_mv = local_statuses_facet_str_mv = getStatuses()
subject_facet_mv = subject_facet_str_mv = getSubject "600abcdfglnpqstyz:610abcdfgklnpstyz:611abcdefgklnpqstyz:630adfgklnpstyz:648a:651avxyz:964abcdefg:967ab"
ziskej_facet_mv = getZiskejFacet()
scale_int_facet_mv = getScaleFacet()

// geographic search
long_lat_display_mv = getBoundingBoxAsPolygon(ENVELOPE)
