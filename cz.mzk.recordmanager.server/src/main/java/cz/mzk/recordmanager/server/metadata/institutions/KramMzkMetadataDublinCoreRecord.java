package cz.mzk.recordmanager.server.metadata.institutions;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.MetadataUtils;

public class KramMzkMetadataDublinCoreRecord extends
		KramDefaultMetadataDublinCoreRecord {

	private static final Pattern PUBLIC_RIGHTS_PATTERN = Pattern.compile(".*public.*");

	private static final String URL_FORMAT = "http://www.digitalniknihovna.cz/mzk/uuid/%s";

	public KramMzkMetadataDublinCoreRecord(DublinCoreRecord dcRecord) {
		super(dcRecord);
	}

	public KramMzkMetadataDublinCoreRecord(DublinCoreRecord dcRecord,
			HarvestedRecord hr) {
		super(dcRecord, hr);
	}

	@Override
	public List<String> getUrls() {
		String policy = dcRecord.getRights().stream()
				.anyMatch(s -> PUBLIC_RIGHTS_PATTERN.matcher(s).matches()) ? Constants.DOCUMENT_AVAILABILITY_ONLINE
				: Constants.DOCUMENT_AVAILABILITY_PROTECTED;
		return Collections.singletonList(MetadataUtils.generateUrl(harvestedRecord.getHarvestedFrom().getIdPrefix(),
				policy, String.format(URL_FORMAT, harvestedRecord.getUniqueId().getRecordId()), ""));
	}

	@Override
	public boolean getIndexWhenMerged() {
		return false;
	}

}
