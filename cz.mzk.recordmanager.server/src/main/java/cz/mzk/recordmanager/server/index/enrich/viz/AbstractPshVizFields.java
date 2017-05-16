package cz.mzk.recordmanager.server.index.enrich.viz;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.scripting.Mapping;
import cz.mzk.recordmanager.server.scripting.MappingResolver;

public abstract class AbstractPshVizFields extends AbstractVizFields implements
		InitializingBean {

	protected static final Pattern SPLITTER = Pattern.compile("\\|");

	@Autowired
	private MappingResolver propertyResolver;

	private static Mapping mapping = null;

	private static final String PSH_MAP = "tezaurus_psh.map";

	@Override
	protected List<String> getEnrichingValues(String key, String enrichingField) {
		return mapping.get(key);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		mapping = propertyResolver.resolve(PSH_MAP);
	}
}
