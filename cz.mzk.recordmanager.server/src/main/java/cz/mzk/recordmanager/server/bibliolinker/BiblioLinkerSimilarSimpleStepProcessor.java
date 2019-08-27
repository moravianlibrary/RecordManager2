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
		Map<Long, Collection<HarvestedRecord>> records;
		Set<HarvestedRecord> toUpdate = new HashSet<>();
		Set<BiblioLinkerSimiliar> similarIds;
		Set<HarvestedRecord> similarHr;
		HarvestedRecord searched;
		// get all records by BiblioLinker id
		records = sortrecords(harvestedRecordDao.getByBiblioLinkerIds(biblioIdsList));
		for (Long blOuter : records.keySet()) {
			for (HarvestedRecord hr : records.get(blOuter)) {
				similarIds = new TreeSet<>(hr.getBiblioLinkerSimiliarUrls());
				similarHr = new HashSet<>();
				for (Long blInner : records.keySet()) {
					// same BibliLinker groups
					if (blOuter.equals(blInner)) continue;
					searched = findSameInstitution(hr, records.get(blInner));
					if (searched == null) continue;
					if (isSimilar(hr, searched, similarHr)) {
						similarIds.add(BiblioLinkerSimiliar.create(getUrlId(searched), searched, type));
						similarHr.add(searched);
						// new similar record, must be updated
						toUpdate.add(hr);
					}
					if (similarIds.size() >= 5) break;
				}
				hr.setBiblioLinkerSimiliarUrls(new ArrayList<>(similarIds));
				progressLogger.incrementAndLogProgress();
			}
		}
		return new ArrayList<>(toUpdate);
	}

	protected boolean isSimilar(HarvestedRecord hr1, HarvestedRecord hr2, Set<HarvestedRecord> similars) {
		return true;
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

	private static Map<Long, Collection<HarvestedRecord>> sortrecords(final Collection<HarvestedRecord> hrs) {
		Map<Long, Collection<HarvestedRecord>> results = new HashMap<>();
		for (HarvestedRecord hr : hrs) {
			Long blId = hr.getBiblioLinker().getId();
			if (results.containsKey(blId)) {
				results.computeIfPresent(blId, (key, value) -> value).add(hr);
			} else {
				results.computeIfAbsent(blId, key -> new ArrayList<>()).add(hr);
			}
		}
		return results;
	}

}
