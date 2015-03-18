package cz.mzk.recordmanager.server.export;

public enum ExportFormat {
	LINE_MARC, ALEPH_MARC, ISO_2709, XML_MARC;
	
	public static ExportFormat stringToExportFormat(String strParam) {
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
		return XML_MARC;
	}
}
