recordtype = "dublincore"
fullrecord = getFullRecord()

local_statuses_facet_str_mv = getStatuses()

_hidden_authority_dummy_field = ""
kramerius_dummy_rights = getRights()

title = getFirstTitle()
title_display = getFirstTitle()

author_display = getAuthorDisplay()
author2_display_mv = getAuthor2Display()

publishDate_display = getFirstDate()

isbn_display_mv = getISBNs()

physical = getPhysicals();

url = getUrls();

barcodes = getBarcodes()
