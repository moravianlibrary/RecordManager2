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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

	public BiblioLinkerSimilarRestStepProcessor() {
	}

	public BiblioLinkerSimilarRestStepProcessor(BiblioLinkerSimilarType type) {
		this.type = type;
	}

	@Override
	public List<HarvestedRecord> process(List<Long> biblioIdsList) throws Exception {
		List<HarvestedRecord> toUpdate = new ArrayList<>();
		for (Long blId : biblioIdsList) {
			Collection<HarvestedRecord> hrs = harvestedRecordDao.getByBiblioLinkerIdAndSimilarFlag(blId);
			Collection<BiblioLinkerSimilar> bls = blSimilarDao.getByBilioLinkerId(blId, MAX_SIMILARS);
			if (bls.isEmpty()) continue;
			for (HarvestedRecord hr : hrs) {
				if (hr.getDeleted() != null) continue;
				List<BiblioLinkerSimilar> newBls = new ArrayList<>();
				for (BiblioLinkerSimilar bl : bls) {
					newBls.add(BiblioLinkerSimilar.create(bl.getUrlId(), bl.getHarvestedRecordSimilarId(), type));
				}
				hr.setBiblioLinkerSimilarUrls(newBls);
				toUpdate.add(hr);
				progressLogger.incrementAndLogProgress();
			}
		}
		return toUpdate;
	}

}
