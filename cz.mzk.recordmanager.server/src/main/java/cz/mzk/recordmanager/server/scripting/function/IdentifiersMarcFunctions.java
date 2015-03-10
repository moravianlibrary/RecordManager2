package cz.mzk.recordmanager.server.scripting.function;

import java.util.List;

import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.marc.MarcRecord;

@Component
public class IdentifiersMarcFunctions implements MarcRecordFunctions {

	public List<String> getEAN(MarcRecord record) {
		return record.getFields("024", field -> field.getIndicator1() == '3', 'a');
	}

	public String getNBN(MarcRecord record) {
		return record.getField("015", 'a');
	}

}
