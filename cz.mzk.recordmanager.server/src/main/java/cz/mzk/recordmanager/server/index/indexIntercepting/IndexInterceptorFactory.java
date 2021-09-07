package cz.mzk.recordmanager.server.index.indexIntercepting;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.util.MetadataUtils;
import org.springframework.stereotype.Component;

@Component
public class IndexInterceptorFactory {

	public IndexInterceptor getIndexInterceptor(HarvestedRecord hr, MarcRecord marcRec) {
		switch (MetadataUtils.getPrefix(hr)) {
		default:
			return new DefaultMarcIndexInterceptor(marcRec);
		}
	}

}
