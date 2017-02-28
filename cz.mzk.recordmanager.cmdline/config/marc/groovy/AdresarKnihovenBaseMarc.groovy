import cz.mzk.recordmanager.server.marc.SubfieldExtractionMethod;

recordtype = "adresar"

fullrecord = getFullrecord()

sigla_display = getFirstFieldForAdresar "SGLa"
name_display = getFirstFieldSeparatedForAdresar "NAZabc", " - "
code_display = getFirstFieldForAdresar "ZKRa"
note_display = getFirstFieldForAdresar "POIa"
ico_display = getFirstFieldForAdresar "ICOa"
emk_display = getFirstFieldForAdresar "EMKa"
type_display = getFirstFieldForAdresar "TYPb"
phone_display_mv = getFieldsForAdresar "TELa", SubfieldExtractionMethod.SEPARATED
fax_display_mv = getFieldsForAdresar "FAXa", SubfieldExtractionMethod.SEPARATED
note2_display = getFirstFieldForAdresar "POUa"
