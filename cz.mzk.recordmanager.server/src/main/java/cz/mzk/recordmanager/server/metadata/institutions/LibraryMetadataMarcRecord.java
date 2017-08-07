package cz.mzk.recordmanager.server.metadata.institutions;

import java.util.regex.Pattern;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;

public class LibraryMetadataMarcRecord extends MetadataMarcRecord {

	private static final Pattern LIBRARY_CLOSED = Pattern
			.compile("KNIHOVNA ZRUŠENA!");
	private static final Pattern INSTITUTION_CLOSED = Pattern
			.compile("ZRUŠENÁ INSTITUCE!");

	public LibraryMetadataMarcRecord(MarcRecord underlayingMarc) {
		super(underlayingMarc);
	}

	@Override
	public boolean matchFilter() {
		if (underlayingMarc.getDataFields("DEL") != null
				&& !underlayingMarc.getDataFields("DEL").isEmpty()) {
			return false;
		}
		for (DataField df : underlayingMarc.getDataFields("STT")) {
			Subfield sf;
			if ((sf = df.getSubfield('a')) != null
					&& (LIBRARY_CLOSED.matcher(sf.getData()).matches() || INSTITUTION_CLOSED
							.matcher(sf.getData()).matches())) {
				return false;
			}
		}

		return true;
	}
}
