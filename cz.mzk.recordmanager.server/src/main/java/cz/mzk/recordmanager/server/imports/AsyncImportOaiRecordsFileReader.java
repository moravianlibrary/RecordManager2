package cz.mzk.recordmanager.server.imports;

import cz.mzk.recordmanager.server.oai.harvest.OaiErrorException;
import cz.mzk.recordmanager.server.oai.model.OAIRecord;
import cz.mzk.recordmanager.server.oai.model.OAIRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class AsyncImportOaiRecordsFileReader implements ItemReader<List<OAIRecord>>, ItemStream {

	private static Logger logger = LoggerFactory.getLogger(AsyncImportOaiRecordsFileReader.class);

	private static final List<OAIRecord> IMPORT_FINISHED_SENTINEL = Collections.emptyList();

	private static final List<OAIRecord> IMPORT_FAILED_SENTINEL = Collections.emptyList();

	private InputStream is;

	private final Unmarshaller unmarshaller;

	private List<String> files = null;

	private boolean done = false;

	private volatile Thread harvestingThread;

	private ArrayBlockingQueue<List<OAIRecord>> queue = new ArrayBlockingQueue<>(5);

	public AsyncImportOaiRecordsFileReader(String filename) {
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
		if (done) {
			return null;
		}
		List<OAIRecord> results = queue.take();
		if (results == IMPORT_FINISHED_SENTINEL) {
			done = true;
			return null;
		} else if (results == IMPORT_FAILED_SENTINEL) {
			done = true;
			throw new RuntimeException("OAI import failed, see logs for details");
		}
		return results;
	}

	private void parseRecords() {
		boolean failed = true;
		try {
			while (true) {
				if (!initializeInputStream()) {
					failed = false;
					break;
				}
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
					queue.put(oaiRoot.getListRecords().getRecords());
				} catch (InterruptedException ie) {
					// to make sure there is a room for sentinel value
					if (queue.remainingCapacity() == 0) {
						queue.poll();
					}
					break;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (RuntimeException re) {
			logger.error("Exception thrown during OAI import", re);
		} finally {
			try {
				queue.put((failed) ? IMPORT_FAILED_SENTINEL : IMPORT_FINISHED_SENTINEL);
			} catch (InterruptedException ie) {
				queue.poll(); // to make sure there is a room for sentinel value
				try {
					queue.put((failed) ? IMPORT_FAILED_SENTINEL : IMPORT_FINISHED_SENTINEL);
				} catch (InterruptedException ie2) {
					// done
				}
			}
		}
		this.harvestingThread = null;
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

	private synchronized boolean initializeInputStream() {
		if (files.isEmpty()) return false;
		try {
			is = new FileInputStream(files.remove(0)); // next file
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {
		harvestingThread = new Thread(this::parseRecords);
		harvestingThread.start();
	}

	@Override
	public void update(ExecutionContext executionContext) throws ItemStreamException {

	}

	@Override
	public void close() throws ItemStreamException {
		if (harvestingThread != null) {
			harvestingThread.interrupt();
		}
	}
}
