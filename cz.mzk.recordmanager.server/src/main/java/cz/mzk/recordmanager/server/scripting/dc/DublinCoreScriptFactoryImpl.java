package cz.mzk.recordmanager.server.scripting.dc;

import cz.mzk.recordmanager.server.scripting.*;
import groovy.lang.Binding;
import groovy.util.DelegatingScript;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.scripting.dc.function.DublinCoreRecordFunctions;
import cz.mzk.recordmanager.server.scripting.function.RecordFunction;
import cz.mzk.recordmanager.server.scripting.function.RecordFunctionsFactory;

@Component
public class DublinCoreScriptFactoryImpl extends AbstractScriptFactory<DublinCoreFunctionContext>
		implements DublinCoreScriptFactory, InitializingBean {

	@Autowired
	private RecordFunctionsFactory functionsFactory;

	@Autowired
	private MappingResolver propertyResolver;

	@Autowired
	private StopWordsResolver stopWordsResolver;

	@Autowired
	private ListResolver listResolver;

	@Autowired(required = false)
	private List<DublinCoreRecordFunctions> functionsList;

	private Map<String, RecordFunction<DublinCoreFunctionContext>> functions = new HashMap<>();

	@Override
	public MappingScript<DublinCoreFunctionContext> create(InputStream... scriptsSource) {
		return super.create(scriptsSource);
	}

	@Override
	protected MappingScript<DublinCoreFunctionContext> create(final Binding binding,
			final List<DelegatingScript> scripts) {
		return new DublinCoreMappingScriptImpl(binding, scripts, propertyResolver,
				stopWordsResolver, listResolver, functions);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		functions = functionsFactory.create(DublinCoreFunctionContext.class, functionsList);
	}

}
