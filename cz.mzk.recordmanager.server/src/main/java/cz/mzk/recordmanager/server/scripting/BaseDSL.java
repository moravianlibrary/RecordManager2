package cz.mzk.recordmanager.server.scripting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

	private final StopWordsResolver stopWordsResolver;

	public BaseDSL(MappingResolver propertyResolver, StopWordsResolver stopWordsResolver) {
		super();
		this.propertyResolver = propertyResolver;
		this.stopWordsResolver = stopWordsResolver;
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

	public List<String> filter(String stopWordsFile, List<String> values) throws IOException {
		Set<String> stopWords = stopWordsResolver.resolve(stopWordsFile);
		return values.stream().filter(value -> !stopWords.contains(value)).collect(Collectors.toCollection(ArrayList::new));
	}

}
