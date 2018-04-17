package cz.mzk.recordmanager.server.scripting;

import cz.mzk.recordmanager.server.ResourceProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ResourceListResolver implements ListResolver {

	private final String root = "/list/";

	private final ResourceProvider provider;

	public ResourceListResolver(ResourceProvider provider) {
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
