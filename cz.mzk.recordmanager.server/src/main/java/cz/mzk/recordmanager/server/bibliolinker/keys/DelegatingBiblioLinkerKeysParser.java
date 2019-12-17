package cz.mzk.recordmanager.server.bibliolinker.keys;

import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DelegatingBiblioLinkerKeysParser implements BiblioLinkerKeysParser,
		InitializingBean {

	@Autowired
	private List<BiblioLinkerKeysParser> parsers;

	private Map<String, BiblioLinkerKeysParser> parserByFormat = new HashMap<>();

	@Override
	public List<String> getSupportedFormats() {
		return new ArrayList<>(parserByFormat.keySet());
	}

	@Override
	public HarvestedRecord parse(HarvestedRecord record) {
		BiblioLinkerKeysParser parser = parserByFormat.get(record.getFormat());
		if (parser == null) {
			throw new IllegalArgumentException(String.format(
					"Record with unsupported format passed: %s",
					record.getFormat()));
		}
		return parser.parse(record);
	}

	@Override
	public HarvestedRecord parse(HarvestedRecord record, MetadataRecord metadataRecord) {
		BiblioLinkerKeysParser parser = parserByFormat.get(record.getFormat());
		if (parser == null) {
			throw new IllegalArgumentException(String.format(
					"Record with unsupported format passed: %s",
					record.getFormat()));
		}
		return parser.parse(record, metadataRecord);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		for (BiblioLinkerKeysParser parser : parsers) {
			for (String format : parser.getSupportedFormats()) {
				parserByFormat.put(format, parser);
			}
		}
	}

}
