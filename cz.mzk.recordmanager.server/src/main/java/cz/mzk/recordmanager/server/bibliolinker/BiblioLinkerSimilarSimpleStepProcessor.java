package cz.mzk.recordmanager.server.bibliolinker;

import cz.mzk.recordmanager.server.marc.InvalidMarcException;
import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;
import cz.mzk.recordmanager.server.model.BiblioLinkerSimilar;
import cz.mzk.recordmanager.server.model.BiblioLinkerSimilarType;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.ProgressLogger;
import cz.mzk.recordmanager.server.util.SolrUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generic implementation of ItemProcessor
 */
@Component
public class BiblioLinkerSimilarSimpleStepProcessor implements
		ItemProcessor<List<Long>, List<HarvestedRecord>> {

	@Autowired
	protected HarvestedRecordDAO harvestedRecordDao;

	@Autowired
	private MetadataRecordFactory mrf;

	private static Logger logger = LoggerFactory.getLogger(BiblioLinkerSimilarSimpleStepProcessor.class);

	private ProgressLogger progressLogger = new ProgressLogger(logger, 1000);

	protected BiblioLinkerSimilarType type;

	// maximum similarities in step
	protected int NEW_SIMILARS_FOR_STEP;
	// maximum similarities count per step
	protected int MAX_SIMILARS_FOR_STEP;
	// maximum similarities count per record
	protected static final int MAX_SIMILARS_FOR_INDEX = 5;

	private static final List<BiblioLinkerSimilarType> ONLY_EMPTY_SIMILAR =
			new ArrayList<>(Collections.singletonList(BiblioLinkerSimilarType.ENTITY_LANGUAGE));

	public BiblioLinkerSimilarSimpleStepProcessor() {
	}

	public BiblioLinkerSimilarSimpleStepProcessor(BiblioLinkerSimilarType type) {
		this.type = type;
		this.MAX_SIMILARS_FOR_STEP = Integer.MAX_VALUE;
		this.NEW_SIMILARS_FOR_STEP = Integer.MAX_VALUE;
	}

	public BiblioLinkerSimilarSimpleStepProcessor(BiblioLinkerSimilarType type, int similarityStepLimit, int newSimilarsCount) {
		this.type = type;
		this.MAX_SIMILARS_FOR_STEP = similarityStepLimit;
		this.NEW_SIMILARS_FOR_STEP = newSimilarsCount;
	}

	@Override
	public List<HarvestedRecord> process(List<Long> biblioIdsList) throws Exception {
		return processRecords(sortRecords(harvestedRecordDao.getByBiblioLinkerIds(biblioIdsList)));
	}

	protected List<HarvestedRecord> processRecords(Map<Long, Collection<HarvestedRecord>> records) {
		Set<HarvestedRecord> toUpdate = new HashSet<>();
		Set<BiblioLinkerSimilar> similarIds;
		Set<HarvestedRecord> similarHr;
		HarvestedRecord searched;
		for (Long blOuter : records.keySet()) {
			for (HarvestedRecord hr : records.get(blOuter)) {
				if (hr.getDeleted() != null) continue;
				similarIds = new TreeSet<>(hr.getBiblioLinkerSimilarUrls());
//				if (!similarIds.isEmpty() && ONLY_EMPTY_SIMILAR.contains(type)) continue;
				if (similarIds.size() >= MAX_SIMILARS_FOR_STEP) break;
				similarHr = new HashSet<>();
				int newSimilars = 0;
				for (Long blInner : records.keySet()) {
					// same BibliLinker groups
					if (blOuter.equals(blInner)) continue;
					searched = findSameInstitution(hr, records.get(blInner));
					if (searched == null) continue;
					if (isSimilar(hr, searched, similarHr)) {
						try {						
							similarIds.add(BiblioLinkerSimilar.create(getUrlId(searched), searched, type));
							similarHr.add(searched);
							// new similar record, must be updated
							toUpdate.add(hr);
							++newSimilars;
						} catch (InvalidMarcException ex) {
							logger.error(ex.getMessage() + " " + searched.getUniqueId());
						}					
					}
					if (similarIds.size() >= MAX_SIMILARS_FOR_INDEX || newSimilars >= NEW_SIMILARS_FOR_STEP) break;
				}
				hr.setBiblioLinkerSimilarUrls(new ArrayList<>(similarIds));
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

	private String getUrlId(final HarvestedRecord hr) {
		JSONObject sampleObject = new JSONObject();
		MetadataRecord mr = mrf.getMetadataRecord(hr);
		List<String> isn;
		sampleObject.put("id", hr.getHarvestedFrom().getIdPrefix() + '.' + hr.getUniqueId().getRecordId());
		List<HarvestedRecordFormatEnum> formats = mr.getDetectedFormatList();
		if (!formats.isEmpty()) {
			List<String> hierarchicFormats = SolrUtils.createRecordTypeHierarchicFacet(formats.get(0));
			sampleObject.put("format", hierarchicFormats.get(hierarchicFormats.size() - 1));
		}
		sampleObject.put("author", mr.getAuthorDisplay());
		sampleObject.put("title", mr.getTitleDisplay());
		if (!mr.getCNBs().isEmpty()) {
			sampleObject.put("cnb", mr.getCNBs().get(0).getCnb());
		}
		isn = mr.getISBNs().stream().map(i -> i.getIsbn().toString()).collect(Collectors.toList());
		if (!isn.isEmpty()) sampleObject.put("isbn", createJSONArray(isn));
		isn = mr.getISMNs().stream().map(i -> i.getIsmn().toString()).collect(Collectors.toList());
		if (!isn.isEmpty()) sampleObject.put("ismn", createJSONArray(isn));
		isn = mr.getISSNs().stream().map(i -> i.getIssn()).collect(Collectors.toList());
		if (!isn.isEmpty()) sampleObject.put("issn", createJSONArray(isn));
		isn = mr.getEANs().stream().map(i -> i.getEan().toString()).collect(Collectors.toList());
		if (!isn.isEmpty()) sampleObject.put("ean", createJSONArray(isn));
		return sampleObject.toString();
	}

	private static JSONArray createJSONArray(List<String> isn) {
		JSONArray result = new JSONArray();
		isn.forEach(result::put);
		return result;
	}

	protected Map<Long, Collection<HarvestedRecord>> sortRecords(final Collection<HarvestedRecord> hrs) {
		Map<Long, Collection<HarvestedRecord>> results = new HashMap<>();
		for (HarvestedRecord hr : hrs) {
			Long id = getIdForSorting(hr);
			if (results.containsKey(id)) {
				results.computeIfPresent(id, (key, value) -> value).add(hr);
			} else {
				results.computeIfAbsent(id, key -> new ArrayList<>()).add(hr);
			}
		}
		return results;
	}

	protected Long getIdForSorting(HarvestedRecord hr) {
		return hr.getBiblioLinker().getId();
	}

}
