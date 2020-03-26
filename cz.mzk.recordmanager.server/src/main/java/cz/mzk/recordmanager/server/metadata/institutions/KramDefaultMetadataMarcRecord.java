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
		return generateUrl(kramUrlBase, getPolicyKramerius());
	}

	public List<String> generateUrl(String kramUrlBase, String policy) {
		return generateUrl(kramUrlBase, policy, URL_COMMENT);
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

}
