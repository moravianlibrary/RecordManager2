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

// remove later
gps_str = adresarGetGps()
lastupdated_str = getFirstFieldForAdresar "AKTa"
allLibraryFields_txt_mv = getAllLibraryFields()
town_search_txt = getFirstFieldForAdresar "MESa"
region_search_txt = translate("adresar_region.map", getFirstFieldForAdresar("KRJa"), null)
function_search_txt_mv = getFieldsForAdresar("FCEa", SubfieldExtractionMethod.SEPARATED, null)
services_search_txt_mv = getFieldsForAdresar("SLUa", SubfieldExtractionMethod.SEPARATED, null)
projects_search_txt_mv = getFieldsForAdresar("PRKa", SubfieldExtractionMethod.SEPARATED, null)
type_search_txt_mv = getFieldsForAdresar "TYPb", SubfieldExtractionMethod.SEPARATED, null
note_search_txt = getFirstFieldForAdresar "POIa"
code_search_txt = getFirstFieldForAdresar "ZKRa"
sigla_search_txt = getFirstFieldForAdresar "SGLa"
responsibility_search_txt_mv = getFieldsForAdresar "JMNkp", SubfieldExtractionMethod.JOINED, " "
note2_search_txt = getFirstFieldForAdresar "POUa"
address_search_txt_mv = getFieldsForAdresar "ADRum", SubfieldExtractionMethod.JOINED, " "
branch_search_txt_mv = getFieldsForAdresar "POBna", SubfieldExtractionMethod.JOINED, " "
name_search_txt = getFirstFieldSeparatedForAdresar "NAZabc", " "
name_alt_search_txt_mv = adresarGetNameAlt " "
cpk_code_search_txt = adresarGetCpkCode()
region_disctrict_facet_str_mv = adresarGetRegionDistrictFacet()
function_facet_str_mv = getFieldsForAdresar("FCEa", SubfieldExtractionMethod.SEPARATED, null)
services_facet_str_mv = getFieldsForAdresar("SLUa", SubfieldExtractionMethod.SEPARATED, null)
projects_facet_str_mv = getFieldsForAdresar("PRKa", SubfieldExtractionMethod.SEPARATED, null)
type_facet_str_mv = getFieldsForAdresar "TYPb", SubfieldExtractionMethod.SEPARATED, null
library_relevance_str = getLibraryRelevance()
portal_facet_str = getPortalFacet()
