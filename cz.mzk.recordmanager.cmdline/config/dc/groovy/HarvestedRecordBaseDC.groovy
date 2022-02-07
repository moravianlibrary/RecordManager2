record_format = "dublincore"

fullrecord = getFullRecord()
title = getFirstTitle()
physical = getPhysicals();
url = getUrls();
barcodes = getBarcodes()
_hidden_index_when_merged_boolean = getIndexWhenMerged()
uuid_str_mv = getUuidForObalkyKnih()

// facet
local_region_institution_facet_mv = getRegionInstitutionFacet()

// search
author_search_txt_mv = getAllCreatorsForSearching()
_hidden_title_search_txt_mv = getFirstTitle()

// display
author2_display_mv = getAuthor2Display()
author_display = getAuthorDisplay()
format_display_mv = getFormat()
isbn_display_mv = getISBNs()
language_display_mv = getLanguages()
oclc_display_mv = getOclcs()
publishDate_display = getFirstDate()
publisher_display_mv = getPublishers()
title_display = getFirstTitle()

// view
view_txt_mv = getViewType()
