package cz.mzk.recordmanager.server.kramerius;

public class FedoraModels {

	// All top-level models are used for harvesting (different Krameriuses can have a set of specific models)
	// monograph & periodical models are common to all instances of Kramerius
	public static final String[] HARVESTED_MODELS = {
			"monograph",
			"periodical",
			"map", 
			"archive",
			"manuscript", 
			"sheetmusic",
			"soundrecording",
			"graphic", };

}
