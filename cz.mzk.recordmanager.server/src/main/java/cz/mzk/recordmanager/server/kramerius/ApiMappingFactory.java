package cz.mzk.recordmanager.server.kramerius;

import cz.mzk.recordmanager.server.scripting.Mapping;
import cz.mzk.recordmanager.server.scripting.MappingResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class ApiMappingFactory {

	@Autowired
	private MappingResolver propertyResolver;

	public static final List<String> API_VERSION = Arrays.asList("5", "7");

	public Mapping getMapping(String version) {
		try {
			return propertyResolver.resolve("kramerius/api/" + version.charAt(0) + ".map");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
