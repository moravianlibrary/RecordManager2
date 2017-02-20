package cz.mzk.recordmanager.server.marc.intercepting;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.marc4j.marc.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.marc.InvalidMarcException;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.util.Constants;

@Component
public class MarcInterceptorFactory {

	@Autowired
	private MarcXmlParser marcXmlParser;
	
	@Autowired
	private ApplicationContext appCtx;
	
	public MarcRecordInterceptor getInterceptor(ImportConfiguration configuration, byte rawRecord[]) {
		String prefix = configuration.getIdPrefix();
		try {
			Record record = parseRecord(rawRecord);
			switch (prefix){
			case Constants.PREFIX_CASLIN: 
				MarcRecordInterceptor mri = new SkatMarcInterceptor(record);
				appCtx.getAutowireCapableBeanFactory().autowireBean(mri);
				return mri;
			case Constants.PREFIX_MZKNORMS: return new MzkNormsMarcInterceptor(record);
			case Constants.PREFIX_NLK: return new NlkMarcInterceptor(record);
			case Constants.PREFIX_OPENLIB: return new OpenlibMarcInterceptor(record);
			case Constants.PREFIX_KKVY: return new KkvyNormsMarcInterceptor(record);
			case Constants.PREFIX_CBVK: return new CbvkMarcInterceptor(record);
			case Constants.PREFIX_BMC: return new BmcMarcInterceptor(record);
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
