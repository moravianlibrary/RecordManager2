package cz.mzk.recordmanager.server.scripting.dc;

import java.io.IOException;
import java.util.Map;

import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.scripting.Mapping;
import cz.mzk.recordmanager.server.scripting.MappingResolver;
import cz.mzk.recordmanager.server.scripting.dc.function.DublinCoreRecordFunction;

public class DublinCoreDSL {
	
	private final DublinCoreRecord record;
	
	private final MappingResolver propertyResolver;
	
	private final Map<String, DublinCoreRecordFunction> functions;

	public DublinCoreDSL(DublinCoreRecord record,
			MappingResolver propertyResolver,
			Map<String, DublinCoreRecordFunction> functions) {
		super();
		this.record = record;
		this.propertyResolver = propertyResolver;
		this.functions = functions;
	}
	
	public String getFirstTitle() {
		return record.getFirstTitle();
	}
	
	public String translate(String file, String input, String defaultValue)
			throws IOException {
		Mapping mapping = propertyResolver.resolve(file);
		String result = (String) mapping.get(input);
		if (result == null) {
			result = defaultValue;
		}
		return result;
	}
	
	public Object methodMissing(String methodName, Object args) {
		DublinCoreRecordFunction func = functions.get(methodName);
		if (func == null) {
			throw new IllegalArgumentException(String.format("missing function: %s", methodName));
		}
		return func.apply(record, args);
	}

}
