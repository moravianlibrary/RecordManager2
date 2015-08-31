package cz.mzk.recordmanager.server.marc.intercepting;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.marc4j.marc.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.marc.InvalidMarcException;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.util.Constants;

@Component
public class MarcInterceptorFactory {

	@Autowired
	private MarcXmlParser marcXmlParser;
	
	public MarcRecordInterceptor getInterceptor(ImportConfiguration configuration, byte rawRecord[]) {
		
		
		String prefix = configuration.getIdPrefix();
		try {
			Record record = parseRecord(rawRecord);
			switch (prefix){
			case Constants.PREFIX_CASLIN: return new SkatMarcInterceptor(record);
			case Constants.PREFIX_MZKNORMS: return new MzkNormsMarcInterceptor(record);
			default: return new DefaultMarcInterceptor(record);
			}
		} catch (InvalidMarcException ime) {
			return null;
		}
		
	}
	
	protected Record parseRecord(byte[] rawRecord) {
		InputStream is = new ByteArrayInputStream(rawRecord);
		return marcXmlParser.parseUnderlyingRecord(is);
	}
}
