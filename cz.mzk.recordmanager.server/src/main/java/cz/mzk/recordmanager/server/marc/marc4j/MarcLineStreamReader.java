package cz.mzk.recordmanager.server.marc.marc4j;

import org.marc4j.Constants;
import org.marc4j.MarcReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;

import java.io.*;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarcLineStreamReader implements MarcReader {

	private BufferedReader br;

	private static final MarcFactory factory = MarcFactoryImpl.newInstance();
	;

	private static final Pattern DATA_SPLITTER = Pattern.compile("\\$+");
	private static final Pattern LINE_SPLITTER = Pattern.compile("\n");
	private static final Pattern LDR_PATTERN = Pattern.compile("^(LDR|LEADER) (.*)$");

	/**
	 * Constructs an instance with the specified input stream.
	 */
	public MarcLineStreamReader(InputStream input) {
		this(input, null);
	}

	/**
	 * Constructs an instance with the specified input stream.
	 */
	public MarcLineStreamReader(InputStream input, String encoding) {
		br = new BufferedReader(new InputStreamReader(
				new DataInputStream((input.markSupported()) ? input
						: new BufferedInputStream(input))));
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
			Record rec = null;
			String newLine;
			while (br.ready()) {
				newLine = br.readLine();
				if (newLine.isEmpty()) return rec;
				if (LDR_PATTERN.matcher(newLine).find()) rec = factory.newRecord();
				parseLine(rec, newLine);
			}
			return rec;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void parseLine(Record record, String strRecord) {
		String[] arrayRec = LINE_SPLITTER.split(strRecord);
		for (String line : arrayRec) {
			String tag = line.substring(0, 3);
			Matcher matcher = LDR_PATTERN.matcher(line);
			if (matcher.find()) {
				record.setLeader(factory.newLeader(matcher.group(2)));
			} else if (Constants.CF_TAG_PATTERN.matcher(tag).find()) {
				record.addVariableField(factory.newControlField(tag, line.substring(4)));
			} else {
				char ind1 = line.charAt(4);
				char ind2 = line.charAt(5);
				String data = line.substring(6);
				if (!data.isEmpty() && data.charAt(0) == '$') {
					DataField df = factory.newDataField(tag, ind1, ind2);
					record.addVariableField(parseDataField(df, data));
				} else {
					record.addVariableField(factory.newControlField(tag, line.substring(4)));
				}
			}
		}
	}

	public static DataField parseDataField(DataField df, String data) {
		String[] a = DATA_SPLITTER.split(data);
		for (String sbstr : Arrays.copyOfRange(a, 1, a.length)) {
			df.addSubfield(factory.newSubfield(sbstr.charAt(0), sbstr.substring(1)));
		}
		return df;
	}
}
