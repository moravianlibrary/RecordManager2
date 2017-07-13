package cz.mzk.recordmanager.server.export;

public enum IOFormat {
	LINE_MARC, ALEPH_MARC, ISO_2709, XML_MARC, DC_XML, XML_PATENTS, OSOBNOSTI_REGIONU, SFX;
	
	public static IOFormat stringToExportFormat(String strParam) {
		if (strParam == null) {
			return XML_MARC;
		}
		if (strParam.matches("(?i)line")) {
			return LINE_MARC;
		}
		if (strParam.matches("(?i)aleph")) {
			return ALEPH_MARC;
		}
		if (strParam.matches("(?i)iso")) {
			return ISO_2709;
		}
		if (strParam.matches("(?i)dcxml")) {
			return DC_XML;
		}
		if (strParam.matches("(?i)patents")) {
			return IOFormat.XML_PATENTS;
		}
		if (strParam.matches("(?i)osobnosti")) {
			return IOFormat.OSOBNOSTI_REGIONU;
		}
		if (strParam.matches("(?i)sfx")) {
			return IOFormat.SFX;
		}
		return XML_MARC;
	}
}
