package cz.mzk.recordmanager.server.bibliolinker;

import cz.mzk.recordmanager.server.model.BiblioLinkerSimilar;
import cz.mzk.recordmanager.server.model.BiblioLinkerSimilarType;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.BiblioLinkerSimilarDAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.ProgressLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Generic implementation of ItemProcessor
 */
@Component
public class BiblioLinkerSimilarRestStepProcessor extends BiblioLinkerSimilarSimpleStepProcessor implements
		ItemProcessor<List<Long>, List<HarvestedRecord>> {

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;

	@Autowired
	private BiblioLinkerSimilarDAO blSimilarDao;

	private static final Logger logger = LoggerFactory.getLogger(BiblioLinkerSimilarRestStepProcessor.class);

	private final ProgressLogger progressLogger = new ProgressLogger(logger, 1000);

	// maximum similarities count per record
	protected static final int MAX_SIMILAR_RECORDS = 5;

	public BiblioLinkerSimilarRestStepProcessor() {
	}

	public BiblioLinkerSimilarRestStepProcessor(BiblioLinkerSimilarType type) {
		this.type = type;
	}

	@Override
	public List<HarvestedRecord> process(List<Long> biblioIdsList) throws Exception {
		Set<HarvestedRecord> toUpdate = new HashSet<>();
		for (Long blId : biblioIdsList) {
			Collection<HarvestedRecord> hrs = harvestedRecordDao.getByBiblioLinkerIdAndSimilarFlag(blId);
			Map<Long, BiblioLinkerSimilar> potentialSimilar = getPotentialSimilar(blId);
			if (potentialSimilar.isEmpty()) continue;
			for (HarvestedRecord hr : hrs) {
				if (hr.getDeleted() != null) continue;
				Set<BiblioLinkerSimilar> actualSimilar = new TreeSet<>(hr.getBiblioLinkerSimilarUrls());
				List<Long> similarIds = getBiblioLinkerIds(actualSimilar);
				for (Map.Entry<Long, BiblioLinkerSimilar> entry : potentialSimilar.entrySet()) {
					if (actualSimilar.size() >= MAX_SIMILAR_RECORDS) break;
					if (similarIds.contains(entry.getKey())) continue;
					actualSimilar.add(BiblioLinkerSimilar.create(entry.getValue().getUrlId(), entry.getValue().getHarvestedRecordSimilarId(), type));
					similarIds.add(entry.getKey());
					toUpdate.add(hr);
				}
				hr.setBiblioLinkerSimilarUrls(new ArrayList<>(actualSimilar));
				progressLogger.incrementAndLogProgress();
			}
		}
		return new ArrayList<>(toUpdate);
	}

	protected Map<Long, BiblioLinkerSimilar> getPotentialSimilar(Long blId) {
		Map<Long, BiblioLinkerSimilar> results = new HashMap<>();
		for (BiblioLinkerSimilar bs : blSimilarDao.getByBilioLinkerId(blId, MAX_SIMILAR_RECORDS * 2)) {
			results.put(bs.getHarvestedRecordSimilar().getBiblioLinker().getId(), bs);
		}
		return results;
	}

	protected List<Long> getBiblioLinkerIds(Set<BiblioLinkerSimilar> similars) {
		List<Long> results = new ArrayList<>();
		for (BiblioLinkerSimilar bs : similars) {
			results.add(bs.getHarvestedRecordSimilar().getBiblioLinker().getId());
		}
		return results;
	}

}
