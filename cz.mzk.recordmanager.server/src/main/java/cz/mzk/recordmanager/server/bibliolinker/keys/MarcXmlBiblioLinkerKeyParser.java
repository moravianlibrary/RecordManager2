package cz.mzk.recordmanager.server.bibliolinker.keys;

import com.google.common.base.Preconditions;
import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class MarcXmlBiblioLinkerKeyParser extends HashingBiblioLinkerKeyParser {

	private final static String FORMAT = "marc21-xml";
	
	@Autowired 
	private MetadataRecordFactory metadataFactory;

	@Override
	public List<String> getSupportedFormats() {
		return Collections.singletonList(FORMAT);
	}

	@Override
	public HarvestedRecord parse(HarvestedRecord record) {
		Preconditions.checkArgument(FORMAT.equals(record.getFormat()));
		MetadataRecord metadata = metadataFactory.getMetadataRecord(record);
		return parse(record, metadata);
	}
}
