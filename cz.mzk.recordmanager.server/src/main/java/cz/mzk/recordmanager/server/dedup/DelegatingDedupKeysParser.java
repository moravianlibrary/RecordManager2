package cz.mzk.recordmanager.server.dedup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

@Component
public class DelegatingDedupKeysParser implements DedupKeysParser,
		InitializingBean {

	@Autowired
	private List<DedupKeysParser> parsers;

	private Map<String, DedupKeysParser> parserByFormat = new HashMap<>();

	@Override
	public List<String> getSupportedFormats() {
		return new ArrayList<>(parserByFormat.keySet());
	}

	@Override
	public HarvestedRecord parse(HarvestedRecord record) {
		DedupKeysParser parser = parserByFormat.get(record.getFormat());
		if (parser == null) {
			throw new IllegalArgumentException(String.format(
					"Record with unsupported format passed: %s",
					record.getFormat()));
		}
		return parser.parse(record);
	}
	
	@Override
	public HarvestedRecord parse(HarvestedRecord record, MetadataRecord metadataRecord) {
		DedupKeysParser parser = parserByFormat.get(record.getFormat());
		if (parser == null) {
			throw new IllegalArgumentException(String.format(
					"Record with unsupported format passed: %s",
					record.getFormat()));
		}
		return parser.parse(record, metadataRecord);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		for (DedupKeysParser parser : parsers) {
			for (String format : parser.getSupportedFormats()) {
				parserByFormat.put(format, parser);
			}
		}
	}

}
