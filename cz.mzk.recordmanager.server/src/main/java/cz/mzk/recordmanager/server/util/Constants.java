package cz.mzk.recordmanager.server.util;

public class Constants {

	public static final String JOB_ID_HARVEST			= "oaiHarvestJob";
	public static final String JOB_ID_HARVEST_PART		= "oaiPartitionedHarvestJob";
	public static final String JOB_ID_DEDUP				= "dedupRecordsJob";
	public static final String JOB_ID_SOLR_INDEX		= "indexRecordsToSolrJob";
	public static final String JOB_ID_EXPORT			= "exportRecordsJob";
	public static final String JOB_ID_IMPORT			= "importRecordsJob";
	
	public static final String JOB_PARAM_CONF_ID 		= "configurationId";
	public static final String JOB_PARAM_FROM_DATE 		= "from";
	public static final String JOB_PARAM_UNTIL_DATE 	= "to";
	public static final String JOB_PARAM_SOLR_URL		= "solrUrl";
	public static final String JOB_PARAM_FORMAT			= "format";
	
	public static final String JOB_PARAM_OUT_FILE		= "outFile";
	public static final String JOB_PARAM_IN_FILE		= "inFile";
}
