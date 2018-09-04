package cz.mzk.recordmanager.server.marc.intercepting;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.marc.marc4j.RecordImpl;
import org.marc4j.marc.*;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class CelitebibMarcInterceptor extends DefaultMarcInterceptor {

	private static final Pattern FIELD_START = Pattern.compile("^[167]..$");
	private static final String TAG_773 = "773";
	private static final String TAG_964 = "964";
	private static final String TAG_CAT = "CAT";
	private static final String TAG_KAT = "KAT";
	private static final char CODE_X = 'x';

	public CelitebibMarcInterceptor(Record record) {
		super(record);
	}

	@Override
	public byte[] intercept() {
		if (super.getRecord() == null) {
			return new byte[0];
		}
		Record newRecord = new RecordImpl();
		newRecord.setLeader(getRecord().getLeader());
		for (ControlField cf : super.getRecord().getControlFields()) {
			newRecord.addVariableField(cf);
		}

		for (DataField df : super.getRecord().getDataFields()) {
			// remove subfield 'x' from fields 1xx, 6xx, 7xx (but not from 773)
			if (FIELD_START.matcher(df.getTag()).matches() && !df.getTag().equals(TAG_773)) {
				DataField newDf = MARC_FACTORY.newDataField(df.getTag(), df.getIndicator1(), df.getIndicator2());
				for (Subfield subfield : df.getSubfields()) {
					if (subfield.getCode() == CODE_X) continue;
					newDf.addSubfield(subfield);
				}
				newRecord.addVariableField(newDf);
				continue;
			}
			// remove field 964, CAT, KAT
			if (df.getTag().equals(TAG_964) || df.getTag().equals(TAG_CAT) || df.getTag().equals(TAG_KAT)) continue;
			// default
			newRecord.addVariableField(df);
		}

		return new MarcRecordImpl(newRecord).export(IOFormat.XML_MARC).getBytes(StandardCharsets.UTF_8);
	}

}
