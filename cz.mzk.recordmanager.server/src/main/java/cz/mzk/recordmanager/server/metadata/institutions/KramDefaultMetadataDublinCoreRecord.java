package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.metadata.MetadataDublinCoreRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.KrameriusConfiguration;
import cz.mzk.recordmanager.server.model.Uuid;
import cz.mzk.recordmanager.server.oai.dao.KrameriusConfigurationDAO;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.MetadataUtils;
import cz.mzk.recordmanager.server.util.constants.EVersionConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KramDefaultMetadataDublinCoreRecord extends
		MetadataDublinCoreRecord {

	@Autowired
	private KrameriusConfigurationDAO krameriusConfigurationDAO;

	private static Logger logger = LoggerFactory.getLogger(KramDefaultMetadataDublinCoreRecord.class);

	private static final Pattern UUID_PATTERN = Pattern.compile("uuid:[\\w-]+");

	private static final Pattern PUBLIC_RIGHTS_PATTERN = Pattern.compile(".*public.*");

	public KramDefaultMetadataDublinCoreRecord(DublinCoreRecord dcRecord, HarvestedRecord hr) {
		super(dcRecord, hr);
	}

	@Override
	public List<String> getUrls() {
		return generateUrl();
	}

	public List<String> generateUrl() {
		KrameriusConfiguration config = krameriusConfigurationDAO.get(harvestedRecord.getHarvestedFrom().getId());
		if (config == null) {
			logger.error("KrameriusConfig does not exists for record {}", harvestedRecord.getUniqueId());
			return Collections.emptyList();
		}
		String policy = dcRecord.getRights().stream()
				.anyMatch(s -> PUBLIC_RIGHTS_PATTERN.matcher(s).matches())
				? Constants.DOCUMENT_AVAILABILITY_ONLINE
				: Constants.DOCUMENT_AVAILABILITY_PROTECTED;
		return Collections.singletonList(MetadataUtils.generateUrl(harvestedRecord.getHarvestedFrom().getIdPrefix(),
				policy, config.getAvailabilityDestUrl() + harvestedRecord.getUniqueId().getRecordId(),
				EVersionConstants.DIGITIZED_LINK));

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

	@Override
	public boolean getIndexWhenMerged() {
		return false;
	}

	@Override
	public String getKrameriusRecordId() {
		return harvestedRecord.getUniqueId().getRecordId();
	}

	/**
	 * @return boolean
	 */
	@Override
	public boolean isDigitized() {
		return true;
	}
}
