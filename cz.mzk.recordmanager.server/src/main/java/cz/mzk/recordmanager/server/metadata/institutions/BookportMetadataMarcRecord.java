package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.MetadataUtils;
import org.marc4j.marc.DataField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BookportMetadataMarcRecord extends MetadataMarcRecord {

	private static final String COMMENT = "Registrovaní uživatelé knihovny získají knihu po přihlášení přes eduID.cz na Bookportu";

	private static final List<String> SOURCES = Arrays.asList("ntk", "nlk", "knav", "vkol");

	public BookportMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public List<String> getUrls() {
		List<String> results = new ArrayList<>();
		for (DataField df : underlayingMarc.getDataFields("856")) {
			if (df.getSubfield('u') != null) {
				for (String source : SOURCES) {
					results.add(MetadataUtils.generateUrl(source,
							Constants.DOCUMENT_AVAILABILITY_ONLINE,
							df.getSubfield('u').getData(), COMMENT));
				}
			}
		}
		return results;
	}
}
