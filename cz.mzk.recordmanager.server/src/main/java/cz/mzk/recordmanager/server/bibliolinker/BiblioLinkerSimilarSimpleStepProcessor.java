package cz.mzk.recordmanager.server.bibliolinker;

import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;
import cz.mzk.recordmanager.server.model.BiblioLinkerSimilarType;
import cz.mzk.recordmanager.server.model.BiblioLinkerSimiliar;
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
 * Generic implementation of of ItemProcessor
 */
@Component
public class BiblioLinkerSimilarSimpleStepProcessor implements
		ItemProcessor<List<Long>, List<HarvestedRecord>> {

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;

	@Autowired
	private MetadataRecordFactory mrf;

	@Autowired
	private MarcXmlParser marcXmlParser;

	private static Logger logger = LoggerFactory.getLogger(BiblioLinkerSimilarSimpleStepProcessor.class);

	private ProgressLogger progressLogger = new ProgressLogger(logger, 1000);

	private BiblioLinkerSimilarType type;

	private static final List<BiblioLinkerSimilarType> ONLY_EMPTY_SIMILAR =
			new ArrayList<>(Collections.singletonList(BiblioLinkerSimilarType.ENTITY_LANGUAGE));

	private static final int MAX_SIMILARS = 5;

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
				if (!similarIds.isEmpty() && ONLY_EMPTY_SIMILAR.contains(type)) continue;
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
					if (similarIds.size() >= MAX_SIMILARS) break;
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

	private String getUrlId(final HarvestedRecord hr) {
		JSONObject sampleObject = new JSONObject();
		MetadataRecord mr = mrf.getMetadataRecord(hr);
		List<String> isn;
		sampleObject.put("id", hr.getHarvestedFrom().getIdPrefix() + '.' + hr.getUniqueId().getRecordId());
		List<HarvestedRecordFormatEnum> formats =mr.getDetectedFormatList();
		if (!formats.isEmpty()) {
			List<String> hierarchicFormats = SolrUtils.createRecordTypeHierarchicFacet(formats.get(0));
			sampleObject.put("format", hierarchicFormats.get(hierarchicFormats.size()-1));
		}
		sampleObject.put("author", mr.getAuthorDisplay());
		sampleObject.put("title", mr.getTitle().isEmpty() ? "" : mr.getTitle().get(0).getTitleStr());
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
