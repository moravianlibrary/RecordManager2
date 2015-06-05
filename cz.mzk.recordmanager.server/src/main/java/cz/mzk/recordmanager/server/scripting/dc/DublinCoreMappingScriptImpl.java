package cz.mzk.recordmanager.server.scripting.dc;

import groovy.lang.Binding;
import groovy.util.DelegatingScript;

import java.util.List;
import java.util.Map;

import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.scripting.MappingResolver;
import cz.mzk.recordmanager.server.scripting.function.RecordFunction;

public class DublinCoreMappingScriptImpl implements DublinCoreMappingScript {

	private final Binding binding;

	private final List<DelegatingScript> scripts;

	private final MappingResolver propertyResolver;

	private final Map<String, RecordFunction<DublinCoreRecord>> functions;

	public DublinCoreMappingScriptImpl(Binding binding, List<DelegatingScript> scripts, 
			MappingResolver propertyResolver, Map<String, RecordFunction<DublinCoreRecord>> functions) {
		super();
		this.scripts = scripts;
		this.binding = binding;
		this.propertyResolver = propertyResolver;
		this.functions = functions;
	}

	@Override
	public Map<String, Object> parse(DublinCoreRecord record) {
		binding.getVariables().clear();
		DublinCoreDSL delegate = new DublinCoreDSL(record, propertyResolver, functions);
		for (DelegatingScript script : scripts) {
			script.setDelegate(delegate);
			script.run();
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> entries = (Map<String, Object>) binding
				.getVariables();
		return entries;
	}

}
