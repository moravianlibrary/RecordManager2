package cz.mzk.recordmanager.server.facade;

public interface ZakonyProLidiFacade {

	void runZakonyProLidiHarvestJob();

	void runZakonyProLidiFulltextJob();
	
}
