package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.FulltextKrameriusDAO;
import cz.mzk.recordmanager.server.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class KramMzkMetadataMarcRecord extends KramDefaultMetadataMarcRecord {

	@Autowired
	private FulltextKrameriusDAO fulltextKrameriusDAO;

	public KramMzkMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public List<String> getUrls() {
		List<String> results = new ArrayList<>();
		String policy = fulltextKrameriusDAO.getPolicy(harvestedRecord.getId());

		results.addAll(generateUrl("http://www.digitalniknihovna.cz/mzk/uuid/", policy));
		if (Constants.DOCUMENT_AVAILABILITY_PROTECTED.equals(policy))
			results.addAll(generateUrl("https://kramerius-vs.mzk.cz/view/", policy));
		return results;
	}

}
