package cz.mzk.recordmanager.server.facade;


public interface MiscellaneousFacade {

	void runFilterCaslinRecordsJob();

	void runGenerateSkatDedupKeys();

	void runZiskejLibraries();
}
