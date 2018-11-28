package cz.mzk.recordmanager.server.bibliolinker;

import cz.mzk.recordmanager.server.model.BiblioLinker;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.BiblioLinkerDAO;
import cz.mzk.recordmanager.server.oai.dao.DedupRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.ProgressLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generic implementation of of ItemProcessor
 */
@Component
public class BiblioLinkerSimpleKeysStepProcessor implements
		ItemProcessor<List<Long>, List<HarvestedRecord>> {

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;

	@Autowired
	private DedupRecordDAO dedupRecordDAO;

	@Autowired
	private BiblioLinkerDAO biblioLinkerDAO;

	private static Logger logger = LoggerFactory.getLogger(BiblioLinkerSimpleKeysStepProcessor.class);
	private ProgressLogger progressLogger = new ProgressLogger(logger, 1000);

	@Override
	public List<HarvestedRecord> process(List<Long> idList) throws Exception {
		Set<HarvestedRecord> hrs = new HashSet<>();

		// get all local records
		for (Long id : idList) {
			hrs.addAll(harvestedRecordDao.getByDedupRecordWithDeleted(dedupRecordDAO.get(id)));
		}
		Set<DedupRecord> drs = hrs.stream().map(HarvestedRecord::getDedupRecord).collect(Collectors.toSet());
		Set<BiblioLinker> bls = hrs.stream().map(HarvestedRecord::getBiblioLinker).collect(Collectors.toSet());
		hrs.addAll(harvestedRecordDao.getByDedupRecordAndNotBiblioLinker(drs, bls));

		// get any BiblioLinkerRecord
		BiblioLinker bl = null;
		for (HarvestedRecord hr : hrs) {
			if (hr.getBiblioLinker() != null) {
				bl = hr.getBiblioLinker();
				break;
			}
		}

		if (bl == null) {
			// create new
			bl = new BiblioLinker();
			bl.setUpdated(new Date());
			bl = biblioLinkerDAO.persist(bl);
		}

		List<HarvestedRecord> update = new ArrayList<>();
		for (HarvestedRecord hr : hrs) {
			if (hr.getBiblioLinker() != bl) {
				hr.setBiblioLinker(bl);
				update.add(hr);
			}
			progressLogger.incrementAndLogProgress();
		}
		return update;
	}
}
