package cz.mzk.recordmanager.server.scripting.marc;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.scripting.FunctionContext;

public class MarcFunctionContext implements FunctionContext<MarcRecord> {

	private final MarcRecord record;

	private final HarvestedRecord harvestedRecord;

	public MarcFunctionContext(MarcRecord record, HarvestedRecord harvestedRecord) {
		this.record = record;
		this.harvestedRecord = harvestedRecord;
	}

	public MarcFunctionContext(MarcRecord record) {
		this(record, null);
	}

	@Override
	public MarcRecord record() {
		return record;
	}

	public HarvestedRecord harvestedRecord() {
		return harvestedRecord;
	}

}
