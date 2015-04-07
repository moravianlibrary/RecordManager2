package cz.mzk.recordmanager.server.export;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;

public class ExportRecordsProcessor implements ItemProcessor<Long, String> {

	private IOFormat iOFormat;
	
	@Autowired
	private MetadataRecordFactory metadataFactory;
	
	@Autowired
	private MarcXmlParser marcXmlParser;
	
	public ExportRecordsProcessor(IOFormat format) {
		this.iOFormat = format;
	}
	
	@Override
	public String process(Long recordId) throws Exception {
		MetadataRecord record = metadataFactory.getMetadataRecord(recordId);
		return record != null ? record.export(iOFormat) : "";
	}

}
