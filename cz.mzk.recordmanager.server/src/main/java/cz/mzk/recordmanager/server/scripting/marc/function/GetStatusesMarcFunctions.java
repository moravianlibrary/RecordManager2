package cz.mzk.recordmanager.server.scripting.marc.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.scripting.marc.MarcFunctionContext;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.SolrUtils;

@Component
public class GetStatusesMarcFunctions implements MarcRecordFunctions {

	protected final static String ABSENT = "a";

	protected final static String PRESENT = "p";
	
	protected final static String ONLINE = "online";
	protected final static Pattern ONLINE_PATTERN = Pattern.compile("(?i).*"+ONLINE+".*");
	
	protected final static String UNKNOWN = "nz";
	protected final static String UNSPECIFIED = "n";
	protected final static String LIMITED = "o";
	protected final static String TEMPORARY = "d";
	protected final static String FREESTACK = "0";

	public List<String> getStatuses(MarcFunctionContext ctx) {
		MarcRecord record = ctx.record();
		List<String> statuses = new ArrayList<String>();
		statuses.addAll(getStatuses(record, "996"));
		statuses.addAll(getStatusFrom856(record, "856"));	
		return statuses;
	}

	private List<String> getStatusFrom856(MarcRecord record, String statusField) {
		List<String> result = new ArrayList<>();
		for (DataField field: record.getDataFields(statusField)) {
			if (field.getIndicator1() == '4' && (field.getIndicator2() == '0' || field.getIndicator2() =='1')) {
				result.addAll(SolrUtils.createHierarchicFacetValues(ONLINE, Constants.DOCUMENT_AVAILABILITY_UNKNOWN));
			}
		}
		return result;
	}

	private List<String> getStatuses(MarcRecord record, String statusField) {
		List<DataField> fields = record.getAllFields().get(statusField);
		if (fields == null || fields.isEmpty()) {
			return Collections.emptyList();
		}
		List<String> statuses = new ArrayList<String>();
		boolean present = false;
		boolean absent = false;
		boolean unknown = false;
		boolean unspecified = false;
		boolean limited = false;
		boolean temporary = false;
		boolean freestack = false;
		for (DataField field : fields) {
			Subfield s = field.getSubfield('s');
			if (s == null) {
				continue;
			}
			String statusInS = s.getData().toLowerCase();
			switch (statusInS) {
			case ABSENT:
				absent = true;
				break;
			case PRESENT:
				present = true;
				break;
			case UNKNOWN:
				unknown = true;
				break;
			case UNSPECIFIED:
				unspecified = true;
				break;
			case LIMITED:
				limited = true;
				break;
			case TEMPORARY:
				temporary = true;
				break;
			}
			
			s = field.getSubfield('a');
			if (s == null) {
				continue;
			}
			String statusInA = s.getData().toLowerCase();
			switch (statusInA) {
			case FREESTACK:
				freestack = true;
				break;
			}
		}
		if (absent) {
			statuses.addAll(SolrUtils.createHierarchicFacetValues("absent"));
		}
		if (present) {
			statuses.addAll(SolrUtils.createHierarchicFacetValues("present"));
		}
		if (unknown) {
			statuses.addAll(SolrUtils.createHierarchicFacetValues("unknown"));
		}
		if (unspecified) {
			statuses.addAll(SolrUtils.createHierarchicFacetValues("unspecified"));
		}
		if (limited) {
			statuses.addAll(SolrUtils.createHierarchicFacetValues("limited"));
		}
		if (temporary) {
			statuses.addAll(SolrUtils.createHierarchicFacetValues("temporary"));
		}
		if (freestack) {
			statuses.addAll(SolrUtils.createHierarchicFacetValues("freestack"));
		}
		return statuses;
	}

}
