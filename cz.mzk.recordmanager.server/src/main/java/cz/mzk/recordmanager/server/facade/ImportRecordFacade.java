package cz.mzk.recordmanager.server.facade;

import java.io.File;

import cz.mzk.recordmanager.server.model.DownloadImportConfiguration;

public interface ImportRecordFacade {

	void importFactory(DownloadImportConfiguration dic);
	
	void importFile(long importConfId, File file, String format);
	
	void downloadAndImportRecordSJob(DownloadImportConfiguration dic);
	
	void importOaiRecordsJob(long impotrConfId, String fileName);
	
	void unpackAndImportRecordsJob(DownloadImportConfiguration dic);

}
