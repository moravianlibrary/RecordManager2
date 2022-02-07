import cz.mzk.recordmanager.server.marc.SubfieldExtractionMethod;

record_format = "library"

fullrecord = getFullrecord()
library_relevance_str = getLibraryRelevance()

// view
view_txt_mv = getViewType()

// display
address_display_mv = adresarGetAddress()
branch_display_mv = getFieldsForAdresar "POBnamg", SubfieldExtractionMethod.JOINED, " | "
branchurl_display_mv = getFieldsForAdresar "POBu", SubfieldExtractionMethod.SEPARATED, null
cpk_code_display = adresarGetCpkCode()
district_display = getFirstFieldForAdresar "KRJb"
email_display_mv = adresarGetEmailOrMvs "EMLuz"
function_display_mv = getFieldsForAdresar("FCEa", SubfieldExtractionMethod.SEPARATED, null)
gps_display = adresarGetGps()
hours_display = adresarGetHours()
ico_display = getFirstFieldSeparatedForAdresar "ICOab", " - "
lastupdated_display = adresarGetLastUpdated()
mvs_display_mv = adresarGetEmailOrMvs "MVScu"
name_alt_display_mv = adresarGetNameAlt " - "
name_display = getFirstFieldSeparatedForAdresar "NAZabcd", " - "
note2_display = getFirstFieldForAdresar "POUa"
note_display = getFirstFieldForAdresar "POIa"
phone_display_mv = getFieldsForAdresar "TELa", SubfieldExtractionMethod.SEPARATED, null
projects_display_mv = getFieldsForAdresar("PRKa", SubfieldExtractionMethod.SEPARATED, null)
reg_lib_display_mv = getFieldsForAdresar "PVKsn", SubfieldExtractionMethod.JOINED, " | "
reg_lib_id_display_mv = getRegionalLibrary()
region_display = getFirstFieldForAdresar "KRJa"
responsibility_display_mv = adresarGetResponsibility()
services_display_mv = getFieldsForAdresar("SLUa", SubfieldExtractionMethod.SEPARATED, null)
sigla_display = getFirstFieldForAdresar "SGLa"
statutory_authority_display = getFirstFieldForAdresar "PVZn"
statutory_authority_url_display = getFirstFieldForAdresar "PVZu"
town_display = getFirstFieldForAdresar "MESa"
type_display_mv = getFieldsForAdresar "TYPb", SubfieldExtractionMethod.SEPARATED, null
url_display_mv = adresarGetUrlDisplay()
