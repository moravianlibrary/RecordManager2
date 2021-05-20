package cz.mzk.recordmanager.server.index.indexIntercepting;

import cz.mzk.recordmanager.server.marc.MarcRecord;

public interface IndexInterceptor {

	MarcRecord intercept();

}
