package cz.mzk.recordmanager.server.kramerius.harvest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class KrameriusFormatResolverImpl implements KrameriusFormatResolver {

	private static Map<String, String> formats = new HashMap<String, String>();

	{
		formats.put("DC", "dublinCore");
		formats.put("BIBLIO_MODS", "MODS"); // for possible future use of MODS
	}

	@Override
	public String resolve(String metadataStream) {
		return formats.get(metadataStream);
	}

}
