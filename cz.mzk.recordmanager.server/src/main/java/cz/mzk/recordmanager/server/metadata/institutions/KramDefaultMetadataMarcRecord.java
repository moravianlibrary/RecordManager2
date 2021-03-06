package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.KrameriusConfiguration;
import cz.mzk.recordmanager.server.model.Uuid;
import cz.mzk.recordmanager.server.oai.dao.KrameriusConfigurationDAO;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.MetadataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KramDefaultMetadataMarcRecord extends
		MetadataMarcRecord {

	@Autowired
	private KrameriusConfigurationDAO krameriusConfigurationDAO;

	private static Logger logger = LoggerFactory.getLogger(KramDefaultMetadataMarcRecord.class);

	private static final Pattern UUID = Pattern.compile("uuid:(.*)");

	public KramDefaultMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public String getUUId() {
		Matcher matcher;
		if ((matcher = UUID.matcher(harvestedRecord.getUniqueId().getRecordId())).matches()) {
			return matcher.group(1);
		}
		return null;
	}

	@Override
	public String getAuthorString() {
		String author = super.getAuthorString();
		return author == null ? underlayingMarc.getField("720", 'a') : author;
	}

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
		return generateUrl(kramUrlBase, policy, Constants.KRAM_EVERSION_COMMENT);
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
		Matcher matcher = UUID_PATTERN.matcher(harvestedRecord.getUniqueId().getRecordId());
		if (matcher.find()) {
			results.add(Uuid.create(matcher.group(0)));
		}
		return new ArrayList<>(results);
	}
}
