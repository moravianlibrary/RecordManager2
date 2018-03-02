package cz.mzk.recordmanager.server.dedup;

import java.util.List;

import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

public interface DedupKeysParser {

	List<String> getSupportedFormats();

	HarvestedRecord parse(HarvestedRecord record) throws DedupKeyParserException;
	
	/**
	 * reuses existing {@link MetadataRecord} object, otherwise acts the same way as parse({@link HarvestedRecord})
	 */
	HarvestedRecord parse(HarvestedRecord record, MetadataRecord metadataRecord) throws DedupKeyParserException;

}
