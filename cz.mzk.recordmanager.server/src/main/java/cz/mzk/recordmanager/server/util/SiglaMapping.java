package cz.mzk.recordmanager.server.util;

import cz.mzk.recordmanager.server.ClasspathResourceProvider;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.model.Sigla;
import cz.mzk.recordmanager.server.scripting.ResourceMappingResolver;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SiglaMapping {

	private static final Map<Long, String> MAPPING = new HashMap<>();
	private static final String SIGLA_MAP = "item_id_sigla.map";

	static {
		try {
			new ResourceMappingResolver(new ClasspathResourceProvider()).resolve(SIGLA_MAP).getMapping()
					.forEach((key, value) -> MAPPING.put(Long.parseLong(key), value.get(0)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getSigla(ImportConfiguration conf) {
		if (MAPPING.containsKey(conf.getId())) return MAPPING.get(conf.getId());
		List<Sigla> siglas;
		if (!(siglas = conf.getSiglas()).isEmpty()) {
			MAPPING.put(conf.getId(), siglas.get(0).getUniqueId().getSigla());
			return siglas.get(0).getUniqueId().getSigla();
		}
		return null;
	}

}
