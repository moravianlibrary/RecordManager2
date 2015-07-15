package cz.mzk.recordmanager.server.scripting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Abstract class for DSL (domain specific language) for description of
 * mapping between metadata records and Solr fields. Example of mapping:
 * 
 * language = translate("mzk_language.map", getLanguages(), null)
 * country_txt = translate("mzk_country.map", getCountry(), null)
 * 
 * 
 * @author xrosecky
 *
 */
public abstract class BaseDSL {

	private final MappingResolver propertyResolver;

	public BaseDSL(MappingResolver propertyResolver) {
		super();
		this.propertyResolver = propertyResolver;
	}

	public String translate(String file, String input, String defaultValue)
			throws IOException {
		Mapping mapping = propertyResolver.resolve(file);
		String result = (String) mapping.get(input);
		if (result == null) {
			result = defaultValue;
		}
		return result;
	}

	public List<String> translate(String file, List<String> inputs,
			String defaultValue) throws IOException {
		List<String> translated = new ArrayList<String>();
		Mapping mapping = propertyResolver.resolve(file);
		for (String input : inputs) {
			String result = (String) mapping.get(input);
			if (result == null) {
				result = defaultValue;
			}
			if (result != null) {
				translated.add(result);
			}
		}
		return translated;
	}

}
