recordtype = "dublincore"
record_format = "dublincore"

fullrecord = getFullRecord()
title = getFirstTitle()
physical = getPhysicals();
url = getUrls();
barcodes = getBarcodes()
_hidden_index_when_merged_boolean = getIndexWhenMerged()
uuid_str_mv = getUuidForObalkyKnih()

// facet
local_region_institution_facet_mv = local_region_institution_facet_str_mv = getRegionInstitutionFacet()
local_statuses_facet_mv = local_statuses_facet_str_mv = getStatuses()

// search
author_search_txt_mv = getAllCreatorsForSearching()
_hidden_title_search_txt_mv = getFirstTitle()

// display
author2_display_mv = getAuthor2Display()
author_display = getAuthorDisplay()
author_sort_display = getAuthorForSorting()
format_display_mv = getFormat()
isbn_display_mv = getISBNs()
language_display_mv = getLanguages()
oclc_display_mv = getOclcs()
publishDate_display = getFirstDate()
publisher_display_mv = getPublishers()
title_display = getFirstTitle()

// view
view_txt_mv = getViewType()
