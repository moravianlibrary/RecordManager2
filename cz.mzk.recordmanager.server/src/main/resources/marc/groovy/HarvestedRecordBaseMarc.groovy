recordtype = "local"
fullrecord = getFullrecord()

language_display_mv = translate("mzk_language.map", getLanguages(), null)
country_display_mv = translate("mzk_country.map", getCountry(), null)

title_display = getFirstField "245abnp"
title_sub_display = getFirstField "245b"

author_display = getFirstField "100abcd"
author2_display_mv = getFields "110ab:111ab:700abcd:710ab:711ab"

publishDate_display = getFirstField "260c"

format_display_mv = getRecordType()

ean_display_mv = getEAN()
isbn_display_mv = getFields "020a"
nbn_display = getFirstField "015a"

url = getFields "856u"
