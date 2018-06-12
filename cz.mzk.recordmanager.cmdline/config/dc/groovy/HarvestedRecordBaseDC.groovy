recordtype = "dublincore"
fullrecord = getFullRecord()

local_statuses_facet_str_mv = getStatuses()

title = getFirstTitle()
title_display = getFirstTitle()

author_display = getAuthorDisplay()
author2_display_mv = getAuthor2Display()

publishDate_display = getFirstDate()

isbn_display_mv = getISBNs()

physical = getPhysicals();

url = getUrls();

barcodes = getBarcodes()
format_display_mv = getFormat()
local_institution_facet_str_mv = getInstitutionFacet()
_hidden_index_when_merged_boolean = getIndexWhenMerged()
author_search_txt_mv = getAllCreatorsForSearching()
title_search_txt_mv = getFirstTitle()
