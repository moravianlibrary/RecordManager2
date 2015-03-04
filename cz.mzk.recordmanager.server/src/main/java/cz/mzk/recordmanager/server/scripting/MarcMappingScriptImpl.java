package cz.mzk.recordmanager.server.scripting;

import groovy.lang.Binding;
import groovy.util.DelegatingScript;

import java.util.List;
import java.util.Map;

import cz.mzk.recordmanager.server.marc.MarcRecord;

public class MarcMappingScriptImpl implements MarcMappingScript {

	private final Binding binding;
	
	private final List<DelegatingScript> scripts;
	
	private final MappingResolver propertyResolver;
	
	public MarcMappingScriptImpl(Binding binding, List<DelegatingScript> scripts, MappingResolver propertyResolver) {
		super();
		this.scripts = scripts;
		this.binding = binding;
		this.propertyResolver = propertyResolver;
	}

	@Override
	public Map<String, Object> parse(MarcRecord record) {
		binding.getVariables().clear();
		MarcDSL delegate = new MarcDSL(record, propertyResolver);
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
