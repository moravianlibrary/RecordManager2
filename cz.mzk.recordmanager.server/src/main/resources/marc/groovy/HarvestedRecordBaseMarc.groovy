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

_hidden_viz_dummy_field = getAuthorityIds "auth.1007:1107:1117:6007:6107:6117:6487:6507:6517:6557:7007:7107:7117|mesh.650a:651a:655a"

sfx_links = getSfxIds()

loanRelevance = getLoanRelevance();

id001_search = getId001()
