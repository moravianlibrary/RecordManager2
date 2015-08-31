package cz.mzk.recordmanager.server.scripting.marc;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.scripting.FunctionContext;

public class MarcFunctionContext implements FunctionContext<MarcRecord> {

	private final MarcRecord record;

	public MarcFunctionContext(MarcRecord record) {
		this.record = record;
	}

	@Override
	public MarcRecord record() {
		return record;
	}

}
