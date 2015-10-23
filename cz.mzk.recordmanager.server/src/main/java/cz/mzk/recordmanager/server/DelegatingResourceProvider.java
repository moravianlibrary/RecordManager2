package cz.mzk.recordmanager.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class DelegatingResourceProvider implements ResourceProvider {

	private final List<ResourceProvider> delegates;

	public DelegatingResourceProvider(ResourceProvider... providers) {
		this.delegates = Arrays.asList(providers);
	}

	@Override
	public InputStream getResource(String resourcePath) throws IOException {
		for (ResourceProvider delegate : delegates) {
			InputStream resource = delegate.getResource(resourcePath);
			if (resource != null) {
				return resource;
			}
		}
		throw new IllegalArgumentException(String.format("Resource %s not found", resourcePath));
	}

}
