package cz.mzk.recordmanager.server.export;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public enum IOFormat {
	LINE_MARC("line"),
	ALEPH_MARC("aleph"),
	ISO_2709("iso"),
	XML_MARC("xml"),
	DC_XML("dcxml"),
	XML_PATENTS("patents"),
	OSOBNOSTI_REGIONU("osobnosti"),
	SFX("sfx"),
	SFX_NLK("sfxnlk"),
	MUNIPRESS("munipress"),
	PALMKNIHY("palmknihy");

	private static final Map<IOFormat, Pattern> FORMAT_PATTERNS = Arrays.stream(IOFormat.values()).collect(
			Collectors.toMap(
					f -> f,
					f -> Pattern.compile(f.format, Pattern.CASE_INSENSITIVE)
			)
	);
	private String format;

	IOFormat(String format) {
		this.format = format;
	}

	public static IOFormat stringToExportFormat(String strParam) {
		if (strParam == null) {
			return XML_MARC;
		}
		for (Map.Entry<IOFormat, Pattern> entry : FORMAT_PATTERNS.entrySet()) {
			if (entry.getValue().matcher(strParam).matches()) return entry.getKey();
		}
		return XML_MARC;
	}

	public static List<String> getStringifyFormats() {
		return Arrays.stream(IOFormat.values()).map(f -> f.format).collect(Collectors.toList());
	}
}
