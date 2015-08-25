package cz.mzk.recordmanager.server.scripting.marc.function;

import java.util.List;

import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.scripting.marc.MarcFunctionContext;

@Component
public class IdentifiersMarcFunctions implements MarcRecordFunctions {

	public List<String> getEAN(MarcFunctionContext ctx) {
		MarcRecord record = ctx.record();
		return record.getFields("024", field -> field.getIndicator1() == '3', 'a');
	}

	public String getNBN(MarcFunctionContext ctx) {
		MarcRecord record = ctx.record();
		return record.getField("015", 'a');
	}

}
