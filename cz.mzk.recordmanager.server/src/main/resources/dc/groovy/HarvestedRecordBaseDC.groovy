recordtype = "dublincore"
record_format = "dublincore"

fullrecord = getFullRecord()

local_statuses_facet_mv = local_statuses_facet_str_mv = getStatuses()

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
