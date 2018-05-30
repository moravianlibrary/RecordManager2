recordtype = "dublinCore"
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
publishDate = getFirstDate()
publishDate_int_mv = getPublishDateForTimeline()
publisher = getPublishers()

topic = getSubjects()

url = getUrls();
contents = getContents();

subject_facet_str_mv = subject_str_mv = filter("subject_facet.txt", getSubjectFacet())
cpk_detected_format_facet_str_mv = getFormat()
