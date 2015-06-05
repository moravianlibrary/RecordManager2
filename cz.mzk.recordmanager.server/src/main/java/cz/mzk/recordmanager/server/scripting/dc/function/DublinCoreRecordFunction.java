package cz.mzk.recordmanager.server.scripting.dc.function;

import cz.mzk.recordmanager.server.dc.DublinCoreRecord;

public interface DublinCoreRecordFunction {

	public Object apply(DublinCoreRecord record, Object args);

}
