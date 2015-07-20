package cz.mzk.recordmanager.server.scripting.marc;

import groovy.lang.Binding;
import groovy.util.DelegatingScript;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.scripting.AbstractScriptFactory;
import cz.mzk.recordmanager.server.scripting.MappingResolver;
import cz.mzk.recordmanager.server.scripting.MappingScript;
import cz.mzk.recordmanager.server.scripting.function.RecordFunction;
import cz.mzk.recordmanager.server.scripting.function.RecordFunctionsFactory;
import cz.mzk.recordmanager.server.scripting.marc.function.MarcRecordFunctions;

@Component
public class MarcScriptFactoryImpl extends AbstractScriptFactory<MarcRecord>
		implements MarcScriptFactory, InitializingBean {

	@Autowired
	private RecordFunctionsFactory functionsFactory;

	@Autowired
	private MappingResolver propertyResolver;

	@Autowired
	private List<MarcRecordFunctions> functionsList;

	private Map<String, RecordFunction<MarcRecord>> functions;

	@Override
	public MappingScript<MarcRecord> create(InputStream... scriptsSource) {
		return (MappingScript<MarcRecord>) super.create(scriptsSource);
	}

	@Override
	protected MappingScript<MarcRecord> create(Binding binding,
			List<DelegatingScript> scripts) {
		return new MarcMappingScriptImpl(binding, scripts, propertyResolver,
				functions);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		functions = functionsFactory.create(MarcRecord.class, functionsList);
	}

}
