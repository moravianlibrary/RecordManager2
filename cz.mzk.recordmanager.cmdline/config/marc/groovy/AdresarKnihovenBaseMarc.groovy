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
phone_display_mv = getFieldsForAdresar "TELa", SubfieldExtractionMethod.SEPARATED, null
fax_display_mv = getFieldsForAdresar "FAXa", SubfieldExtractionMethod.SEPARATED, null
note2_display = getFirstFieldForAdresar "POUa"
branchurl_display = getFirstFieldForAdresar "POBu"
branch_display = getFirstFieldSeparatedForAdresar "POBnamg", " | "
directory_display = getFirstFieldSeparatedForAdresar "ADKuz", " | "
url_display_mv = getFieldsForAdresar "URLuz", SubfieldExtractionMethod.JOINED, " | "
function_display_mv = translate("adresar_function.map", getFieldsForAdresar("FCEa", SubfieldExtractionMethod.SEPARATED, null), null)
services_display_mv = translate("adresar_services.map", getFieldsForAdresar("SLUa", SubfieldExtractionMethod.SEPARATED, null), null)
projects_display_mv = translate("adresar_projects.map", getFieldsForAdresar("PRKa", SubfieldExtractionMethod.SEPARATED, null), null)
responsibility_display_mv = adresarGetResponsibility()
address_display_mv = adresarGetAddress()
email_display_mv = adresarGetEmailOrMvs "EMLuz"
mvs_display_mv = adresarGetEmailOrMvs "MVScu"
reg_lib_display_mv = getFieldsForAdresar "PVKsn", SubfieldExtractionMethod.JOINED, " | "

town_search_txt = getFirstFieldForAdresar "MESa"
region_search_txt = getFirstFieldForAdresar "KRJa"
district_search_txt = getFirstFieldForAdresar "KRJb"
function_search_txt_mv = translate("adresar_function.map", getFieldsForAdresar("FCEa", SubfieldExtractionMethod.SEPARATED, null), null)
services_search_txt_mv = translate("adresar_services.map", getFieldsForAdresar("SLUa", SubfieldExtractionMethod.SEPARATED, null), null)
projects_search_txt_mv = translate("adresar_projects.map", getFieldsForAdresar("PRKa", SubfieldExtractionMethod.SEPARATED, null), null)
type_search_txt = getFirstFieldForAdresar "TYPb"
note_search_txt = getFirstFieldForAdresar "POIa"
code_search_txt = getFirstFieldForAdresar "ZKRa"
sigla_search_txt = getFirstFieldForAdresar "SGLa"
responsibility_search_txt_mv = getFieldsForAdresar "JMNkp", SubfieldExtractionMethod.JOINED, " "
note2_search_txt = getFirstFieldForAdresar "POUa"
ils_search_txt = getFirstFieldForAdresar "KNSa"
reg_lib_search_txt_mv = getFieldsForAdresar "PVKs", SubfieldExtractionMethod.JOINED, null
address_search_txt_mv = getFieldsForAdresar "ADRum", SubfieldExtractionMethod.JOINED, " "
branch_search_txt = getFirstFieldSeparatedForAdresar "POBna", " "

region_disctrict_facet_str_mv = adresarGetRegionDistrictFacet()
function_facet_str_mv = translate("adresar_function.map", getFieldsForAdresar("FCEa", SubfieldExtractionMethod.SEPARATED, null), null)
services_facet_str_mv = translate("adresar_services.map", getFieldsForAdresar("SLUa", SubfieldExtractionMethod.SEPARATED, null), null)
projects_facet_str_mv = translate("adresar_projects.map", getFieldsForAdresar("PRKa", SubfieldExtractionMethod.SEPARATED, null), null)
type_facet_str = getFirstFieldForAdresar "TYPb"
ils_facet_str = getFirstFieldForAdresar "KNSa"
