package cz.mzk.recordmanager.server.miscellaneous.fit.semanticEnrichment;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SemanticEnrichmentStreamReader {

	BufferedReader reader = null;

	private static final Pattern RECORD_ID = Pattern.compile("(uuid:[^.]*)");

	private String recordId;

	/**
	 * Constructs an instance with the specified input stream.
	 */
	public SemanticEnrichmentStreamReader(String fileName) throws FileNotFoundException {
		initializeReader(fileName);
		Matcher matcher = RECORD_ID.matcher(fileName);
		if (matcher.find()) recordId = matcher.group(1);
	}

	private void initializeReader(String fileName) throws FileNotFoundException {
		reader = new BufferedReader(new FileReader(new File(fileName)));
	}

	/**
	 * Returns the next record in the iteration.
	 *
	 * @return SemanticEnrichment object
	 */
	public List<SemanticEnrichment> next() {
		Set<SemanticEnrichment> result = new HashSet<>();
		try {
			while (reader.ready()) {
				try {
					String[] line = reader.readLine().split("\\t");
					if (line.length >= 5) {
						result.add(SemanticEnrichment.create(recordId, line[2], Long.parseLong(line[4])));
					}
				} catch (NumberFormatException ignore) {
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ArrayList<>(result);
	}

}
