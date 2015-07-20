package cz.mzk.recordmanager.server.scripting.dc;

import groovy.lang.Binding;
import groovy.util.DelegatingScript;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.scripting.AbstractScriptFactory;
import cz.mzk.recordmanager.server.scripting.MappingResolver;
import cz.mzk.recordmanager.server.scripting.MappingScript;
import cz.mzk.recordmanager.server.scripting.dc.function.DublinCoreRecordFunctions;
import cz.mzk.recordmanager.server.scripting.function.RecordFunction;
import cz.mzk.recordmanager.server.scripting.function.RecordFunctionsFactory;

@Component
public class DublinCoreScriptFactoryImpl extends AbstractScriptFactory<DublinCoreRecord>
		implements DublinCoreScriptFactory, InitializingBean {

	@Autowired
	private RecordFunctionsFactory functionsFactory;

	@Autowired
	private MappingResolver propertyResolver;

	@Autowired(required=false)
	private List<DublinCoreRecordFunctions> functionsList;

	private Map<String, RecordFunction<DublinCoreRecord>> functions = new HashMap<>();

	@Override
	public MappingScript<DublinCoreRecord> create(InputStream... scriptsSource) {
		return (MappingScript<DublinCoreRecord>) super.create(scriptsSource);
	}

	@Override
	protected MappingScript<DublinCoreRecord> create(final Binding binding,
			final List<DelegatingScript> scripts) {
		return new DublinCoreMappingScriptImpl(binding, scripts, propertyResolver,
				functions);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		functions = functionsFactory.create(DublinCoreRecord.class, functionsList);
	}

}
