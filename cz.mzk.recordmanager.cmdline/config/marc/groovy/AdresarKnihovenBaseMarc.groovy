import cz.mzk.recordmanager.server.marc.SubfieldExtractionMethod;

recordtype = "library"
fullrecord = getFullrecord()

// search
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
ils_search_txt = getFirstFieldForAdresar "KNSa"
address_search_txt_mv = getFieldsForAdresar "ADRum", SubfieldExtractionMethod.JOINED, " "
branch_search_txt_mv = getFieldsForAdresar "POBna", SubfieldExtractionMethod.JOINED, " "
name_search_txt = getFirstFieldSeparatedForAdresar "NAZabc", " "
name_alt_search_txt_mv = adresarGetNameAlt " "
cpk_code_search_txt = adresarGetCpkCode()

// facets
region_disctrict_facet_str_mv = adresarGetRegionDistrictFacet()
function_facet_str_mv = getFieldsForAdresar("FCEa", SubfieldExtractionMethod.SEPARATED, null)
services_facet_str_mv = getFieldsForAdresar("SLUa", SubfieldExtractionMethod.SEPARATED, null)
projects_facet_str_mv = getFieldsForAdresar("PRKa", SubfieldExtractionMethod.SEPARATED, null)
type_facet_str_mv = getFieldsForAdresar "TYPb", SubfieldExtractionMethod.SEPARATED, null
library_relevance_str = getLibraryRelevance()
portal_facet_str = getPortalFacet()

// morelikethis
region_disctrict_town_str_mv = adresarGetRegionDistrictTown()

// autocomplete
town_str = town_autocomplete = getFirstFieldForAdresar "MESa"
name_str = name_autocomplete = getFirstFieldSeparatedForAdresar "NAZabc", " "

// map
address_map_display_mv = getFieldsForAdresar "ADRum", SubfieldExtractionMethod.JOINED, " "
gps_display = adresarGetGps()
name_display = getFirstFieldSeparatedForAdresar "NAZabc", " - "
