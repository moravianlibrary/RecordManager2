package cz.mzk.recordmanager.server.facade;

import java.io.File;

import cz.mzk.recordmanager.server.model.DownloadImportConfiguration;

public interface ImportRecordFacade {

	public void importFactory(DownloadImportConfiguration dic);
	
	public void importFile(long importConfId, File file, String format);
	
	public void downloadAndImportRecordSJob(DownloadImportConfiguration dic);

}
