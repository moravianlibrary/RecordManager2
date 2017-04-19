recordtype = "local"
fullrecord = getFullrecord()

title_display = getTitleDisplay()
title_sub_display = getFirstField "245b"

author_display = getAuthorDisplay()
author2_display_mv = getAuthor2Display()
authority2_display_mv = getAuthIds("110:111:700:710:711")

publishDate_display = getPublishDateDisplay()

ean_display_mv = getEAN()
isbn_display_mv = getFieldsTrim "020a"
nbn_display = getFirstField "015a"

url = getUrls()
local_statuses_facet_str_mv = getStatuses()

holdings_996_str_mv = getHoldings996()

_hidden_authority_dummy_field = getAuthorityIds "1007:7007"
_hidden_corporation_dummy_field = getAuthorityIds "1107:1117:7107:7117"
_hidden_subject_dummy_field = getAuthorityIds "6007:6107:6117:6487:6507:6517"
_hidden_genre_dummy_field = getAuthorityIds "6557"

sfx_links = getSfxIds()

loanRelevance = getLoanRelevance();

id001_search = getId001()

barcodes = getBarcodes()
format_display_mv = getFormat()
