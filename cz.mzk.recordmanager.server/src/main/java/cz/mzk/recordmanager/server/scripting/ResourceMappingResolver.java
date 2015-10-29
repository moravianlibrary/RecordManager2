package cz.mzk.recordmanager.server.scripting;

import java.io.IOException;
import java.io.InputStream;

import cz.mzk.recordmanager.server.ResourceProvider;

public class ResourceMappingResolver implements MappingResolver {

	private final String root = "/mapping/";

	private final ResourceProvider provider;

	public ResourceMappingResolver(ResourceProvider provider) {
		super();
		this.provider = provider;
	}

	@Override
	public Mapping resolve(String file) throws IOException {
		String path = root + file;
		InputStream is = provider.getResource(path);
		if (is == null) {
			throw new IllegalArgumentException(
					String.format("Mapping '%s' not found", path));
		}
		Mapping mapping = new Mapping(is);
		return mapping;
	}

}
