package cz.mzk.recordmanager.server.scripting.dc;

import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.scripting.FunctionContext;

public class DublinCoreFunctionContext implements FunctionContext<DublinCoreRecord> {
	
	private final DublinCoreRecord record;

	private final HarvestedRecord harvestedRecord;

	private final MetadataRecord metadataRecord;
	
	public DublinCoreFunctionContext(DublinCoreRecord record, HarvestedRecord harvestedRecord, MetadataRecord metadataRecord) {
		this.record = record;
		this.harvestedRecord = harvestedRecord;
		this.metadataRecord = metadataRecord;
	}

	public DublinCoreFunctionContext(DublinCoreRecord record) {
		this(record, null, null);
	}
	
	public DublinCoreFunctionContext(DublinCoreRecord record, MetadataRecord metadataRecord) {
		this(record, null, metadataRecord);
	}

	@Override
	public DublinCoreRecord record() {
		return record;
	}

	public HarvestedRecord harvestedRecord() {
		return harvestedRecord;
	}
	
	public MetadataRecord metadataRecord(){
		return metadataRecord;
	}

}
