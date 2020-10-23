package cz.mzk.recordmanager.server.scripting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * Map for translation of values before indexing to Solr.
 * 
 * @see cz.mzk.recordmanager.server.scripting.MappingResolver
 * 
 * @author xrosecky
 * 
 */
public class Mapping {

	private final Map<String, List<String>> mapping;

	public Mapping(InputStream is) throws IOException {
		Map<String, List<String>> map = new HashMap<String, List<String>>(); 
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] splitted = line.split("=", 2);
			if (splitted.length == 2) {
				String key = trim(splitted[0]);
				String val = trim(splitted[1]);
				List<String> results = new ArrayList<>();
				List<String> values = map.get(key);
				if (values != null) results = new ArrayList<>(values);
				results.add(val);
				map.put(key, Collections.unmodifiableList(results));
			}
		}
		this.mapping = Collections.unmodifiableMap(map);
	}

	/**
	 * Translate the given key to corresponding value.
	 * 
	 * 
	 */
	public List<String> get(String key) {
		return mapping.get(key);
	}

	private String trim(String string) {
		string = string.trim();
		if (string.startsWith("\"") && string.endsWith("\"")) {
			string = string.substring(1, string.length() - 1);
		}
		return string;
	}

	public Map<String, List<String>> getMapping() {
		return mapping;
	}
}
