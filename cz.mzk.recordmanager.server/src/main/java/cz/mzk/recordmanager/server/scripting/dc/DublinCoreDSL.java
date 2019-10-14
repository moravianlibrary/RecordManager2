package cz.mzk.recordmanager.server.scripting.dc;

import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.model.Isbn;
import cz.mzk.recordmanager.server.model.Issn;
import cz.mzk.recordmanager.server.model.Oclc;
import cz.mzk.recordmanager.server.scripting.BaseDSL;
import cz.mzk.recordmanager.server.scripting.ListResolver;
import cz.mzk.recordmanager.server.scripting.MappingResolver;
import cz.mzk.recordmanager.server.scripting.StopWordsResolver;
import cz.mzk.recordmanager.server.scripting.function.RecordFunction;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.SolrUtils;

import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DublinCoreDSL extends BaseDSL {

	private final Charset UTF8_CHARSET = Charset.forName("UTF-8");

	private final DublinCoreRecord record;
	private MetadataRecord dcMetadataRecord;

	private DublinCoreFunctionContext dcContext;

	private final Map<String, RecordFunction<DublinCoreFunctionContext>> functions;

	private final static Pattern AUTHOR_PATTERN = Pattern.compile("([^,]+),(.+)");
	private final static Pattern MDT_PATTERN = Pattern.compile("[\\W0-9]+");
	private static final Pattern PUBLIC_RIGHTS_PATTERN = Pattern.compile(".*public.*");

	private static final int ACTUAL_YEAR = Calendar.getInstance().get(Calendar.YEAR);

	public DublinCoreDSL(DublinCoreFunctionContext dcContext,
						 MappingResolver propertyResolver, StopWordsResolver stopWordsResolver, ListResolver listResolver,
						 Map<String, RecordFunction<DublinCoreFunctionContext>> functions) {
		super(propertyResolver, stopWordsResolver, listResolver);
		this.dcContext = dcContext;
		this.record = dcContext.record();
		this.functions = functions;
		this.dcMetadataRecord = dcContext.metadataRecord();
	}

	public String getFirstTitle() {
		return dcMetadataRecord.getTitleDisplay();
	}

	public String getFullRecord() {
		return record.getRawRecord() == null ? "" : new String(record.getRawRecord(), UTF8_CHARSET);
	}

	public String getRights() {
		List<String> rights = record.getRights();
		if (dcContext.harvestedRecord().getHarvestedFrom().getIdPrefix().equals(Constants.PREFIX_KRAM3_NKP)) {
			return Constants.DOCUMENT_AVAILABILITY_UNKNOWN;
		}
		if (rights == null || rights.isEmpty()) {
			return Constants.DOCUMENT_AVAILABILITY_UNKNOWN;
		}
		return rights.stream().anyMatch(s -> PUBLIC_RIGHTS_PATTERN.matcher(s).matches())
				? Constants.DOCUMENT_AVAILABILITY_ONLINE
				: Constants.DOCUMENT_AVAILABILITY_PROTECTED;
	}

	public List<String> getOtherTitles() {
		List<String> titles = record.getTitles();

		if (titles.size() <= 1) titles.clear();
		else titles.subList(1, titles.size());

		if (record.getTitleAlts() != null) titles.addAll(record.getTitleAlts());
		return titles;
	}

	public String getFirstCreator() {
		return record.getFirstCreator();
	}

	public List<String> getOtherCreators() {
		List<String> creators = record.getCreators();
		List<String> contributors = record.getContributors();
		if (!creators.isEmpty()) {
			creators.remove(0); //removes first creator who goes to different field
		}
		if (!contributors.isEmpty()) {
			creators.addAll(contributors); //adds all contributors to other creators
		}
		if (creators.isEmpty()) {
			return null;
		}
		return creators;
	}

	public String getAuthorDisplay() {
		return dcMetadataRecord.getAuthorDisplay();
	}

	public List<String> getAuthor2Display() {
		List<String> authors = getOtherCreators();
		if (authors == null) return Collections.emptyList();

		List<String> result = new ArrayList<>();
		for (String name : authors) {
			String newName = SolrUtils.changeNameDC(name);
			if (newName != null) result.add(newName);
		}
		return result;
	}

	public List<String> getAuthorFind() {
		List<String> result = new ArrayList<>();
		result.add(getAuthorDisplay());
		result.addAll(getAuthor2Display());
		return result;
	}

	public Integer getFirstDate() {
		try {
			return Integer.valueOf(record.getFirstDate());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public List<Integer> getPublishDateForTimeline() {
		Integer year = getFirstDate();
		if (year != null && 800 <= year && year <= (ACTUAL_YEAR + 1)) return Collections.singletonList(year);
		return null;
	}

	public List<String> getPublishers() {
		return record.getPublishers();
	}

	public List<String> getSubjects() {
		return record.getSubjects();
	}

	public List<String> getSubjectFacet() {
		List<String> subjects = getSubjects();
		if (subjects == null || subjects.isEmpty()) return subjects;

		List<String> result = new ArrayList<>();
		for (String subject : subjects) {
			if (!MDT_PATTERN.matcher(subject).matches()) result.add(subject);
		}
		return result;
	}

	public List<String> getAllFields() {
		List<String> resultsList = new ArrayList<>();
		resultsList.addAll(record.getContributors());
		resultsList.addAll(record.getCoverages());
		resultsList.addAll(record.getCreators());
		resultsList.addAll(record.getDates());
		resultsList.addAll(record.getDescriptions());
		resultsList.addAll(record.getFormats());
		resultsList.addAll(record.getIdentifiers());
		resultsList.addAll(record.getLanguages());
		resultsList.addAll(record.getPublishers());
		resultsList.addAll(record.getSubjects());
		resultsList.addAll(record.getTitles());
		resultsList.addAll(record.getTitleAlts());
		resultsList.addAll(record.getContents());
		return resultsList;
	}

	public String getDescriptionText() {
		StringBuilder result = new StringBuilder();
		List<String> descriptions = record.getDescriptions();

		if (descriptions == null) {
			return null;
		} else {
			for (String s : descriptions) {
				result.append(s);
			}
		}
		return result.toString();
	}

	public String getPolicy() {
		return dcMetadataRecord.getPolicyKramerius();
	}

	public List<String> getISBNs() {
		List<Isbn> isbns = dcMetadataRecord.getISBNs();
		List<String> isbnsS = new ArrayList<>();

		for (Isbn n : isbns) {
			String isbn = n.getIsbn().toString();
			isbnsS.add(isbn);
		}
		return isbnsS;
	}

	public List<String> getISSNs() {
		List<String> result = new ArrayList<>();
		for (Issn issn : dcMetadataRecord.getISSNs()) {
			result.add(issn.getIssn());
		}
		return result;
	}

	public Object methodMissing(String methodName, Object args) {
		RecordFunction<DublinCoreFunctionContext> func = functions.get(methodName);
		if (func == null) {
			throw new IllegalArgumentException(String.format("missing function: %s", methodName));
		}
		return func.apply(dcContext, args);
	}

	public List<String> getStatuses() {
		List<String> statuses = dcContext.metadataRecord().getDefaultStatuses();
		if (statuses != null && !statuses.isEmpty()) return statuses;
		return SolrUtils.createHierarchicFacetValues(Constants.DOCUMENT_AVAILABILITY_ONLINE, getRights());
	}

	public List<String> getUrls() {
		return dcMetadataRecord.getUrls();
	}

	public List<String> getPhysicals() {
		return record.getPhysicals();
	}

	public List<String> getContents() {
		return record.getContents();
	}

	public boolean getIndexWhenMerged() {
		return dcMetadataRecord.getIndexWhenMerged();
	}

	public List<String> getBarcodes() {
		return dcMetadataRecord.getBarcodes();
	}

	public List<String> getFormat() {
		return SolrUtils.createRecordTypeHierarchicFacet(dcMetadataRecord.getDetectedFormatList());
	}

	public List<String> getInstitutionFacet() {
		return SolrUtils.getInstitution(dcContext.harvestedRecord().getHarvestedFrom());
	}

	public Set<String> getAllCreatorsForSearching() {
		Set<String> results = new HashSet<>();
		results.addAll(dcContext.record().getCreators());
		results.addAll(dcContext.record().getContributors());
		return results.stream().map(String::toLowerCase).collect(Collectors.toSet());
	}

	public Set<String> getAllTitlesForSeraching() {
		return dcContext.record().getTitles().stream().map(String::toLowerCase).collect(Collectors.toSet());
	}

	public Set<String> getOclcs() {
		return dcContext.metadataRecord().getOclcs().stream().map(Oclc::getOclcStr).collect(Collectors.toSet());
	}

	public List<String> getLanguages() {
		return record.getLanguages();
	}

}
