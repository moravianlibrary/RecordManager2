package cz.mzk.recordmanager.server.miscellaneous.caslin.keys;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.SkatKey;
import cz.mzk.recordmanager.server.model.SkatKey.SkatKeyCompositeId;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.SkatKeyDAO;
import cz.mzk.recordmanager.server.util.ProgressLogger;
import org.marc4j.marc.DataField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class GenerateSkatKeysProcessor implements ItemProcessor<Long, Set<SkatKey>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenerateSkatKeysProcessor.class);

	private static final ProgressLogger PROGRESS_LOGGER = new ProgressLogger(LOGGER, 1000);

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;

	@Autowired
	private SkatKeyDAO skatKeyDao;

	@Autowired
	private MarcXmlParser marcXmlParser;

	@Override
	public Set<SkatKey> process(Long item) {
		PROGRESS_LOGGER.incrementAndLogProgress();
		Set<SkatKey> parsedKeys = new HashSet<>();
		HarvestedRecord hr = harvestedRecordDao.get(item);
		if (hr.getRawRecord() == null) {
			return deleteKeys(hr, Collections.emptySet());
		}
		MarcRecord marc;
		try {
			marc = marcXmlParser.parseRecord(new ByteArrayInputStream(hr.getRawRecord()));
		} catch (Exception e) {
			return deleteKeys(hr, Collections.emptySet());
		}
		for (DataField df : marc.getDataFields("996")) {
			if (df.getSubfield('e') == null || df.getSubfield('w') == null) {
				continue;
			}
			String sigla = df.getSubfield('e').getData();
			String recordId = df.getSubfield('w').getData();

			if (recordId.length() > 100 || sigla.length() > 20) {
				//ignore garbage
				continue;
			}
			parsedKeys.add(new SkatKey(new SkatKeyCompositeId(hr.getId(), sigla, recordId)));
		}
		return deleteKeys(hr, parsedKeys);
	}

	/**
	 * Delete removed keys, return new keys, ignore others
	 *
	 * @param hr         caslin record
	 * @param parsedKeys all keys from caslin record
	 * @return Set of new {@link SkatKey}
	 */
	private Set<SkatKey> deleteKeys(HarvestedRecord hr, Set<SkatKey> parsedKeys) {
		// find already existing keys
		Set<SkatKey> existingKeys = new HashSet<>(skatKeyDao.getSkatKeysForRecord(hr.getId()));
		Set<SkatKey> newKeys = new HashSet<>();
		for (SkatKey current : parsedKeys) {
			if (existingKeys.contains(current)) {
				existingKeys.remove(current);
				continue;
			}
			newKeys.add(current);
		}
		// dedup if old keys exists
		if (!existingKeys.isEmpty()) {
			hr.setNextDedupFlag(true);
			harvestedRecordDao.persist(hr);
		}
		// delete old keys
		for (SkatKey existingKey : existingKeys) {
			skatKeyDao.delete(existingKey);
		}
		return newKeys;
	}

}
