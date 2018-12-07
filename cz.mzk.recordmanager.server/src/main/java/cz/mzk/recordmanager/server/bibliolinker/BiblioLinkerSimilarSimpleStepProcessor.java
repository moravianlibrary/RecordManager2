package cz.mzk.recordmanager.server.bibliolinker;

import cz.mzk.recordmanager.server.model.BiblioLinkerSimiliar;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.BiblioLinkerSimilarDAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.ProgressLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic implementation of of ItemProcessor
 */
@Component
public class BiblioLinkerSimilarSimpleStepProcessor implements
		ItemProcessor<List<Long>, List<HarvestedRecord>> {

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;

	@Autowired
	private BiblioLinkerSimilarDAO biblioLinkerSimilarDAO;

	private static Logger logger = LoggerFactory.getLogger(BiblioLinkerSimilarSimpleStepProcessor.class);

	private ProgressLogger progressLogger = new ProgressLogger(logger, 1000);

	@Override
	public List<HarvestedRecord> process(List<Long> idList) throws Exception {
		Map<HarvestedRecord, String> hrIds = new HashMap<>();
		for (Long id : idList) {
			HarvestedRecord hr = harvestedRecordDao.get(id);
			String urlId = hr.getHarvestedFrom().getIdPrefix() + '.' + hr.getUniqueId().getRecordId();
			hrIds.put(hr, urlId);
			progressLogger.incrementAndLogProgress();
		}
		for (HarvestedRecord hrOuter : hrIds.keySet()) {
			List<BiblioLinkerSimiliar> similarIds = new ArrayList<>();
			hrOuter.getBiblioLinkerSimiliarUrls().forEach(id -> biblioLinkerSimilarDAO.delete(id));
			for (HarvestedRecord hrInner : hrIds.keySet()) {
				if (hrOuter == hrInner) continue;
				similarIds.add(BiblioLinkerSimiliar.create(hrIds.get(hrInner)));
			}
			hrOuter.setBiblioLinkerSimiliarUrls(similarIds);
		}
		return new ArrayList<>(hrIds.keySet());
	}
}
