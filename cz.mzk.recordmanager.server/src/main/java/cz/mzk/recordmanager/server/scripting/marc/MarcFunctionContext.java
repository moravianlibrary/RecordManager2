package cz.mzk.recordmanager.server.scripting.marc;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.metadata.mappings996.Mappings996;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.scripting.FunctionContext;

public class MarcFunctionContext implements FunctionContext<MarcRecord> {

	private final MarcRecord record;

	private final HarvestedRecord harvestedRecord;

	private final MetadataRecord metadataRecord;

	private final Mappings996 mappings996;

	public MarcFunctionContext(MarcRecord record, HarvestedRecord harvestedRecord, MetadataRecord metadataRecord,
			Mappings996 mappings996) {
		this.record = record;
		this.harvestedRecord = harvestedRecord;
		this.metadataRecord = metadataRecord;
		this.mappings996 = mappings996;
	}

	public MarcFunctionContext(MarcRecord record) {
		this(record, null, null, null);
	}
	
	public MarcFunctionContext(MarcRecord record, MetadataRecord metadataRecord) {
		this(record, null, metadataRecord, null);
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

	public Mappings996 mappings996() {
		return mappings996;
	}
}
