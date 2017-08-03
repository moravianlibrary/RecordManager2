package cz.mzk.recordmanager.server.miscellaneous.skat;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SkatIdsStreamReader {

	private BufferedReader br;

	private static final Pattern ID_PATTERN = Pattern.compile("[0-9]{9}");

	private static final String SKC_ID_PREFIX = "SKC01-";

	/**
	 * Constructs an instance with the specified input stream.
	 */
	public SkatIdsStreamReader(InputStream input) {
		br = new BufferedReader(new InputStreamReader(new DataInputStream(
				(input.markSupported()) ? input
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
	public String next() {
		return nextId();
	}

	private String nextId() {
		try {
			String id = null;
			String newLine;
			while (br.ready()) {
				newLine = br.readLine();
				id = parseLine(newLine);
				if (id != null) return id;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String parseLine(String line) {
		Matcher matcher = ID_PATTERN.matcher(line);
		if (matcher.find()) {
			return SKC_ID_PREFIX + matcher.group(0);
		}
		return null;
	}

}
