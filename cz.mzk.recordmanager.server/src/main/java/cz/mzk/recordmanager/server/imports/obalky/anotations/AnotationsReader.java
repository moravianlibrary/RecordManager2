package cz.mzk.recordmanager.server.imports.obalky.anotations;

import cz.mzk.recordmanager.server.model.ObalkyKnihAnotation;
import cz.mzk.recordmanager.server.util.HttpClient;
import cz.mzk.recordmanager.server.util.ProgressLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnotationsReader implements ItemReader<ObalkyKnihAnotation> {

	private static Logger logger = LoggerFactory.getLogger(AnotationsReader.class);

	@Autowired
	private HttpClient httpClient;


	private String filename = null;

	private BufferedReader reader = null;

	private static final String ANOTATIONS_URL = "http://www.obalkyknih.cz/dumpdb/anotace.txt";

	private static final Pattern LINE_PARSER =
			Pattern.compile("(\\S*)\\s+(\\S*)\\s+(?:\\(OCoLC\\))?(\\S*)\\s+([-0-9]{10} [:0-9]{8})\\s+(.*)");

	private static final SimpleDateFormat UPDATED_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public AnotationsReader(String filename) {
		this.filename = filename;
	}

	private ProgressLogger progressLogger = new ProgressLogger(logger, 5000);

	@Override
	public synchronized ObalkyKnihAnotation read() throws Exception {
		if (reader == null) initializeReader();
		while (reader.ready()) {
			progressLogger.incrementAndLogProgress();
			String line = reader.readLine();
			Matcher matcher = LINE_PARSER.matcher(line);
			if (!matcher.matches()) continue;
			ObalkyKnihAnotation newAnotation = new ObalkyKnihAnotation();
			try {
				newAnotation.setIsbn(Long.parseLong(matcher.group(1)));
			} catch (NumberFormatException nfe) {
				newAnotation.setIsbn(null);
			}
			newAnotation.setNbn(matcher.group(2).equals("\\N") ? null : matcher.group(2));
			newAnotation.setOclc(matcher.group(3).equals("\\N") ? null : matcher.group(3));
			newAnotation.setUpdated(UPDATED_FORMAT.parse(matcher.group(4)));
			newAnotation.setAnotation(matcher.group(5));
			if (newAnotation.getIsbn() == null && newAnotation.getNbn() == null && newAnotation.getOclc() == null)
				continue;
			return newAnotation;
		}
		return null;
	}

	protected void initializeReader() throws IOException {
		if (filename != null) reader = new BufferedReader(new FileReader(new File(filename)));
		else reader = new BufferedReader(new InputStreamReader(httpClient.executeGet(ANOTATIONS_URL)));
	}

}
