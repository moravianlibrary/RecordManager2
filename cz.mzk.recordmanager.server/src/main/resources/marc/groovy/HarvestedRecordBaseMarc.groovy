recordtype = "local"
fullrecord = getFullrecord()

language_display_mv = translate("mzk_language.map", getLanguages(), null)
country_display_mv = translate("mzk_country.map", getCountry(), null)

title_display = getTitleDisplay()
title_sub_display = getFirstField "245b"

author_display = getAuthorDisplay()
author2_display_mv = getAuthor2Display()

publishDate_display = getPublishDateDisplay()

ean_display_mv = getEAN()
isbn_display_mv = getFieldsTrim "020a"
nbn_display = getFirstField "015a"

url = getUrls()
local_statuses_facet_str_mv = getStatuses()

holdings_996_str_mv = getHoldings996()

_hidden_authority_dummy_field = getFields "1007:7007"
_hidden_authors_dummy = getFields "100abcd:700abcd"
sfx_links = getSfxIds()

barcodes = getFields "996b"

loanRelevance = getLoanRelevance();

id001_search = getId001()
