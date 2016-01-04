package cz.mzk.recordmanager.server.imports;

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

	private static String OBALKY_KNIH_TOC_URL = "http://www.obalkyknih.cz/api/toc.xml";

	private StaxEventItemReader<ObalkyKnihTOC> reader;

	@Override
	public ObalkyKnihTOC read() throws Exception,
			UnexpectedInputException, ParseException,
			NonTransientResourceException {
		if (reader == null) {
			initializeReader();
		}
		return reader.read();
	}

	protected void initializeReader() throws Exception {
		try {
			reader = new StaxEventItemReader<ObalkyKnihTOC>();
			reader.setResource(new InputStreamResource(httpClient.executeGet(OBALKY_KNIH_TOC_URL)));
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

}
