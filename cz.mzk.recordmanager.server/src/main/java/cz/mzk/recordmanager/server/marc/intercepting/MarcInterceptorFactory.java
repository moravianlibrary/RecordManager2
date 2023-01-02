package cz.mzk.recordmanager.server.marc.intercepting;

import cz.mzk.recordmanager.server.marc.InvalidMarcException;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
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

	public MarcRecordInterceptor getInterceptor(HarvestedRecord hr) {
		return getInterceptor(hr.getHarvestedFrom(), hr.getUniqueId().getRecordId(), hr.getRawRecord());
	}

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
			case Constants.PREFIX_PALMKNIHY: return new PalmknihyMarcInterceptor(record, configuration, recordId);
			case Constants.PREFIX_KKPC: return new KkpcMarcInterceptor(record, configuration, recordId);
			case Constants.PREFIX_ARCHBIB:
			case Constants.PREFIX_CZHISTBIB:
				return new CzhistbibMarcInterceptor(record, configuration, recordId);
			case Constants.PREFIX_MKTRI:
			case Constants.PREFIX_TRIBUKOVEC:
			case Constants.PREFIX_TRIDOLNILOMNA:
			case Constants.PREFIX_TRIDOLNITOSANOVIC:
			case Constants.PREFIX_TRIHNOJNIK:
			case Constants.PREFIX_TRIHORNILOMNA:
			case Constants.PREFIX_TRIHORNITOSANOVICE:
			case Constants.PREFIX_TRIHRADEK:
			case Constants.PREFIX_TRIKOMORNILHOTKA:
			case Constants.PREFIX_TRIKOSARISKA:
			case Constants.PREFIX_TRIMILIKOV:
			case Constants.PREFIX_TRIMOSTY:
			case Constants.PREFIX_TRINAVSI:
			case Constants.PREFIX_TRINYNED:
			case Constants.PREFIX_TRIPISEK:
			case Constants.PREFIX_TRIREKA:
			case Constants.PREFIX_TRIROPICE:
			case Constants.PREFIX_TRISMILOVICE:
			case Constants.PREFIX_TRISTRITEZ:
			case Constants.PREFIX_TRITRANOVICE:
			case Constants.PREFIX_TRIVELOPOLI:
			case Constants.PREFIX_TRIVENDRYNE:
				return new MkTriMarcInterceptor(record, configuration, recordId);
			case Constants.PREFIX_MKTRIREKS:
				return new MkTriReksMarcInterceptor(record, configuration, recordId);
			case Constants.PREFIX_NACR:
				return new NacrMarcInterceptor(record, configuration, recordId);
			default: return new DefaultMarcInterceptor(record, configuration, recordId);
			}
		} catch (InvalidMarcException ime) {
			return null;
		}
	}

}
