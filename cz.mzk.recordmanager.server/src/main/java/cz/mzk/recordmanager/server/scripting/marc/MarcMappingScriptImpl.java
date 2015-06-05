package cz.mzk.recordmanager.server.scripting.marc;

import groovy.lang.Binding;
import groovy.util.DelegatingScript;

import java.util.List;
import java.util.Map;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.scripting.MappingResolver;
import cz.mzk.recordmanager.server.scripting.marc.function.MarcRecordFunction;

public class MarcMappingScriptImpl implements MarcMappingScript {

	private final Binding binding;
	
	private final List<DelegatingScript> scripts;
	
	private final MappingResolver propertyResolver;

	private final Map<String, MarcRecordFunction> functions;
	
	public MarcMappingScriptImpl(Binding binding, List<DelegatingScript> scripts, 
			MappingResolver propertyResolver, Map<String, MarcRecordFunction> functions) {
		super();
		this.scripts = scripts;
		this.binding = binding;
		this.propertyResolver = propertyResolver;
		this.functions = functions;
	}

	@Override
	public Map<String, Object> parse(MarcRecord record) {
		binding.getVariables().clear();
		MarcDSL delegate = new MarcDSL(record, propertyResolver, functions);
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
