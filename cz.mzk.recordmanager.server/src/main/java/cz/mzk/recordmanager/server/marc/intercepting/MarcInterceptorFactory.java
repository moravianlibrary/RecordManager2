package cz.mzk.recordmanager.server.marc.intercepting;

import cz.mzk.recordmanager.server.marc.InvalidMarcException;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.util.Constants;
import org.marc4j.marc.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class MarcInterceptorFactory {

	@Autowired
	private MarcXmlParser marcXmlParser;

	@Autowired
	private ApplicationContext appCtx;

	public MarcRecordInterceptor getInterceptor(ImportConfiguration configuration, byte rawRecord[]) {
		return getInterceptor(configuration, null, rawRecord);
	}

	public MarcRecordInterceptor getInterceptor(ImportConfiguration configuration, String recordId, byte rawRecord[]) {
		String prefix = configuration.getIdPrefix();
		try {
			Record record = marcXmlParser.parseUnderlyingRecord(rawRecord);
			switch (prefix){
			case Constants.PREFIX_CASLIN:
				MarcRecordInterceptor mri = new SkatMarcInterceptor(record);
				appCtx.getAutowireCapableBeanFactory().autowireBean(mri);
				return mri;
			case Constants.PREFIX_MZKNORMS: return new MzkNormsMarcInterceptor(record);
			case Constants.PREFIX_RKKA: return new RkkaMarcInterceptor(record, configuration, recordId);
			case Constants.PREFIX_NLK: return new NlkMarcInterceptor(record, configuration, recordId);
			case Constants.PREFIX_OPENLIB: return new OpenlibMarcInterceptor(record);
			case Constants.PREFIX_KKVY: return new KkvyMarcInterceptor(record, configuration, recordId);
			case Constants.PREFIX_CBVK: return new CbvkMarcInterceptor(record, configuration, recordId);
			case Constants.PREFIX_BMC: return new BmcMarcInterceptor(record);
			case Constants.PREFIX_TDKIV: return new TdkivMarcInterceptor(record);
			case Constants.PREFIX_CELITEBIB : return new CelitebibMarcInterceptor(record);
			case Constants.PREFIX_KKKV : return new KkkvMarcInterceptor(record, configuration, recordId);
			case Constants.PREFIX_IIR: return new IirMarcInterceptor(record, configuration, recordId);
			case Constants.PREFIX_DIVABIB: return new DivabibMarcInterceptor(record);
			case Constants.PREFIX_KFBZ: return new KfbzMarcInterceptor(record, configuration, recordId);
			default: return new DefaultMarcInterceptor(record, configuration, recordId);
			}
		} catch (InvalidMarcException ime) {
			return null;
		}
	}

}
