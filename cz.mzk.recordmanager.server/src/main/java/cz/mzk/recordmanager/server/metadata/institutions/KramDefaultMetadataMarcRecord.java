package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.util.MetadataUtils;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KramDefaultMetadataMarcRecord extends
		MetadataMarcRecord {

	private static final Pattern UUID = Pattern.compile("uuid:(.*)");
	private static final String URL_COMMENT = "Digitalizovaný dokument";

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

	public List<String> generateUrl(String kramUrlBase) {
		return Collections.singletonList(MetadataUtils.generateUrl(harvestedRecord.getHarvestedFrom().getIdPrefix(),
				getPolicyKramerius(), kramUrlBase + harvestedRecord.getUniqueId().getRecordId(), URL_COMMENT));
	}

	@Override
	public boolean getIndexWhenMerged() {
		return false;
	}

}
