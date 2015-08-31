package cz.mzk.recordmanager.server.oai.harvest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class OAIFormatResolverImpl implements OAIFormatResolver {

	private static Map<String, String> formats = new HashMap<String, String>();
	
	{
		formats.put("marc21", "marc21-xml");
		formats.put("xml-marc", "marc21-xml");
		formats.put("marccpk", "marc21-xml");
		formats.put("oai_dc", "dublinCore");
	}
	
	
	@Override
	public String resolve(String metadataPrefix) {
		return formats.get(metadataPrefix);
	}

}
