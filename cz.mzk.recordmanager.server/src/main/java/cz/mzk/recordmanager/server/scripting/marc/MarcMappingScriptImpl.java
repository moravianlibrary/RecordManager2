package cz.mzk.recordmanager.server.scripting.marc;

import cz.mzk.recordmanager.server.scripting.ListResolver;
import groovy.lang.Binding;
import groovy.util.DelegatingScript;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.mzk.recordmanager.server.scripting.MappingResolver;
import cz.mzk.recordmanager.server.scripting.MappingScript;
import cz.mzk.recordmanager.server.scripting.StopWordsResolver;
import cz.mzk.recordmanager.server.scripting.function.RecordFunction;

public class MarcMappingScriptImpl implements MappingScript<MarcFunctionContext> {

	private final Binding binding;
	
	private final List<DelegatingScript> scripts;
	
	private final MappingResolver propertyResolver;

	private final StopWordsResolver stopWordsResolver;

	private final ListResolver listResolver;

	private final Map<String, RecordFunction<MarcFunctionContext>> functions;

	public MarcMappingScriptImpl(Binding binding, List<DelegatingScript> scripts,
			MappingResolver propertyResolver, StopWordsResolver stopWordsResolver, ListResolver listResolver,
			Map<String, RecordFunction<MarcFunctionContext>> functions) {
		super();
		this.scripts = scripts;
		this.binding = binding;
		this.propertyResolver = propertyResolver;
		this.stopWordsResolver = stopWordsResolver;
		this.listResolver = listResolver;
		this.functions = functions;
	}

	@Override
	public Map<String, Object> parse(MarcFunctionContext ctx) {
		binding.getVariables().clear();
		MarcDSL delegate = new MarcDSL(ctx, propertyResolver, stopWordsResolver, listResolver, functions);
		for (DelegatingScript script : scripts) {
			script.setDelegate(delegate);
			script.run();
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> entries = (Map<String, Object>) binding
				.getVariables();
		Map<String, Object> copy = new HashMap<>(entries);
		return copy;
	}
	
}
