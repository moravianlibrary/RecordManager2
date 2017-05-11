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
subject_facet_str_mv = getSubject "600abcdfglnpqstyz:610abcdfgklnpstyz:611abcdefgklnpqstyz:630adfgklnpstyz2:648a:650avyz:651avxyz:964abcdefg:967ab"
genre_facet_str_mv = getFields "655avxyz"
holdings_996_str_mv = getHoldings996()

_hidden_viz_dummy_field = getAuthorityIds "auth.1007:1107:1117:6007:6107:6117:6487:6507:6517:6557:7007:7107:7117|psh.650a:699a|mesh.650a:651a:655a"

sfx_links = getSfxIds()

loanRelevance = getLoanRelevance();

id001_search = getId001();

conspectus_str_mv = getConspectus();

link773_str = get773link();
f773_display = get773display();
id001_str = getId001();
