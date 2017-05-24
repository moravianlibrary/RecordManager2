package cz.mzk.recordmanager.server.metadata.institutions;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.metadata.MetadataDublinCoreRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.KrameriusConfiguration;
import cz.mzk.recordmanager.server.oai.dao.KrameriusConfigurationDAO;
import cz.mzk.recordmanager.server.util.Constants;

public class KramDefaultMetadataDublinCoreRecord extends
		MetadataDublinCoreRecord {

	@Autowired
	private KrameriusConfigurationDAO krameriusConfiguationDao;

	public KramDefaultMetadataDublinCoreRecord(DublinCoreRecord dcRecord) {
		super(dcRecord);
	}

	public KramDefaultMetadataDublinCoreRecord(DublinCoreRecord dcRecord,
			HarvestedRecord hr) {
		super(dcRecord, hr);
	}

	@Override
	public List<String> getUrls() {
		return super.getUrls();
	}

	public List<String> getUrlsByImportConfId(Long importConfId) {
		String kramUrl = null;
		String kramUrlBase = null;
		KrameriusConfiguration kramConf = krameriusConfiguationDao
				.get(importConfId);
		if (kramConf != null && kramConf.getUrl() != null) {
			kramUrlBase = Pattern.compile("api/v\\d\\.\\d")
					.matcher(kramConf.getUrl()).replaceAll("")
					+ "i.jsp?pid=";
		}

		String policy = dcRecord.getRights().stream()
				.anyMatch(s -> s.matches(".*public.*")) ? Constants.DOCUMENT_AVAILABILITY_ONLINE
				: Constants.DOCUMENT_AVAILABILITY_PROTECTED;
		kramUrl = policy + "|" + kramUrlBase
				+ harvestedRecord.getUniqueId().getRecordId() + "|";
		return Collections.singletonList(kramUrl);

	}

}
