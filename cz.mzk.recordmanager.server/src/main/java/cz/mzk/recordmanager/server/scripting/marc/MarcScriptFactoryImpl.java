package cz.mzk.recordmanager.server.scripting.marc;

import cz.mzk.recordmanager.server.scripting.*;
import groovy.lang.Binding;
import groovy.util.DelegatingScript;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.scripting.function.RecordFunction;
import cz.mzk.recordmanager.server.scripting.function.RecordFunctionsFactory;
import cz.mzk.recordmanager.server.scripting.marc.function.MarcRecordFunctions;

@Component
public class MarcScriptFactoryImpl extends AbstractScriptFactory<MarcFunctionContext>
		implements MarcScriptFactory, InitializingBean {

	@Autowired
	private RecordFunctionsFactory functionsFactory;

	@Autowired
	private MappingResolver propertyResolver;

	@Autowired
	private StopWordsResolver stopWordsResolver;

	@Autowired
	private ListResolver listResolver;

	@Autowired
	private List<MarcRecordFunctions> functionsList;

	private Map<String, RecordFunction<MarcFunctionContext>> functions;

	@Override
	public MappingScript<MarcFunctionContext> create(InputStream... scriptsSource) {
		return super.create(scriptsSource);
	}

	@Override
	protected MappingScript<MarcFunctionContext> create(Binding binding,
			List<DelegatingScript> scripts) {
		return new MarcMappingScriptImpl(binding, scripts, propertyResolver,
				stopWordsResolver, listResolver, functions);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		functions = functionsFactory.create(MarcFunctionContext.class, functionsList);
	}

}
