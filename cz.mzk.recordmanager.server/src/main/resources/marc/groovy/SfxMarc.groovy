recordtype = "sfx"

fullrecord = getFullrecord()

author_txt_mv = getFields "100a:700a"

sfx_id_txt = getId001()
sfx_source_txt = get003()
sfx_title_txt_mv = getFields "245a:246a"
isbn = getFields "020a"
issn = getFields "022a"
embargo_str = getFields "500a"
publishDate_txt_mv = getPublishDate()
volume_txt_mv = getFields "996v"
sfx_url_txt = getSfxUrl()
