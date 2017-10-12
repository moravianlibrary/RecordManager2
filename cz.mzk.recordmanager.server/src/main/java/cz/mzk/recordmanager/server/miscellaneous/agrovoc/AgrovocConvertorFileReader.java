package cz.mzk.recordmanager.server.miscellaneous.agrovoc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import cz.mzk.recordmanager.server.marc.marc4j.AgrovocXmlStreamReader;

public class AgrovocConvertorFileReader implements ItemReader<Map<String, Map<String, List<String>>>> {

	private static Logger logger = LoggerFactory.getLogger(AgrovocConvertorFileReader.class);

	private AgrovocXmlStreamReader reader;

	private FileInputStream inStream;

	public AgrovocConvertorFileReader(String filename) throws FileNotFoundException {
		initializeFilesReader(filename);
	}

	@Override
	public Map<String, Map<String, List<String>>> read() throws Exception, UnexpectedInputException,
			ParseException, NonTransientResourceException {
		 Map<String, Map<String, List<String>>> batch = reader.next();
		return (batch == null || batch.isEmpty()) ? null : batch;
	}

	protected void initializeFilesReader(String filename) {
		try {
			inStream = new FileInputStream(filename);
			reader = new AgrovocXmlStreamReader(inStream);
		} catch (FileNotFoundException e) {
			logger.warn(e.getMessage());
		}
	}

}
