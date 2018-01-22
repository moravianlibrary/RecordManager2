package cz.mzk.recordmanager.server.oai.harvest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class OAIFormatResolverImpl implements OAIFormatResolver {

	private static Map<String, String> formats = new HashMap<>();

	static {
		formats.put("marc21", "marc21-xml");
		formats.put("xml-marc", "marc21-xml");
		formats.put("marccpk", "marc21-xml");
		formats.put("oai_marcxml_cpk", "marc21-xml");
		formats.put("marc21e", "marc21-xml");
		formats.put("oai_dc", "dublinCore");
		formats.put("ese", "ese");
	}


	@Override
	public String resolve(String metadataPrefix) {
		return formats.get(metadataPrefix);
	}

}
