package cz.mzk.recordmanager.server.scripting.dc;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.scripting.BaseDSL;
import cz.mzk.recordmanager.server.scripting.MappingResolver;
import cz.mzk.recordmanager.server.scripting.function.RecordFunction;

public class DublinCoreDSL extends BaseDSL {

	private final DublinCoreRecord record;

	private final Map<String, RecordFunction<DublinCoreRecord>> functions;

	public DublinCoreDSL(DublinCoreRecord record,
			MappingResolver propertyResolver,
			Map<String, RecordFunction<DublinCoreRecord>> functions) {
		super(propertyResolver);
		this.record = record;
		this.functions = functions;
	}

	public String getFirstTitle() {
		return record.getFirstTitle();
	}
	
	public String getFullRecord() {
		String result = "";
		try {
			result = record.getRawRecord() == null ? "" : new String(record.getRawRecord(), "UTF-8");
		} catch (UnsupportedEncodingException e) {}
		return result;
	}

	public Object methodMissing(String methodName, Object args) {
		RecordFunction<DublinCoreRecord> func = functions.get(methodName);
		if (func == null) {
			throw new IllegalArgumentException(String.format("missing function: %s", methodName));
		}
		return func.apply(record, args);
	}

}
