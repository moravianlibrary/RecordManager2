import cz.mzk.recordmanager.server.marc.SubfieldExtractionMethod;

recordtype = "library"
fullrecord = getFullrecord()
library_relevance_str = getLibraryRelevance()

// display
address_display_mv = adresarGetAddress()
branch_display_mv = getFieldsForAdresar "POBnamg", SubfieldExtractionMethod.JOINED, " | "
branchurl_display_mv = getFieldsForAdresar "POBu", SubfieldExtractionMethod.SEPARATED, null
cpk_code_display = adresarGetCpkCode()
email_display_mv = adresarGetEmailOrMvs "EMLuz"
function_display_mv = getFieldsForAdresar("FCEa", SubfieldExtractionMethod.SEPARATED, null)
gps_display = adresarGetGps()
hours_display = adresarGetHours()
ico_display = getFirstFieldSeparatedForAdresar "ICOab", " - "
lastupdated_display = getFirstFieldForAdresar "AKTa"
mvs_display_mv = adresarGetEmailOrMvs "MVScu"
name_alt_display_mv = adresarGetNameAlt " - "
name_display = getFirstFieldSeparatedForAdresar "NAZabc", " - "
note2_display = getFirstFieldForAdresar "POUa"
note_display = getFirstFieldForAdresar "POIa"
phone_display_mv = getFieldsForAdresar "TELa", SubfieldExtractionMethod.SEPARATED, null
projects_display_mv = getFieldsForAdresar("PRKa", SubfieldExtractionMethod.SEPARATED, null)
reg_lib_display_mv = getFieldsForAdresar "PVKsn", SubfieldExtractionMethod.JOINED, " | "
responsibility_display_mv = adresarGetResponsibility()
services_display_mv = getFieldsForAdresar("SLUa", SubfieldExtractionMethod.SEPARATED, null)
sigla_display = getFirstFieldForAdresar "SGLa"
type_display_mv = getFieldsForAdresar "TYPb", SubfieldExtractionMethod.SEPARATED, null
url_display_mv = adresarGetUrlDisplay()
