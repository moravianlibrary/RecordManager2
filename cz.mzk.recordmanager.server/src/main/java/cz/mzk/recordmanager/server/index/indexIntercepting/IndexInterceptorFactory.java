package cz.mzk.recordmanager.server.index.indexIntercepting;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import org.springframework.stereotype.Component;

@Component
public class IndexInterceptorFactory {

	public IndexInterceptor getIndexInterceptor(HarvestedRecord hr, MarcRecord marcRec) {
		switch (hr.getHarvestedFrom().getIdPrefix()) {
		default:
			return new DefaultMarcIndexInterceptor(marcRec);
		}
	}

}
