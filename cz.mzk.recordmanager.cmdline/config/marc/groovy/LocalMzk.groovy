local_base_facet_mv = getMZKBases()
local_view_statuses_facet_mv = getMZKStatuses true
local_acq_date = getMZKAcquisitionDateStamp()

local_callnumber_str_mv = getFieldsUnique "996c"
callnumber_second_str_mv = getFields("996h").collect{it -> it.replace(' ', '|')}
callnumber_search = getFieldsUnique "996c"
local_location_txt_mv = getFieldsUnique "996h"
local_barcode_txt_mv = getFieldsUnique "996b"
previous_owner_txt_mv = getFieldsUnique "981ab:982ab:983ab"

// sort
author_sort_cz = getAuthorForSorting("100abcd:110abcd:111abcd:700abcd:710abcd:711abcd")
title_sort_cz = getSortableTitle()
publishDate_sort = publishDateSort = getPublishDateForSorting()
