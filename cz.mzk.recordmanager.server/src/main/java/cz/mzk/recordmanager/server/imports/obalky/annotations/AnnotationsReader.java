package cz.mzk.recordmanager.server.imports.obalky.annotations;

import cz.mzk.recordmanager.server.model.ObalkyKnihAnnotation;
import cz.mzk.recordmanager.server.util.HttpClient;
import cz.mzk.recordmanager.server.util.ProgressLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnnotationsReader implements ItemReader<ObalkyKnihAnnotation> {

	private static Logger logger = LoggerFactory.getLogger(AnnotationsReader.class);

	@Autowired
	private HttpClient httpClient;


	private String filename = null;

	private BufferedReader reader = null;

	private static final String ANNOTATIONS_URL = "http://www.obalkyknih.cz/dumpdb/anotace.txt";

	private static final Pattern LINE_PARSER =
			Pattern.compile("(\\S*)\\s+(\\S*)\\s+(?:\\(OCoLC\\))?(\\S*)\\s+([-0-9]{10} [:0-9]{8})\\s+(.*)");

	private static final SimpleDateFormat UPDATED_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public AnnotationsReader(String filename) {
		this.filename = filename;
	}

	private ProgressLogger progressLogger = new ProgressLogger(logger, 5000);

	@Override
	public synchronized ObalkyKnihAnnotation read() throws Exception {
		if (reader == null) initializeReader();
		while (reader.ready()) {
			progressLogger.incrementAndLogProgress();
			String line = reader.readLine();
			Matcher matcher = LINE_PARSER.matcher(line);
			if (!matcher.matches()) continue;
			ObalkyKnihAnnotation newAnnotation = new ObalkyKnihAnnotation();
			try {
				newAnnotation.setIsbn(Long.parseLong(matcher.group(1)));
			} catch (NumberFormatException nfe) {
				newAnnotation.setIsbn(null);
			}
			newAnnotation.setNbn(matcher.group(2).equals("\\N") ? null : matcher.group(2));
			newAnnotation.setOclc(matcher.group(3).equals("\\N") ? null : matcher.group(3));
			newAnnotation.setUpdated(UPDATED_FORMAT.parse(matcher.group(4)));
			newAnnotation.setLastHarvest(new Date());
			newAnnotation.setAnnotation(matcher.group(5));
			if (newAnnotation.getIsbn() == null && newAnnotation.getNbn() == null && newAnnotation.getOclc() == null)
				continue;
			return newAnnotation;
		}
		return null;
	}

	protected void initializeReader() throws IOException {
		if (filename != null) reader = new BufferedReader(new FileReader(new File(filename)));
		else reader = new BufferedReader(new InputStreamReader(httpClient.executeGet(ANNOTATIONS_URL)));
	}

}
