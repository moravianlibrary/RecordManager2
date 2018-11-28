package cz.mzk.recordmanager.server.bibliolinker;

import cz.mzk.recordmanager.server.model.BiblioLinker;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.BiblioLinkerDAO;
import cz.mzk.recordmanager.server.oai.dao.DedupRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

	@Override
	public List<HarvestedRecord> process(List<Long> idList) throws Exception {
		List<HarvestedRecord> hrList = new ArrayList<>();

		// get all local records
		for (Long id : idList) {
			hrList.addAll(harvestedRecordDao.getByDedupRecordWithDeleted(dedupRecordDAO.get(id)));
		}

		// get any BiblioLinkerRecord
		BiblioLinker bl = null;
		for (HarvestedRecord hr : hrList) {
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

		for (HarvestedRecord hr : hrList) {
			hr.setBiblioLinker(bl);
		}

		return hrList;
	}
}
