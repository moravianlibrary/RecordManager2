package cz.mzk.recordmanager.server.imports.obalky.annotations;

import cz.mzk.recordmanager.server.model.ObalkyKnihAnnotation;
import cz.mzk.recordmanager.server.util.CleaningUtils;
import cz.mzk.recordmanager.server.util.HttpClient;
import cz.mzk.recordmanager.server.util.MetadataUtils;
import cz.mzk.recordmanager.server.util.ProgressLogger;
import cz.mzk.recordmanager.server.util.identifier.ISBNUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class AnnotationsReader implements ItemReader<ObalkyKnihAnnotation> {

	private static Logger logger = LoggerFactory.getLogger(AnnotationsReader.class);

	@Autowired
	private HttpClient httpClient;


	private String filename = null;

	private StaxEventItemReader<ObalkyKnihAnnotation> reader;

	private static final String ANNOTATIONS_URL = "http://www.obalkyknih.cz/dumpdb/anotace.txt";

	private static final String OCLC_PREFIX = "(OCoLC)";

	private static final Pattern OCLC_PREFIX_PATTERN = Pattern.compile("\\(OCoLC\\)");

	private static final SimpleDateFormat UPDATED_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public AnnotationsReader(String filename) {
		this.filename = filename;
	}

	private ProgressLogger progressLogger = new ProgressLogger(logger, 5000);

	private static final int EFFECTIVE_LENGHT = 32;

	@Override
	public synchronized ObalkyKnihAnnotation read() throws Exception {
		progressLogger.incrementAndLogProgress();
		if (reader == null) {
			initializeReader();
		}
		ObalkyKnihAnnotation next = reader.read();
		if (next != null) {
			normalize(next);
		}
		return next;
	}

	protected void initializeReader() throws IOException {
		try {
			reader = new StaxEventItemReader<>();
			InputStream is;
			if (filename != null) is = new FileInputStream(new File(filename));
			else is = httpClient.executeGet(ANNOTATIONS_URL);
			reader.setResource(new InputStreamResource(is));
			reader.setFragmentRootElementName("book");
			Jaxb2Marshaller unmarshaller = new Jaxb2Marshaller();
			unmarshaller.setClassesToBeBound(ObalkyKnihAnnotation.class);
			unmarshaller.afterPropertiesSet();
			reader.setUnmarshaller(unmarshaller);
			reader.setSaveState(false);
			reader.open(null);
			reader.afterPropertiesSet();
		} catch (Exception ex) {
			throw new RuntimeException("StaxEventItemReader can not be created", ex);
		}
	}

	private void normalize(ObalkyKnihAnnotation annotation) {
		if (annotation.getOclc() != null && annotation.getOclc().startsWith(OCLC_PREFIX)) {
			annotation.setOclc(CleaningUtils.replaceAll(annotation.getOclc(), OCLC_PREFIX_PATTERN, ""));
		}
		if (annotation.getIsbnStr() != null) {
			annotation.setIsbn(ISBNUtils.toISBN13Long(annotation.getIsbnStr()));
		}
		annotation.setCnb(MetadataUtils.shorten(annotation.getCnb(), EFFECTIVE_LENGHT));
		annotation.setOclc(MetadataUtils.shorten(annotation.getOclc(), EFFECTIVE_LENGHT));
		annotation.setLastHarvest(new Date());
		try {
			annotation.setUpdated(UPDATED_FORMAT.parse(annotation.getUpdatedStr()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

}
