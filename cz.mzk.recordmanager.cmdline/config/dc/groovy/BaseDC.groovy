recordtype = "dublincore"
record_format = "dublincore"

fullrecord = getFullRecord()

allfields = getAllFields()

author = getFirstCreator()
author2 = getOtherCreators()
author_find = getAuthorFind();

isbn = getISBNs()
issn = getISSNs()

title = getFirstTitle()

title_auto_str = getFirstTitle()
title_alt = getOtherTitles()
publishDate_txt_mv = publishDate = getFirstDate()
publishDate_int_mv = getPublishDateForTimeline()
publisher = getPublishers()

topic = getSubjects()

url = getUrls();
contents = getContents();

// facets
record_format_facet_mv = cpk_detected_format_facet_str_mv = getFormat()
subject_facet_mv = subject_facet_str_mv = subject_str_mv = filter("subject_facet.txt", getSubjectFacet())

// sort
title_sort_cz = getTitleForSorting()
author_sort_cz = getAuthorForSorting()
