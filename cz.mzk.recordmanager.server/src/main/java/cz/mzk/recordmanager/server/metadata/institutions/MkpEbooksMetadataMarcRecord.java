package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.MetadataUtils;
import org.marc4j.marc.DataField;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class MkpEbooksMetadataMarcRecord extends EbooksMetadataMarcRecord {

	private static final Pattern URL_Y_PATTERN = Pattern.compile("Plný text");
	private static final String URL_COMMENT = "free_%s_link";

	public MkpEbooksMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public List<String> getUrls() {
		List<String> results = new ArrayList<>();
		for (DataField df : underlayingMarc.getDataFields("856")) {
			if (df.getSubfield('y') != null
					&& URL_Y_PATTERN.matcher(df.getSubfield('y').getData()).find()
					&& df.getSubfield('u') != null
					&& df.getSubfield('q') != null) {
				results.add(MetadataUtils.generateUrl(harvestedRecord.getHarvestedFrom().getIdPrefix(),
						Constants.DOCUMENT_AVAILABILITY_ONLINE,
						df.getSubfield('u').getData(), String.format(URL_COMMENT, df.getSubfield('q').getData())));
			}
		}
		return results;
	}

	@Override
	public List<String> getDefaultStatuses() {
		return Collections.singletonList(Constants.DOCUMENT_AVAILABILITY_ONLINE);
	}

	@Override
	public boolean matchFilter() {
		if (!super.matchFilter()) return false;
		return !underlayingMarc.getDataFields("856").isEmpty();
	}

}
