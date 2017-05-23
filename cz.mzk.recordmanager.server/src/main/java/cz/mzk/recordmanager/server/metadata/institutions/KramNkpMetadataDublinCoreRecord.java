package cz.mzk.recordmanager.server.metadata.institutions;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.oai.dao.ImportConfigurationDAO;
import cz.mzk.recordmanager.server.util.Constants;

public class KramNkpMetadataDublinCoreRecord extends
		KramDefaultMetadataDublinCoreRecord {

	@Autowired
	private ImportConfigurationDAO importConfigurationDAO;

	public KramNkpMetadataDublinCoreRecord(DublinCoreRecord dcRecord) {
		super(dcRecord);
	}

	@Override
	public List<String> getUrls() {
		List<ImportConfiguration> ics = importConfigurationDAO
				.findByIdPrefix(Constants.PREFIX_KRAM_NKP);
		if (ics != null && !ics.isEmpty()) {
			return getUrlsByImportConfId(ics.get(0).getId());
		}
		return Collections.emptyList();
	}

}
