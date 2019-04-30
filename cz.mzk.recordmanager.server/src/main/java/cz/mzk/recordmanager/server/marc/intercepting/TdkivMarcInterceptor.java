package cz.mzk.recordmanager.server.marc.intercepting;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.mzk.recordmanager.server.util.RecordUtils;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.marc.marc4j.MarcFactoryImpl;
import cz.mzk.recordmanager.server.marc.marc4j.RecordImpl;

public class TdkivMarcInterceptor extends DefaultMarcInterceptor {

	private static final Pattern RECORD_ID = Pattern.compile("doc_number=([0-9]*)");
	private static final String DATE_STRING_005 = "yyyyMMddHHmmss'.0'";
	private static final String TEXT_856y = "heslo v České terminologické databázi knihovnictví a informační vědy";
	private static final HashMap<String, String> TAGS = new HashMap<>();

	static {
		TAGS.put("TER", "150");
		TAGS.put("EKV", "450");
		TAGS.put("PTE", "550");
		TAGS.put("ZDR", "650");
		TAGS.put("VYK", "678");
		TAGS.put("AUT", "AUT");
	}

	public TdkivMarcInterceptor(Record record) {
		super(record);
	}

	@Override
	public byte[] intercept() {
		if (super.getRecord() == null) {
			return new byte[0];
		}

		Record newRecord = new RecordImpl();
		MarcFactory marcFactory = new MarcFactoryImpl();

		newRecord.addVariableField(marcFactory.newControlField("FMT", "VA"));
		newRecord.addVariableField(marcFactory.newControlField("003", "CZ-PrNK"));
		newRecord.addVariableField(marcFactory.newControlField("005", new SimpleDateFormat(DATE_STRING_005).format(new Date())));
		newRecord.addVariableField(marcFactory.newControlField("008", "000804|n|anznnbabn-----------n-a|a------"));
		newRecord.addVariableField(marcFactory.newDataField("040", ' ', ' ', "a", "ABA001", "b", "cze", "d", "ABA001"));

		newRecord.setLeader(getRecord().getLeader());
		for (ControlField cf : super.getRecord().getControlFields()) {
			newRecord.addVariableField(cf);
		}
		for (DataField df : super.getRecord().getDataFields()) {
			if (df.getTag().equals("DRL") && df.getSubfield('a') != null && newRecord.getControlNumberField() == null) {
				Matcher matcher = RECORD_ID.matcher(df.getSubfield('a').getData());
				if (matcher.find()) {
					newRecord.addVariableField(marcFactory.newControlField("001", matcher.group(1)));
				}
			}

			if (TAGS.containsKey(df.getTag())) {
				df.setTag(TAGS.get(df.getTag()));
			} else if (df.getTag().equals("ANG")) {
				df.setTag("750");
				df.setIndicator1('0');
				df.setIndicator2('7');
			} else if (df.getTag().equals("DRL")) {
				df = marcFactory.newDataField("856", '4', '1', "u", df.getSubfield('a').getData(), "y", TEXT_856y);
			} else continue;
			newRecord.addVariableField(df);
		}

		return new MarcRecordImpl(RecordUtils.sortFields(newRecord)).export(IOFormat.XML_MARC).getBytes(StandardCharsets.UTF_8);
	}

}
