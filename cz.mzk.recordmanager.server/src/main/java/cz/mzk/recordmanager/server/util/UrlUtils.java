package cz.mzk.recordmanager.server.util;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.utils.URIBuilder;

import com.google.common.base.Preconditions;

public class UrlUtils {
	
	public static String buildUrl(String baseUrl, Map<String, String> query) {
		Preconditions.checkNotNull(baseUrl, "baseUrl");
		try {
			URIBuilder uriBuilder = new URIBuilder(baseUrl);
			for (Entry<String, String> entry : query.entrySet()) {
				uriBuilder.addParameter(entry.getKey(), entry.getValue());
			}
			return uriBuilder.build().toString();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Invalid baseUrl: " + baseUrl);
		}
		
	}

}
