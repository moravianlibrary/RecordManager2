package cz.mzk.recordmanager.server.imports;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import cz.mzk.recordmanager.server.util.CleaningUtils;
import cz.mzk.recordmanager.server.util.MetadataUtils;
import cz.mzk.recordmanager.server.util.identifier.ISBNUtils;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import cz.mzk.recordmanager.server.model.ObalkyKnihTOC;
import cz.mzk.recordmanager.server.util.HttpClient;

public class ObalkyKnihRecordsReader implements ItemReader<ObalkyKnihTOC> {

	@Autowired
	private HttpClient httpClient;

	private static final String OBALKY_KNIH_TOC_URL = "http://www.obalkyknih.cz/api/toc.xml";

	private static final String OCLC_PREFIX = "(OCoLC)";

	private static final Pattern OCLC_PREFIX_PATTERN = Pattern.compile(OCLC_PREFIX);

	private static final int EFFECTIVE_LENGHT = 32;

	private StaxEventItemReader<ObalkyKnihTOC> reader;

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
			reader.setResource(new InputStreamResource(httpClient.executeGet(OBALKY_KNIH_TOC_URL)) {

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
	}

}
