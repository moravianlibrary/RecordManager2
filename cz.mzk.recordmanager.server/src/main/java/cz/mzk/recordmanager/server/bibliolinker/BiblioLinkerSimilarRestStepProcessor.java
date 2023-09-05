package cz.mzk.recordmanager.server.bibliolinker;

import cz.mzk.recordmanager.server.model.BiblioLinkerSimilar;
import cz.mzk.recordmanager.server.model.BiblioLinkerSimilarType;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.BiblioLinkerSimilarDAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.ProgressLogger;
import org.hibernate.query.criteria.internal.expression.function.AggregationFunction;
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
public class BiblioLinkerSimilarRestStepProcessor extends BiblioLinkerSimilarSimpleStepProcessor implements
		ItemProcessor<List<Long>, List<HarvestedRecord>> {

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;

	@Autowired
	private BiblioLinkerSimilarDAO blSimilarDao;

	private static Logger logger = LoggerFactory.getLogger(BiblioLinkerSimilarRestStepProcessor.class);

	private ProgressLogger progressLogger = new ProgressLogger(logger, 1000);

	// maximum similarities count per record
	protected static final int MAX_SIMILARS = 5;

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
			Collection<BiblioLinkerSimilar> bls = blSimilarDao.getByBilioLinkerId(blId, MAX_SIMILARS);
			if (bls.isEmpty()) continue;
			for (HarvestedRecord hr : hrs) {
				if (hr.getDeleted() != null) continue;
				Set<BiblioLinkerSimilar> similarIds = new TreeSet<>(hr.getBiblioLinkerSimilarUrls());
				for (BiblioLinkerSimilar bl : bls) {
					if (similarIds.size() >= MAX_SIMILARS) break;
					similarIds.add(BiblioLinkerSimilar.create(bl.getUrlId(), bl.getHarvestedRecordSimilarId(), type));
					toUpdate.add(hr);
				}
				hr.setBiblioLinkerSimilarUrls(new ArrayList<>(similarIds));
				progressLogger.incrementAndLogProgress();
			}
		}
		return new ArrayList<>(toUpdate);
	}

}
