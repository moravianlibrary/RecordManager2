package cz.mzk.recordmanager.server.scripting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Mapping {
	
	private static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");

	private final Map<String, String> mapping;
	
	public Mapping(InputStream is) throws IOException {
		Map<String, String> map = new HashMap<String, String>(); 
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, UTF_8_CHARSET));
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] splitted = line.split("=", 2);
			if (splitted.length == 2) {
				String key = trim(splitted[0]);
				String val = trim(splitted[1]);
				map.put(key, val);
			}
		}
		this.mapping = Collections.unmodifiableMap(map);
	}
	
	public String trim(String string) {
		string = string.trim();
		if (string.startsWith("\"") && string.endsWith("\"")) {
			string = string.substring(1, string.length() - 1);
		}
		return string;
	}

	public String get(String key) {
		return mapping.get(key);
	}

}
