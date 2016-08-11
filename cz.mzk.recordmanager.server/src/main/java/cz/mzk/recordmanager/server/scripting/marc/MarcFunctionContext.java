package cz.mzk.recordmanager.server.scripting.marc;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.scripting.FunctionContext;

public class MarcFunctionContext implements FunctionContext<MarcRecord> {
	
	private final MarcRecord record;

	private final HarvestedRecord harvestedRecord;

	private final MetadataRecord metadataRecord;
	
	public MarcFunctionContext(MarcRecord record, HarvestedRecord harvestedRecord, MetadataRecord metadataRecord) {
		this.record = record;
		this.harvestedRecord = harvestedRecord;
		this.metadataRecord = metadataRecord;
	}

	public MarcFunctionContext(MarcRecord record) {
		this(record, null, null);
	}
	
	public MarcFunctionContext(MarcRecord record, MetadataRecord metadataRecord) {
		this(record, null, metadataRecord);
	}

	@Override
	public MarcRecord record() {
		return record;
	}

	public HarvestedRecord harvestedRecord() {
		return harvestedRecord;
	}
	
	public MetadataRecord metadataRecord(){
		return metadataRecord;
	}

}
