package cz.mzk.recordmanager.server.imports.obalky;

import cz.mzk.recordmanager.server.model.ObalkyKnihTOC;
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
import org.springframework.beans.factory.annotation.Value;
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

public class ObalkyKnihRecordsReader implements ItemReader<ObalkyKnihTOC> {

	@Autowired
	private HttpClient httpClient;

	private String filename = null;

	private Date from = null;

	private static final String OBALKY_KNIH_TOC_URL_FORMAT = "https://%s:%s@servis.obalkyknih.cz/export/okcz_toc.php%s";

	private static final String OBALKY_KNIH_TOC_URL_PARAM_FORMAT = "?last_change=%tY-%tm-%td";

	private static final String OCLC_PREFIX = "(OCoLC)";

	private static final Pattern OCLC_PREFIX_PATTERN = Pattern.compile("\\(OCoLC\\)");

	private static final int EFFECTIVE_LENGHT = 32;

	private StaxEventItemReader<ObalkyKnihTOC> reader;

	private static final SimpleDateFormat UPDATED_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static Logger logger = LoggerFactory.getLogger(ObalkyKnihRecordsReader.class);

	private ProgressLogger progressLogger = new ProgressLogger(logger, 5000);

	@Value(value = "${ok_username:#{null}}")
	private String USERNAME = "";

	@Value(value = "${ok_password:#{null}}")
	private String PASSWORD = "";

	public ObalkyKnihRecordsReader(String filename, Date from) {
		this.filename = filename;
		this.from = from;
	}

	private static class FixingInputStream extends InputStream {

		private InputStream delegate;

		public FixingInputStream(InputStream delegate) {
			this.delegate = delegate;
		}

		@Override
		public int read() throws IOException {
			int nextByte;
			while ((nextByte = delegate.read()) == 12) continue;
			return nextByte;
		}

	}

	@Override
	public ObalkyKnihTOC read() throws Exception {
		progressLogger.incrementAndLogProgress();
		if (reader == null) {
			initializeReader();
		}
		ObalkyKnihTOC next = reader.read();
		if (next != null) {
			normalize(next);
		}
		return next;
	}

	protected void initializeReader() throws Exception {
		try {
			reader = new StaxEventItemReader<>();
			InputStream is;
			if (filename != null) is = new FileInputStream(new File(filename));
			else is = httpClient.executeGet(createUrl());
			reader.setResource(new InputStreamResource(is) {

				@Override
				public InputStream getInputStream() throws IOException,
						IllegalStateException {
					return new FixingInputStream(super.getInputStream());
				}

			});
			reader.setFragmentRootElementName("book");
			Jaxb2Marshaller unmarshaller = new Jaxb2Marshaller();
			unmarshaller.setClassesToBeBound(ObalkyKnihTOC.class);
			unmarshaller.afterPropertiesSet();
			reader.setUnmarshaller(unmarshaller);
			reader.setSaveState(false);
			reader.open(null);
			reader.afterPropertiesSet();
		} catch (Exception ex) {
			throw new RuntimeException("StaxEventItemReader can not be created", ex);
		}
	}

	private void normalize(ObalkyKnihTOC toc) {
		if (toc.getOclc() != null && toc.getOclc().startsWith(OCLC_PREFIX)) {
			toc.setOclc(CleaningUtils.replaceAll(toc.getOclc(), OCLC_PREFIX_PATTERN, ""));
		}
		if (toc.getIsbnStr() != null) {
			toc.setIsbn(ISBNUtils.toISBN13Long(toc.getIsbnStr()));
		}
		toc.setNbn(MetadataUtils.shorten(toc.getNbn(), EFFECTIVE_LENGHT));
		toc.setOclc(MetadataUtils.shorten(toc.getOclc(), EFFECTIVE_LENGHT));
		toc.setEan(MetadataUtils.shorten(toc.getEan(), EFFECTIVE_LENGHT));
		toc.setLastHarvest(new Date());
		try {
			toc.setUpdated(UPDATED_FORMAT.parse(toc.getUpdatedStr()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	private String createUrl() {
		String params = from == null ? "" : String.format(OBALKY_KNIH_TOC_URL_PARAM_FORMAT, from, from, from);
		return String.format(OBALKY_KNIH_TOC_URL_FORMAT, USERNAME, PASSWORD, params);
	}

}
