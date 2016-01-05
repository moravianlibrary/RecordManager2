package cz.mzk.recordmanager.server.imports;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.validator.routines.ISBNValidator;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
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

	private final ISBNValidator isbnValidator = ISBNValidator.getInstance(true);

	private StaxEventItemReader<ObalkyKnihTOC> reader;

	private static class FixingInputStream extends InputStream {

		private InputStream delegate;

		public FixingInputStream(InputStream delegate) {
			this.delegate = delegate;
		}

		@Override
		public int read() throws IOException {
			int nextByte = -1;
			while ((nextByte = delegate.read()) == 12);
			return nextByte;
		}

	}

	@Override
	public ObalkyKnihTOC read() throws Exception,
			UnexpectedInputException, ParseException,
			NonTransientResourceException {
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
			reader = new StaxEventItemReader<ObalkyKnihTOC>();
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

	protected void normalize(ObalkyKnihTOC toc) {
		if (toc.getOclc() != null && toc.getOclc().startsWith(OCLC_PREFIX)) {
			toc.setOclc(toc.getOclc().replace(OCLC_PREFIX, ""));
		}
		if (toc.getIsbnStr() != null) {
			String isbn = isbnValidator.validate(toc.getIsbnStr());
			if (isbn != null) {
				toc.setIsbn(Long.valueOf(isbn));
			}
		}
	}

}
