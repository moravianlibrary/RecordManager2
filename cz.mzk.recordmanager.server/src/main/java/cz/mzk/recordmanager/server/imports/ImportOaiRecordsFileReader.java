package cz.mzk.recordmanager.server.imports;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.springframework.batch.item.ItemReader;

import cz.mzk.recordmanager.server.oai.harvest.OaiErrorException;
import cz.mzk.recordmanager.server.oai.model.OAIRecord;
import cz.mzk.recordmanager.server.oai.model.OAIRoot;

public class ImportOaiRecordsFileReader implements ItemReader<List<OAIRecord>> {

	private InputStream is;

	private final Unmarshaller unmarshaller;

	private List<String> files = null;

	private static final int BATCH_SIZE = 100;

	public ImportOaiRecordsFileReader(String filename) throws FileNotFoundException {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(OAIRoot.class);
			this.unmarshaller = jaxbContext.createUnmarshaller();
		} catch (JAXBException je) {
			throw new RuntimeException(je);
		}
		getFilesName(filename);
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
				if (is.markSupported()) {
					is.mark(Integer.MAX_VALUE);
					is.reset();
				}
				OAIRoot oaiRoot = (OAIRoot) unmarshaller.unmarshal(is);
				is.close();
				if (oaiRoot.getOaiError() != null) {
					throw new OaiErrorException(oaiRoot.getOaiError().getMessage());
				}
				results.addAll(oaiRoot.getListRecords().getRecords());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return results;
	}

	private void getFilesName(String filename) {
		if (files == null) files = new ArrayList<>();
		File f = new File(filename);
		if (f.isFile()) {
			files.add(f.getAbsolutePath());
		} else {
			File[] listFiles = f.listFiles();
			if (listFiles == null) return;
			for (File file : listFiles) {
				if (file.isDirectory()) getFilesName(file.getAbsolutePath());
				else files.add(file.getAbsolutePath());
			}
		}
	}

	private void initializeInputStream() {
		try {
			is = new FileInputStream(files.remove(0)); // next file
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
