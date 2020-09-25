recordtype = "dublinCore"
record_format = "dublinCore"

fullrecord = getFullRecord()

allfields = getAllFields()

author = getFirstCreator()
author_search = getFirstCreator()
author2 = getOtherCreators()
author_find = getAuthorFind();

isbn = getISBNs()
issn = getISSNs()

title = getFirstTitle()
title_auto_str = getFirstTitle()
title_alt = getOtherTitles()
publishDate_txt_mv = publishDate = getFirstDate()
publisher = getPublishers()

topic = getSubjects()

url = getUrls();
contents = getContents();

subject_facet_facet_mv = subject_facet_str_mv = getSubjectFacet();
record_format_facet_mv = cpk_detected_format_facet_str_mv = getFormat()
