package cz.mzk.recordmanager.server.kramerius.harvest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class KrameriusFormatResolverImpl implements KrameriusFormatResolver {

	private static Map<String, String> formats = new HashMap<>();

	static {
		formats.put("DC", "dublinCore");
		formats.put("BIBLIO_MODS", "marc21-xml"); // for possible future use of MODS
	}

	@Override
	public String resolve(String metadataStream) {
		String format = formats.get(metadataStream);
		if (format == null) {
			throw new IllegalArgumentException(String.format("Format for metadata stream %s not found", metadataStream));
		}
		return format;
	}

}
