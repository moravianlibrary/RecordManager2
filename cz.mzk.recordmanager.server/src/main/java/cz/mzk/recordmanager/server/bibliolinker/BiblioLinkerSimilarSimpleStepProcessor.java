package cz.mzk.recordmanager.server.bibliolinker;

import cz.mzk.recordmanager.server.model.BiblioLinkerSimilarType;
import cz.mzk.recordmanager.server.model.BiblioLinkerSimiliar;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.ProgressLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Generic implementation of of ItemProcessor
 */
@Component
public class BiblioLinkerSimilarSimpleStepProcessor implements
		ItemProcessor<List<Long>, List<HarvestedRecord>> {

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;


	private static Logger logger = LoggerFactory.getLogger(BiblioLinkerSimilarSimpleStepProcessor.class);

	private ProgressLogger progressLogger = new ProgressLogger(logger, 1000);

	private BiblioLinkerSimilarType type;

	public BiblioLinkerSimilarSimpleStepProcessor() {
	}

	public BiblioLinkerSimilarSimpleStepProcessor(BiblioLinkerSimilarType type) {
		this.type = type;
	}

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
			Set<BiblioLinkerSimiliar> similarIds = new TreeSet<>(hrOuter.getBiblioLinkerSimiliarUrls());
			for (HarvestedRecord hrInner : hrIds.keySet()) {
				if (hrOuter == hrInner) continue;
				similarIds.add(BiblioLinkerSimiliar.create(hrIds.get(hrInner), type));
				if (similarIds.size() >= 5) break;
			}
			hrOuter.setBiblioLinkerSimiliarUrls(new ArrayList<>(similarIds));
		}
		return new ArrayList<>(hrIds.keySet());
	}
}
