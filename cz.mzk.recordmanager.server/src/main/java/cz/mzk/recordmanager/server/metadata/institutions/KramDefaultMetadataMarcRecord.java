package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.KrameriusConfiguration;
import cz.mzk.recordmanager.server.model.Uuid;
import cz.mzk.recordmanager.server.oai.dao.KrameriusConfigurationDAO;
import cz.mzk.recordmanager.server.util.MetadataUtils;
import cz.mzk.recordmanager.server.util.constants.EVersionConstants;
import org.marc4j.marc.DataField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KramDefaultMetadataMarcRecord extends MetadataMarcRecord {

	@Autowired
	private KrameriusConfigurationDAO krameriusConfigurationDAO;

	private static final Logger logger = LoggerFactory.getLogger(KramDefaultMetadataMarcRecord.class);

	private static final Pattern UUID = Pattern.compile("uuid:(.*)");

	public KramDefaultMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public String getUUId() {
		Matcher matcher;
		if (harvestedRecord != null && (matcher = UUID.matcher(harvestedRecord.getUniqueId().getRecordId())).matches()) {
			return matcher.group(1);
		}
		for (DataField df : underlayingMarc.getDataFields("OAI")) {
			if (df.getSubfield('a') != null && (matcher = UUID.matcher(df.getSubfield('a').getData())).matches()) {
				return matcher.group(1);
			}
		}
		return null;
	}

	@Override
	public String getAuthorString() {
		String author = super.getAuthorString();
		return author == null ? underlayingMarc.getField("720", 'a') : author;
	}

	@Override
	public List<String> getUrls() {
		KrameriusConfiguration config = krameriusConfigurationDAO.get(harvestedRecord.getHarvestedFrom().getId());
		if (config == null) {
			logger.error("KrameriusConfig does not exists for record {}", harvestedRecord.getUniqueId());
			return Collections.emptyList();
		}
		return generateUrl(config.getAvailabilityDestUrl());
	}

	public List<String> generateUrl(String kramUrlBase) {
		return generateUrl(kramUrlBase, getPolicyKramerius());
	}

	public List<String> generateUrl(String kramUrlBase, String policy) {
		return generateUrl(kramUrlBase, policy, EVersionConstants.DIGITIZED_LINK);
	}

	public List<String> generateUrl(String kramUrlBase, String policy, String comment) {
		return generateUrl(harvestedRecord.getHarvestedFrom().getIdPrefix(), kramUrlBase, policy, comment);
	}

	public List<String> generateUrl(String source, String kramUrlBase, String policy, String comment) {
		return Collections.singletonList(MetadataUtils.generateUrl(source, policy,
				kramUrlBase + harvestedRecord.getUniqueId().getRecordId(), comment));
	}

	@Override
	public boolean getIndexWhenMerged() {
		return false;
	}

	@Override
	public List<Uuid> getUuids() {
		Set<Uuid> results = new HashSet<>();
		if (harvestedRecord != null) {
			Matcher matcher = UUID_PATTERN.matcher(harvestedRecord.getUniqueId().getRecordId());
			if (matcher.find()) {
				results.add(Uuid.create(matcher.group(0)));
			}
		}
		for (DataField df : underlayingMarc.getDataFields("OAI")) {
			if (df.getSubfield('a') != null) {
				results.add(Uuid.create(df.getSubfield('a').getData()));
			}
		}
		return new ArrayList<>(results);
	}

	@Override
	public String getKrameriusRecordId() {
		if (harvestedRecord != null) {
			return harvestedRecord.getUniqueId().getRecordId();
		}
		for (DataField df : underlayingMarc.getDataFields("OAI")) {
			if (df.getSubfield('a') != null) {
				return df.getSubfield('a').getData();
			}
		}
		return null;
	}

	/**
	 * @return boolean
	 */
	@Override
	public boolean isDigitized() {
		return true;
	}
}
