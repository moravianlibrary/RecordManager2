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
	public List<HarvestedRecord> process(List<Long> biblioIdsList) throws Exception {
		Map<Long, Collection<HarvestedRecord>> records = new HashMap<>();
		Set<HarvestedRecord> toUpdate = new HashSet<>();
		for (Long blId : new HashSet<>(biblioIdsList)) {
			records.put(blId, harvestedRecordDao.getByBiblioLinkerId(blId));
		}
		for (Long blOuter : records.keySet()) {
			for (HarvestedRecord hr : records.get(blOuter)) {
				Set<BiblioLinkerSimiliar> similarIds = new TreeSet<>(hr.getBiblioLinkerSimiliarUrls());
				for (Long blInner : records.keySet()) {
					if (similarIds.size() >= 5) break;
					if (blOuter.equals(blInner)) continue;
					HarvestedRecord searched = findSameInstitution(hr, records.get(blInner));
					if (searched == null) continue;
					similarIds.add(BiblioLinkerSimiliar.create(getUrlId(searched), type));
					toUpdate.add(hr);
				}
				hr.setBiblioLinkerSimiliarUrls(new ArrayList<>(similarIds));
				progressLogger.incrementAndLogProgress();
			}
		}
		return new ArrayList<>(toUpdate);
	}

	private static HarvestedRecord findSameInstitution(final HarvestedRecord source, final Collection<HarvestedRecord> searched) {
		String institutionPrefix = source.getHarvestedFrom().getIdPrefix();
		for (HarvestedRecord hr : searched) {
			if (hr.getDeleted() != null) continue; // deleted record
			if (hr.getHarvestedFrom().getIdPrefix().equals(institutionPrefix)) {
				return hr;
			}
		}
		return null;
	}

	private static String getUrlId(final HarvestedRecord hr) {
		return hr.getHarvestedFrom().getIdPrefix() + '.' + hr.getUniqueId().getRecordId();
	}

	private static List<HarvestedRecord> getAllRecords(final Map<Long, Collection<HarvestedRecord>> map) {
		List<HarvestedRecord> results = new ArrayList<>();
		for (Collection<HarvestedRecord> hrs : map.values()) {
			results.addAll(hrs);
		}
		return results;
	}

}
