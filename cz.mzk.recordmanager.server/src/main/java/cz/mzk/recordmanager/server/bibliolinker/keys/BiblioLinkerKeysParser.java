package cz.mzk.recordmanager.server.bibliolinker.keys;

import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

import java.util.List;

public interface BiblioLinkerKeysParser {

	List<String> getSupportedFormats();

	HarvestedRecord parse(HarvestedRecord record) throws BiblioLinkerKeyParserException;

	/**
	 * reuses existing {@link MetadataRecord} object, otherwise acts the same way as parse({@link HarvestedRecord})
	 */
	HarvestedRecord parse(HarvestedRecord record, MetadataRecord metadataRecord) throws BiblioLinkerKeyParserException;

}
