package cz.mzk.recordmanager.server.imports.obalky.anotations;

import cz.mzk.recordmanager.server.model.ObalkyKnihAnotation;
import cz.mzk.recordmanager.server.oai.dao.ObalkyKnihAnotationDAO;
import cz.mzk.recordmanager.server.util.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnotationsTasklet implements Tasklet {

	private static Logger logger = LoggerFactory.getLogger(AnotationsTasklet.class);

	@Autowired
	private HttpClient httpClient;

	@Autowired
	private ObalkyKnihAnotationDAO anotationDAO;

	private String filename = null;

	private BufferedReader reader = null;

	private static final String ANOTATIONS_URL = "http://www.obalkyknih.cz/dumpdb/anotace.txt";

	private static final Pattern LINE_PARSER = Pattern.compile("(\\S*)\\s(\\S*)\\s(?:\\(OCoLC\\))?(\\S*)\\s([-0-9]{10} [:0-9]{8})(.*)");

	private static final SimpleDateFormat UPDATED_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public AnotationsTasklet(String filename) {
		this.filename = filename;
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		if (reader == null) initializeReader();
		while (reader.ready()) {
			String line = reader.readLine();
			Matcher matcher = LINE_PARSER.matcher(line);
			if (!matcher.matches()) continue;
			ObalkyKnihAnotation newAnotation = new ObalkyKnihAnotation();
			newAnotation.setIsbn(matcher.group(1).equals("\\N") ? null : matcher.group(1));
			newAnotation.setNbn(matcher.group(2).equals("\\N") ? null : matcher.group(2));
			newAnotation.setOclc(matcher.group(3).equals("\\N") ? null : matcher.group(3));
			newAnotation.setUpdated(UPDATED_FORMAT.parse(matcher.group(4)));
			newAnotation.setAnotation(matcher.group(5));
			anotationDAO.persist(newAnotation);
		}
		return RepeatStatus.FINISHED;
	}

	protected void initializeReader() throws IOException {
		if (filename != null) reader = new BufferedReader(new FileReader(new File(filename)));
		else reader = new BufferedReader(new InputStreamReader(httpClient.executeGet(ANOTATIONS_URL)));
	}
}
