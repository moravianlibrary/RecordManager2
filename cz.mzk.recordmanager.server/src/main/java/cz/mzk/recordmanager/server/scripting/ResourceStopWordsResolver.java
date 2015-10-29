package cz.mzk.recordmanager.server.scripting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import cz.mzk.recordmanager.server.ResourceProvider;

public class ResourceStopWordsResolver implements StopWordsResolver {

	private final String root = "/stopwords/";

	private final ResourceProvider provider;

	public ResourceStopWordsResolver(ResourceProvider provider) {
		super();
		this.provider = provider;
	}

	@Override
	public Set<String> resolve(String file) throws IOException {
		String path = root + file;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(provider.getResource(path), StandardCharsets.UTF_8))) {
			return reader.lines().collect(Collectors.toCollection(HashSet::new));
		}
	}

}
