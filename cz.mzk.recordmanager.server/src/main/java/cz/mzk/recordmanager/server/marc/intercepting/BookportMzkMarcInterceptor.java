package cz.mzk.recordmanager.server.marc.intercepting;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.marc.marc4j.MarcFactoryImpl;
import cz.mzk.recordmanager.server.marc.marc4j.RecordImpl;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookportMzkMarcInterceptor extends DefaultMarcInterceptor {

	private static final Pattern URL_PATTERN = Pattern.compile("(.*)(/kniha/.*)");

	private static final String COMMENT = "Registrovaní uživatelé knihovny získají knihu po přihlášení přes eduID.cz na Bookportu";

	public BookportMzkMarcInterceptor(Record record, ImportConfiguration configuration, String recordId) {
		super(record, configuration, recordId);
	}

	@Override
	public byte[] intercept() {
		if (super.getRecord() == null) {
			return new byte[0];
		}

		MarcRecord marc = new MarcRecordImpl(super.getRecord());
		Record newRecord = new RecordImpl();
		MarcFactory marcFactory = new MarcFactoryImpl();

		newRecord.setLeader(getRecord().getLeader());
		for (ControlField cf : super.getRecord().getControlFields()) {
			newRecord.addVariableField(cf);
		}

		Map<String, List<DataField>> dfMap = marc.getAllFields();
		for (String tag : new TreeSet<String>(dfMap.keySet())) { // sorted tags
			for (DataField df : dfMap.get(tag)) {
				// kill fields 996l = VF
				if (df.getTag().equals("856")) {
					Matcher matcher = URL_PATTERN.matcher(df.getSubfield('u').getData());
					if (matcher.matches()) {
						String url = matcher.group(1) + "/AccountSaml/SignIn/?idp=https%3A%2F%2Fshibboleth.mzk.cz%2Fsimplesaml%2Fmetadata.xml&returnUrl=" + matcher.group(2);
						newRecord.addVariableField(marcFactory.newDataField("856", ' ', ' ', "u", url, "y", COMMENT));
					}
				} else newRecord.addVariableField(df);
			}
		}

		return new MarcRecordImpl(newRecord).export(IOFormat.XML_MARC).getBytes(StandardCharsets.UTF_8);
	}

}
