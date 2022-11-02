package cz.mzk.recordmanager.server.kramerius;

import cz.mzk.recordmanager.server.scripting.Mapping;
import cz.mzk.recordmanager.server.scripting.MappingResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ApiMappingFactory {

	@Autowired
	private MappingResolver propertyResolver;

	public Mapping getMapping(String version) {
		try {
			return propertyResolver.resolve("kramerius/api/" + version + ".map");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
