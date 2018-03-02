import cz.mzk.recordmanager.server.marc.SubfieldExtractionMethod;

recordtype = "library"
fullrecord = getFullrecord()
lastupdated_str = getFirstFieldForAdresar "AKTa"

sigla_display = getFirstFieldForAdresar "SGLa"
name_display = getFirstFieldSeparatedForAdresar "NAZabc", " - "
code_display = getFirstFieldForAdresar "ZKRa"
note_display = getFirstFieldForAdresar "POIa"
ico_display = getFirstFieldSeparatedForAdresar "ICOab", " - "
emk_display = getFirstFieldForAdresar "EMKa"
type_display_mv = getFieldsForAdresar "TYPb", SubfieldExtractionMethod.SEPARATED, null
phone_display_mv = getFieldsForAdresar "TELa", SubfieldExtractionMethod.SEPARATED, null
fax_display_mv = getFieldsForAdresar "FAXa", SubfieldExtractionMethod.SEPARATED, null
note2_display = getFirstFieldForAdresar "POUa"
branchurl_display_mv = getFieldsForAdresar "POBu", SubfieldExtractionMethod.SEPARATED, null
branch_display_mv = getFieldsForAdresar "POBnamg", SubfieldExtractionMethod.JOINED, " | "
url_display_mv = adresarGetUrlDisplay()
function_display_mv = getFieldsForAdresar("FCEa", SubfieldExtractionMethod.SEPARATED, null)
services_display_mv = getFieldsForAdresar("SLUa", SubfieldExtractionMethod.SEPARATED, null)
projects_display_mv = getFieldsForAdresar("PRKa", SubfieldExtractionMethod.SEPARATED, null)
responsibility_display_mv = adresarGetResponsibility()
address_display_mv = adresarGetAddress()
email_display_mv = adresarGetEmailOrMvs "EMLuz"
mvs_display_mv = adresarGetEmailOrMvs "MVScu"
reg_lib_display_mv = getFieldsForAdresar "PVKsn", SubfieldExtractionMethod.JOINED, " | "
name_alt_display_mv = adresarGetNameAlt " - "
hours_display = adresarGetHours()
region_display = translate("adresar_region.map", getFirstFieldForAdresar("KRJa"), null)
district_display = getFirstFieldForAdresar "KRJb"

allLibraryFields_txt_mv = getAllLibraryFields()
town_search_txt = getFirstFieldForAdresar "MESa"
region_search_txt = translate("adresar_region.map", getFirstFieldForAdresar("KRJa"), null)
district_search_txt = getFirstFieldForAdresar "KRJb"
function_search_txt_mv = getFieldsForAdresar("FCEa", SubfieldExtractionMethod.SEPARATED, null)
services_search_txt_mv = getFieldsForAdresar("SLUa", SubfieldExtractionMethod.SEPARATED, null)
projects_search_txt_mv = getFieldsForAdresar("PRKa", SubfieldExtractionMethod.SEPARATED, null)
type_search_txt_mv = getFieldsForAdresar "TYPb", SubfieldExtractionMethod.SEPARATED, null
note_search_txt = getFirstFieldForAdresar "POIa"
code_search_txt = getFirstFieldForAdresar "ZKRa"
sigla_search_txt = getFirstFieldForAdresar "SGLa"
responsibility_search_txt_mv = getFieldsForAdresar "JMNkp", SubfieldExtractionMethod.JOINED, " "
note2_search_txt = getFirstFieldForAdresar "POUa"
ils_search_txt = getFirstFieldForAdresar "KNSa"
reg_lib_search_txt_mv = getFieldsForAdresar "PVKs", SubfieldExtractionMethod.JOINED, null
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
ils_facet_str = getFirstFieldForAdresar "KNSa"

gps_str = adresarGetGps()
library_relevance_str = getLibraryRelevance()
portal_facet_str = getPortalFacet()
region_disctrict_town_str_mv = adresarGetRegionDistrictTown()

// autocomplete
town_str = town_autocomplete = getFirstFieldForAdresar "MESa"
name_str = name_autocomplete = getFirstFieldSeparatedForAdresar "NAZabc", " "
