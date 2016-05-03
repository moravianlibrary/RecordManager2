package cz.mzk.recordmanager.server.facade;

import java.io.File;

public interface ImportRecordFacade {

	public void importFile(long importConfId, File file, String format);

}
