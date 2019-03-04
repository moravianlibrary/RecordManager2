package cz.mzk.recordmanager.server.marc.intercepting;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Pattern;

import cz.mzk.recordmanager.server.util.CleaningUtils;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.marc.marc4j.RecordImpl;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import org.marc4j.marc.Subfield;

public class CbvkMarcInterceptor extends DefaultMarcInterceptor {

	public CbvkMarcInterceptor(Record record, ImportConfiguration configuration, String recordId) {
		super(record, configuration, recordId);
	}

	private static final Pattern URL_WITH_PORT_PATTERN = Pattern.compile("kramerius\\.cbvk\\.cz:8080");
	private static final String URL_WITHOUT_PORT = "kramerius.cbvk.cz";

	private static final char CODE_U = 'u';

	@Override
	public byte[] intercept() {
		if (super.getRecord() == null) {
			return new byte[0];
		}

		MarcRecord marc = new MarcRecordImpl(super.getRecord());
		Record newRecord = new RecordImpl();

		newRecord.setLeader(getRecord().getLeader());
		for (ControlField cf : super.getRecord().getControlFields()) {
			newRecord.addVariableField(cf);
		}

		Map<String, List<DataField>> dfMap = marc.getAllFields();
		for (String tag : new TreeSet<>(dfMap.keySet())) { // sorted tags
			for (DataField df : dfMap.get(tag)) {
				// remove field 520
				if (df.getTag().equals("520")) continue;
				if (df.getTag().equals("856")) {
					DataField newDf = MARC_FACTORY.newDataField(df.getTag(), df.getIndicator1(), df.getIndicator2());
					for (Subfield subfield : df.getSubfields()) {
						if (subfield.getCode() == CODE_U) {
							subfield.setData(CleaningUtils.replaceFirst(subfield.getData(), URL_WITH_PORT_PATTERN, URL_WITHOUT_PORT));
						}
						newDf.addSubfield(subfield);
					}
					newRecord.addVariableField(newDf);
				}
				processField996(df);
				newRecord.addVariableField(df);
			}
		}
		return new MarcRecordImpl(newRecord).export(IOFormat.XML_MARC).getBytes(StandardCharsets.UTF_8);
	}
}
