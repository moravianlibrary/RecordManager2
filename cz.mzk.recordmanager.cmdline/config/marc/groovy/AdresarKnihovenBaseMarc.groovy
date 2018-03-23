import cz.mzk.recordmanager.server.marc.SubfieldExtractionMethod;

recordtype = "library"
fullrecord = getFullrecord()
library_relevance_str = getLibraryRelevance()

// search
address_search_txt_mv = getFieldsForAdresar "ADRum", SubfieldExtractionMethod.JOINED, " "
branch_search_txt_mv = getFieldsForAdresar "POBna", SubfieldExtractionMethod.JOINED, " "
code_search_txt = getFirstFieldForAdresar "ZKRa"
cpk_code_search_txt = adresarGetCpkCode()
function_search_txt_mv = getFieldsForAdresar("FCEa", SubfieldExtractionMethod.SEPARATED, null)
ils_search_txt = getFirstFieldForAdresar "KNSa"
name_alt_search_txt_mv = adresarGetNameAlt " "
name_search_txt = getFirstFieldSeparatedForAdresar "NAZabc", " "
note2_search_txt = getFirstFieldForAdresar "POUa"
note_search_txt = getFirstFieldForAdresar "POIa"
projects_search_txt_mv = getFieldsForAdresar("PRKa", SubfieldExtractionMethod.SEPARATED, null)
region_search_txt = translate("adresar_region.map", getFirstFieldForAdresar("KRJa"), null)
responsibility_search_txt_mv = getFieldsForAdresar "JMNkp", SubfieldExtractionMethod.JOINED, " "
services_search_txt_mv = getFieldsForAdresar("SLUa", SubfieldExtractionMethod.SEPARATED, null)
sigla_search_txt = getFirstFieldForAdresar "SGLa"
town_search_txt = getFirstFieldForAdresar "MESa"
type_search_txt_mv = getFieldsForAdresar "TYPb", SubfieldExtractionMethod.SEPARATED, null

// facets
function_facet_str_mv = getFieldsForAdresar("FCEa", SubfieldExtractionMethod.SEPARATED, null)
portal_facet_str = getPortalFacet()
projects_facet_str_mv = getFieldsForAdresar("PRKa", SubfieldExtractionMethod.SEPARATED, null)
region_disctrict_facet_str_mv = adresarGetRegionDistrictFacet()
services_facet_str_mv = getFieldsForAdresar("SLUa", SubfieldExtractionMethod.SEPARATED, null)
type_facet_str_mv = getFieldsForAdresar "TYPb", SubfieldExtractionMethod.SEPARATED, null

// morelikethis
region_disctrict_town_str_mv = adresarGetRegionDistrictTown()

// autocomplete
name_str = name_autocomplete = getFirstFieldSeparatedForAdresar "NAZabc", " "
town_str = town_autocomplete = getFirstFieldForAdresar "MESa"

// map
address_map_display_mv = getFieldsForAdresar "ADRum", SubfieldExtractionMethod.JOINED, " "
gps_display = adresarGetGps()
name_display = getFirstFieldSeparatedForAdresar "NAZabc", " - "
