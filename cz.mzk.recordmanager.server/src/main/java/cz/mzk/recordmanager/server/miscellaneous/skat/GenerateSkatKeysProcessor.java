package cz.mzk.recordmanager.server.miscellaneous.skat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.marc4j.marc.DataField;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.SkatKey;
import cz.mzk.recordmanager.server.model.SkatKey.SkatKeyCompositeId;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;

public class GenerateSkatKeysProcessor implements ItemProcessor<Long, List<SkatKey>> {

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;
	
	@Autowired
	private MarcXmlParser marcXmlParser;
	
	@Override
	public List<SkatKey> process(Long item) throws Exception {
		HarvestedRecord hr = harvestedRecordDao.get(item);
		if (hr.getRawRecord() == null) {
			return null;
		}
	
		InputStream is = new ByteArrayInputStream(hr.getRawRecord());
		MarcRecord marc = marcXmlParser.parseRecord(is);

		List<SkatKey> result = new ArrayList<>();
		for (DataField df: marc.getDataFields("910")) {
			if (df.getSubfield('a') == null) {
				continue;
			}
			if (df.getSubfield('x') == null) {
				continue;
			}
			String sigla = df.getSubfield('a').getData();
			String recordId = df.getSubfield('x').getData();
			
			SkatKey key = new SkatKey(new SkatKeyCompositeId(hr.getId(), sigla, recordId));
			result.add(key);
		}
		return result;
	}

}
