package cz.mzk.recordmanager.server.dedup;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.DedupKeys;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

@Component
public class MarcXmlDedupKeyParser implements DedupKeysParser {

	@Override
	public List<String> getSupportedFormats() {
		return Collections.singletonList("marc21-xml");
	}
	
	@Override
	public DedupKeys parse(HarvestedRecord record) {
		DedupKeys result = new DedupKeys();
		result.setId(record.getId());
		return result;
	}

}
