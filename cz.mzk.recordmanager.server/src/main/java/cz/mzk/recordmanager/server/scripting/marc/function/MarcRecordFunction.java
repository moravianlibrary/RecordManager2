package cz.mzk.recordmanager.server.scripting.marc.function;

import cz.mzk.recordmanager.server.marc.MarcRecord;

public interface MarcRecordFunction {

	public Object apply(MarcRecord record, Object args);

}
