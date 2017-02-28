recordtype = "adresar"

fullrecord = getFullrecord()

sigla_display = getFirstFieldForAdresar "SGLa"
name_display = getFirstFieldSeparatedForAdresar "NAZabc", " - "
code_display = getFirstFieldForAdresar "ZKRa"
note_display = getFirstFieldForAdresar "POIa"
ico_display = getFirstFieldForAdresar "ICOa"
emk_display = getFirstFieldForAdresar "EMKa"