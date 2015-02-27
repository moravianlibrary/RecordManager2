package cz.mzk.recordmanager.server.util;

public class Constants {

	public static final String JOB_ID_HARVEST			= "oaiHarvestJob";
	public static final String JOB_ID_HARVEST_PART		= "oaiPartitionedHarvestJob";
	public static final String JOB_ID_DEDUP				= "dedupRecordsJob";
	public static final String JOB_ID_SOLR_INDEX		= "indexRecordsToSolrJob";
	
	public static final String JOB_PARAM_CONF_ID 		= "configurationId";
	public static final String JOB_PARAM_FROM_DATE 		= "fromDate";
	public static final String JOB_PARAM_UNTIL_DATE 	= "untilDate";
}
