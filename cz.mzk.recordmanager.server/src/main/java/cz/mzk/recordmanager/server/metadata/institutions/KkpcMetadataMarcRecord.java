package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import org.marc4j.marc.DataField;

import java.util.regex.Matcher;

public class KkpcMetadataMarcRecord extends MetadataMarcRecord {

	public KkpcMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public String getPalmknihyId() {
		Matcher matcher;
		for (String url : underlayingMarc.getFields("996", 'd')) {
			matcher = PALMKNIHY_ID.matcher(url);
			if (matcher.matches()) return matcher.group(1);
		}
		return null;
	}

	@Override
	public boolean matchFilter() {
		if (!super.matchFilter()) return false;
		int ebooks = 0;
		int others = 0;
		for (DataField df : underlayingMarc.getDataFields("996")) {
			if (df.getSubfield('d') != null && EBOOKS_URL.matcher(df.getSubfield('d').getData()).find()) ebooks++;
			else others++;
		}
		return ebooks == 0 || others > 0;
	}

}
