package cz.mzk.recordmanager.server.imports;

import cz.mzk.recordmanager.server.oai.harvest.OaiErrorException;
import cz.mzk.recordmanager.server.oai.model.OAIRecord;
import cz.mzk.recordmanager.server.oai.model.OAIRoot;
import cz.mzk.recordmanager.server.util.CleaningUtils;
import cz.mzk.recordmanager.server.util.ProgressLogger;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ImportOaiRecordsFileReader implements ItemReader<List<OAIRecord>> {

	private static final Logger logger = LoggerFactory.getLogger(AsyncImportOaiRecordsFileReader.class);

	private byte[] bytes;

	private final Unmarshaller unmarshaller;

	private List<String> files = null;

	private static final int BATCH_SIZE = 100;

	private final ProgressLogger progress;

	public ImportOaiRecordsFileReader(String filename) throws FileNotFoundException {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(OAIRoot.class);
			this.unmarshaller = jaxbContext.createUnmarshaller();
			getFilesName(filename);
		} catch (JAXBException je) {
			throw new RuntimeException(je);
		}
		this.progress = new ProgressLogger(logger, 5000);
	}

	@Override
	public List<OAIRecord> read() throws Exception {
		if (!files.isEmpty()) {
			return parseRecords();
		}
		return null;
	}

	private List<OAIRecord> parseRecords() {
		List<OAIRecord> results = new ArrayList<>();
		while (results.size() < BATCH_SIZE && !files.isEmpty()) {
			initializeInputStream();
			try {
				InputStream is = new ByteArrayInputStream(bytes);
				if (is.markSupported()) {
					is.mark(Integer.MAX_VALUE);
					is.reset();
				}
				OAIRoot oaiRoot;
				try {
					oaiRoot = (OAIRoot) unmarshaller.unmarshal(is);
				} catch (UnmarshalException ex) {
					logger.warn("Invalid XML characters");
					oaiRoot = (OAIRoot) unmarshaller.unmarshal(
							CleaningUtils.removeInvalidXMLCharacters(new ByteArrayInputStream(bytes)));
				}
				is.close();
				if (oaiRoot.getOaiError() != null) {
					throw new OaiErrorException(oaiRoot.getOaiError().getMessage());
				}
				progress.incrementAndLogProgress();
				results.addAll(oaiRoot.getListRecords().getRecords());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return results;
	}

	private void getFilesName(String filename) throws FileNotFoundException {
		if (files == null) files = new ArrayList<>();
		File f = new File(filename);
		if (f.isFile()) {
			files.add(f.getAbsolutePath());
		} else {
			File[] listFiles = f.listFiles();
			if (listFiles == null) throw new FileNotFoundException();
			for (File file : listFiles) {
				if (file.isDirectory()) getFilesName(file.getAbsolutePath());
				else files.add(file.getAbsolutePath());
			}
		}
	}

	private void initializeInputStream() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			IOUtils.copy(new FileInputStream(files.remove(0)), baos); // next file
			bytes = baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
