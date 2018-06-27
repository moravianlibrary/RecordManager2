package cz.mzk.recordmanager.server.miscellaneous.caslin.keys;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.marc4j.marc.DataField;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.SkatKey;
import cz.mzk.recordmanager.server.model.SkatKey.SkatKeyCompositeId;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.SkatKeyDAO;

public class GenerateSkatKeysProcessor implements ItemProcessor<Long, Set<SkatKey>> {

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;
	
	@Autowired
	private SkatKeyDAO skatKeyDao;
	
	@Autowired
	private MarcXmlParser marcXmlParser;
	
	@Override
	public Set<SkatKey> process(Long item) throws Exception {
		Set<SkatKey> parsedKeys = new HashSet<>();
		HarvestedRecord hr = harvestedRecordDao.get(item);
		if (hr.getRawRecord() == null) {
			return parsedKeys;
		}
		
		MarcRecord marc = null;
		InputStream is = new ByteArrayInputStream(hr.getRawRecord());
		try {
			marc = marcXmlParser.parseRecord(is);
		} catch (Exception e) {
			return parsedKeys;
		}
		
		for (DataField df: marc.getDataFields("996")) {
			if (df.getSubfield('e') == null) {
				continue;
			}
			if (df.getSubfield('w') == null) {
				continue;
			}
			String sigla = df.getSubfield('e').getData();
			String recordId = df.getSubfield('w').getData();
			
			if (recordId.length() > 100 || sigla.length() > 20) {
				//ignore garbage
				continue;
			}
			
			SkatKey key = new SkatKey(new SkatKeyCompositeId(hr.getId(), sigla, recordId));
			parsedKeys.add(key);
		}
		
		// ignore records having not enough information
		if (parsedKeys.size() < 2) {
			return Collections.emptySet();
		}

		// find already existing keys
		Set<SkatKey> existingKeys = new HashSet<>(skatKeyDao.getSkatKeysForRecord(item));
		Set<SkatKey> newKeys = new HashSet<>();
		for(SkatKey current: parsedKeys) {
			if (existingKeys.contains(current)) {
				continue;
			}
			newKeys.add(current);
		}

		return newKeys;
	}

}
