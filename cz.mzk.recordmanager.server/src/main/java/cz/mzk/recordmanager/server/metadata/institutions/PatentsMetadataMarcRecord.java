package cz.mzk.recordmanager.server.metadata.institutions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import org.marc4j.marc.DataField;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.CitationRecordType;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.SolrUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class PatentsMetadataMarcRecord extends MetadataMarcRecord{

	@Autowired
	private HarvestedRecordDAO hrDao;

	private Pattern IPC = Pattern.compile("MPT|IPC", Pattern.CASE_INSENSITIVE);
	private static final Pattern APPLICATION_PATTERN = Pattern.compile("Číslo přihlášky: (.*)");
	private static final Pattern DOCNUMBER_PATTERN = Pattern.compile("St36_CZ_([^_]*)_A3");
	
	public PatentsMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}
	
	@Override
	public List<HarvestedRecordFormatEnum> getDetectedFormatList() {
		String f001 = underlayingMarc.getControlField("001");
		if (f001 == null) return Collections.emptyList();
		if (f001.endsWith("_B6")) {
			return Collections.singletonList(HarvestedRecordFormatEnum.PATENTS_PATENTS);
		}
		if (f001.endsWith("_A3")) {
			return Collections.singletonList(HarvestedRecordFormatEnum.PATENTS_PATENT_APPLICATIONS);
		}
		if (f001.endsWith("_U1")) {
			return Collections.singletonList(HarvestedRecordFormatEnum.PATENTS_UTILITY_MODELS);
		}
		return Collections.emptyList();	
	}

	@Override
	public List<String> getUrls() {
		return getUrls(Constants.DOCUMENT_AVAILABILITY_ONLINE);
	}

	@Override
	public List<String> getDefaultStatuses() {
		return SolrUtils.createHierarchicFacetValues(Constants.DOCUMENT_AVAILABILITY_ONLINE, Constants.DOCUMENT_AVAILABILITY_ONLINE);
	}
	
	@Override
	public CitationRecordType getCitationFormat() {
		return CitationRecordType.PATENT;
	}

	@Override
	public List<String> getInternationalPatentClassfication() {
		List<String> results = new ArrayList<>();
		for (DataField df : underlayingMarc.getDataFields("024")) {
			if (df.getSubfield('2') != null
					&& IPC.matcher(df.getSubfield('2').getData()).matches()) {
				if (df.getSubfield('a') != null) {
					results.add(df.getSubfield('a').getData());
				}
			}
		}
		return results;
	}

	@Override
	public Boolean getMetaproxyBool() {
		return false;
	}

	@Override
	public String getUpvApplicationId() {
		String f001 = underlayingMarc.getControlField("001");
		if (f001 == null) return null;
		if (f001.endsWith("_B6")) {
			return getApplicationId();
		}
		return null;
	}

	@Override
	public boolean matchFilter() {
		if (!super.matchFilter()) return false;

		String f001 = underlayingMarc.getControlField("001");
		if (f001 == null) return true;

		if (f001.endsWith("_A3")) {
			if (hrDao.existsUpvApplicationId(getDocNumber())) return false;
		}
		if (f001.endsWith("_B6")) {
			String id = getApplicationId();
			if (id != null) hrDao.deleteUpvApplicationRecord("St36_CZ_"+id+"_A3");
		}

		return true;
	}

	protected String getApplicationId() {
		for (String data : underlayingMarc.getFields("500", 'a')) {
			Matcher matcher = APPLICATION_PATTERN.matcher(data);
			if (matcher.matches()) {
				return matcher.group(1);
			}
		}
		return null;
	}

	protected String getDocNumber() {
		String data = underlayingMarc.getControlField("001");
		Matcher matcher = DOCNUMBER_PATTERN.matcher(data);
		if (matcher.matches()) {
			return matcher.group(1);
		}

		return null;
	}

}
