package cz.mzk.recordmanager.server.facade;

import java.io.File;
import java.time.LocalDateTime;

import cz.mzk.recordmanager.server.model.DownloadImportConfiguration;

public interface ImportRecordFacade {

	void importFactory(DownloadImportConfiguration dic);

	void importFile(long importConfId, File file, String format);

	void downloadAndImportRecordSJob(DownloadImportConfiguration dic);

	void importOaiRecordsJob(long impotrConfId, String fileName);

	void unpackAndImportRecordsJob(DownloadImportConfiguration dic);

	void harvestInspirationsJob();

	LocalDateTime getLastCompletedExecution(String jobName);

	void reharvestAntikvariaty();

}
