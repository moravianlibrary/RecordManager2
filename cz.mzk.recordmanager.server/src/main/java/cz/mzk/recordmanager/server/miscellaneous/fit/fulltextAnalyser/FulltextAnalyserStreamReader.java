package cz.mzk.recordmanager.server.miscellaneous.fit.fulltextAnalyser;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FulltextAnalyserStreamReader {

	BufferedReader reader = null;

	private static final Pattern ID_OBALKY = Pattern.compile("=== ID zaznamu v souboru okcz_toc.xml ===");
	private static final Pattern ID_MZK = Pattern.compile("=== Odpovidajici ID zaznamu v souboru export_mzk.mrc ===");
	private static final Pattern ID_NKP = Pattern.compile("=== Odpovidajici ID zaznamu v souboru export_nkp.mrc ===");
	private static final Pattern NAMES = Pattern.compile("=== Nejvyznamnejsi nalezena jmena ===");
	private static final Pattern NAME = Pattern.compile("(.*)\\s+[0-9]+");

	/**
	 * Constructs an instance with the specified input stream.
	 */
	public FulltextAnalyserStreamReader(String fileName) throws FileNotFoundException {
		initializeReader(fileName);
	}

	private void initializeReader(String fileName) throws FileNotFoundException {
		reader = new BufferedReader(new FileReader(new File(fileName)));
	}

	/**
	 * Returns the next record in the iteration.
	 *
	 * @return FulltextAnalyser object
	 */
	public FulltextAnalyser next() {
		FulltextAnalyser result = new FulltextAnalyser();
		try {
			while (reader.ready()) {
				String line = reader.readLine();
				if (ID_OBALKY.matcher(line).matches()) result.setObalkyKnihId(reader.readLine());
				if (ID_MZK.matcher(line).matches()) result.setMzkId(reader.readLine());
				if (ID_NKP.matcher(line).matches()) result.setNkpId(reader.readLine());
				if (NAMES.matcher(line).matches()) {
					while (reader.ready()) {
						Matcher matcher = NAME.matcher(reader.readLine());
						if (matcher.matches()) result.addName(matcher.group(1));
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

}
