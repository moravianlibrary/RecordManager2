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

		// get all dedup records
		Set<DedupRecord> drs = new HashSet<>();
		for (Long id : idList) {
			drs.add(dedupRecordDAO.get(id));
		}
		// get local records by dedup_record_id
		for (DedupRecord dr : drs) {
			hrs.addAll(harvestedRecordDao.getByDedupRecordWithDeleted(dr));
		}
		// get local records by biblio_record_id
		Set<BiblioLinker> bls = hrs.stream().map(HarvestedRecord::getBiblioLinker).collect(Collectors.toSet());
		hrs.addAll(harvestedRecordDao.getByBiblioLinkerAndNotDedupRecord(drs, bls));

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

		// set biblio_linker_id, update if needed
		Set<HarvestedRecord> update = new HashSet<>();
		for (HarvestedRecord hr : hrs) {
			if (hr.getBiblioLinker() != bl || hr.isBiblioLinkerSimilar()) {
				hr.setBiblioLinker(bl);
				hr.setBiblioLinkerSimilar(false);
				update.add(hr);
			}
			progressLogger.incrementAndLogProgress();
		}

		// choose one record for next job (similarity)
		HarvestedRecord bestRecord = hrs.iterator().next();
		for (HarvestedRecord hr : hrs) {
			if (hr.getDeleted() == null && hr.getWeight() != null) {
				if (bestRecord.getWeight() == null || bestRecord.getWeight() < hr.getWeight()) bestRecord = hr;
			}
		}
		bestRecord.setBiblioLinkerSimilar(true);
		update.add(bestRecord);

		return new ArrayList<>(update);
	}


}
