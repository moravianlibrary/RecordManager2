package cz.mzk.recordmanager.server.marc.marc4j;

import org.marc4j.MarcReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarcAlephStreamReader implements MarcReader {

	private static final Pattern ID_PATTERN = Pattern.compile("^([^ ]*) .*$");
	private static final Pattern LDR_PATTERN = Pattern.compile("^[^ ]* LDR {3}L (.*)$");
	private static final Pattern CF_PATTERN = Pattern.compile("^[^ ]* ([\\w]{3}) {3}L ((?!\\$\\$).*)$");
	private static final Pattern DF_PATTERN = Pattern.compile("^[^ ]* ([\\w]{3})(.)(.) L (.*)$");
	private BufferedReader br;
	private MarcFactory factory;
	private String line = null;
	private String lastRecordId = null;

	/**
	 * Constructs an instance with the specified input stream.
	 */
	public MarcAlephStreamReader(InputStream input) {
		this(input, null);
	}

	/**
	 * Constructs an instance with the specified input stream.
	 */
	public MarcAlephStreamReader(InputStream input, String encoding) {
		br = new BufferedReader(new InputStreamReader(
				new DataInputStream((input.markSupported()) ? input
						: new BufferedInputStream(input))));
		factory = MarcFactoryImpl.newInstance();
	}

	/**
	 * Returns true if the iteration has more records, false otherwise.
	 */
	public boolean hasNext() {
		try {
			return br.ready();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Returns the next record in the iteration.
	 *
	 * @return Record - the record object
	 */
	public Record next() {
		return nextRecord();
	}

	private Record nextRecord() {
		try {
			Record rec = factory.newRecord();
			Matcher matcher;

			parseLine(rec);

			while ((line = br.readLine()) != null) {
				if ((matcher = ID_PATTERN.matcher(line)).matches()) {
					if (lastRecordId == null) { // get first record ID
						lastRecordId = matcher.group(1);
					} else if (!matcher.group(1).equals(lastRecordId)) {
						lastRecordId = matcher.group(1);
						return rec;
					}
				}
				parseLine(rec);
			}
			return rec;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void parseLine(Record record) {
		if (line == null || line.isEmpty()) return;
		Matcher matcher;
		if ((matcher = LDR_PATTERN.matcher(line)).matches()) {
			record.setLeader(factory.newLeader(matcher.group(1)));
		} else if ((matcher = CF_PATTERN.matcher(line)).matches()) {
			record.addVariableField(factory.newControlField(matcher.group(1), matcher.group(2)));
		} else if ((matcher = DF_PATTERN.matcher(line)).matches()) {
			DataField df = factory.newDataField(matcher.group(1), matcher.group(2).charAt(0), matcher.group(3).charAt(0));
			for (String data : matcher.group(4).split("\\$\\$")) {
				if (data.length() > 1) { // sf code (1 char) + text
					df.addSubfield(factory.newSubfield(data.charAt(0), data.substring(1)));
				}
			}
			record.addVariableField(df);
		}
	}

}
