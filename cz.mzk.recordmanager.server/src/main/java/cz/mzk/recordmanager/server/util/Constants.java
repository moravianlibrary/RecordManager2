package cz.mzk.recordmanager.server.util;

public class Constants {


	public static final String JOB_ID_HARVEST			= "oaiHarvestJob";
	public static final String JOB_ID_HARVEST_PART		= "oaiPartitionedHarvestJob";
	public static final String JOB_ID_HARVEST_AUTH		= "oaiHarvestAuthorityJob";
	public static final String JOB_ID_HARVEST_ONE_BY_ONE = "oaiHarvestOneByOneJob";
	public static final String JOB_ID_HARVEST_KRAMERIUS = "krameriusHarvestJob";
	public static final String JOB_ID_FULLTEXT_KRAMERIUS = "krameriusFulltextJob";
	public static final String JOB_ID_HARVEST_SINGLE	= "oaiHarvestSingleRecordJob";
	public static final String JOB_ID_DEDUP				= "dedupRecordsJob";
	public static final String JOB_ID_SOLR_INDEX_ALL_RECORDS		= "indexAllRecordsToSolrJob";
	public static final String JOB_ID_SOLR_INDEX					= "indexRecordsToSolrJob";
	public static final String JOB_ID_SOLR_INDEX_LOCAL_RECORDS		= "indexLocalRecordsToSolrJob";
	public static final String JOB_ID_EXPORT						= "exportRecordsJob";
	public static final String JOB_ID_IMPORT						= "importRecordsJob";
	public static final String JOB_ID_IMPORT_ANTIKVARIATY			= "antikvariatyImportRecordsJob";
	public static final String JOB_ID_REGEN_DEDUP_KEYS				= "regenerateDedupKeysJob";
	public static final String JOB_ID_DELETE_ALL_RECORDS_FROM_SOLR	= "deleteAllRecordsFromSolrJob";
	
	public static final String JOB_PARAM_CONF_ID 		= "configurationId";
	public static final String JOB_PARAM_FROM_DATE 		= "from";
	public static final String JOB_PARAM_UNTIL_DATE 	= "to";
	public static final String JOB_PARAM_SOLR_URL		= "solrUrl";
	public static final String JOB_PARAM_SOLR_QUERY		= "query";
	public static final String JOB_PARAM_FORMAT			= "format";
	public static final String JOB_PARAM_RESUMPTION_TOKEN = "resumptionToken";
	public static final String JOB_PARAM_REPEAT    		= "repeat";
	
	public static final String JOB_PARAM_OUT_FILE		= "outFile";
	public static final String JOB_PARAM_IN_FILE		= "inFile";
	
	public static final String JOB_PARAM_TIMESTAMP		= "timestamp";
	
	public static final String METADATA_FORMAT_MARC21      = "marc21-xml";
	public static final String METADATA_FORMAT_XML_MARC    = "xml-marc";
	public static final String METADATA_FORMAT_DUBLIN_CORE = "dublinCore";
	
	public static final String PREFIX_MKZ				= "mzk";
	public static final String PREFIX_NKP				= "nkp";
	public static final String PREFIX_NTK				= "ntk";

	public static final String JOB_PARAM_RECORD_ID		= "recordId";
	/* need some changing paramteter :-/ - 
	 * A job instance already exists and is complete for parameters={}. 
	 * If you want to run this job again, change the parameters. */
	public static final String JOB_PARAM_HACK			= "h";
}	

