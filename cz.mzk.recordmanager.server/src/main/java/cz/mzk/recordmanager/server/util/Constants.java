package cz.mzk.recordmanager.server.util;

public class Constants {


	public static final String JOB_ID_HARVEST			= "oaiHarvestJob";
	public static final String JOB_ID_HARVEST_PART		= "oaiPartitionedHarvestJob";
	public static final String JOB_ID_HARVEST_AUTH		= "oaiHarvestAuthorityJob";
	public static final String JOB_ID_HARVEST_ONE_BY_ONE = "oaiHarvestOneByOneJob";
	public static final String JOB_ID_HARVEST_KRAMERIUS = "krameriusHarvestJob";
	public static final String JOB_ID_HARVEST_COSMOTRON = "cosmotronHarvestJob";
	public static final String JOB_ID_FULLTEXT_KRAMERIUS = "krameriusFulltextJob";
	public static final String JOB_ID_HARVEST_SINGLE	= "oaiHarvestSingleRecordJob";
	public static final String JOB_ID_DEDUP				= "dedupRecordsJob";
	public static final String JOB_ID_SOLR_INDEX_ALL_RECORDS		= "indexAllRecordsToSolrJob";
	public static final String JOB_ID_SOLR_INDEX_HARVESTED_RECORDS	= "indexHarvestedRecordsToSolrJob";
	public static final String JOB_ID_SOLR_INDEX					= "indexRecordsToSolrJob";
	public static final String JOB_ID_SOLR_INDEX_INDIVIDUAL_RECORDS	= "indexIndividualRecordsToSolrJob";
	public static final String JOB_ID_EXPORT						= "exportRecordsJob";
	public static final String JOB_ID_EXPORT_COSMOTRON_996			= "exportCosmotron996Job";
	public static final String JOB_ID_IMPORT						= "importRecordsJob";
	public static final String JOB_ID_IMPORT_ANTIKVARIATY			= "antikvariatyImportRecordsJob";
	public static final String JOB_ID_IMPORT_OAI					= "importOaiRecordsJob";
	public static final String JOB_ID_REGEN_DEDUP_KEYS				= "regenerateDedupKeysJob";
	public static final String JOB_ID_DELETE_ALL_RECORDS_FROM_SOLR	= "deleteAllRecordsFromSolrJob";
	public static final String JOB_ID_GENERATE_SKAT_DEDUP_KEYS		= "generateSkatDedupKeys";
	
	public static final String JOB_PARAM_CONF_ID 		= "configurationId";
	public static final String JOB_PARAM_FROM_DATE 		= "from";
	public static final String JOB_PARAM_UNTIL_DATE 	= "to";
	public static final String JOB_PARAM_SOLR_URL		= "solrUrl";
	public static final String JOB_PARAM_SOLR_QUERY		= "query";
	public static final String JOB_PARAM_FORMAT			= "format";
	public static final String JOB_PARAM_RESUMPTION_TOKEN = "resumptionToken";
	public static final String JOB_PARAM_FULLTEXT_FIRST	= "firstId";
	public static final String JOB_PARAM_FULLTEXT_LAST  = "lastId";
	public static final String JOB_PARAM_REPEAT    		= "repeat";
	public static final String JOB_PARAM_INCREMENTAL	= "incremental";
	public static final String JOB_PARAM_RECORD_IDS		= "recordIds";
	
	public static final String JOB_PARAM_OUT_FILE		= "outFile";
	public static final String JOB_PARAM_IN_FILE		= "inFile";
	public static final String JOB_PARAM_DELETED_OUT_FILE = "deletedOutFile";
	
	public static final String JOB_PARAM_TIMESTAMP		= "timestamp";
	
	public static final String METADATA_FORMAT_MARC21      = "marc21-xml";
	public static final String METADATA_FORMAT_XML_MARC    = "xml-marc";
	public static final String METADATA_FORMAT_MARC_CPK    = "marccpk";
	public static final String METADATA_FORMAR_OAI_MARCXML_CPK = "oai_marcxml_cpk";
	public static final String METADATA_FORMAT_DUBLIN_CORE = "dublinCore";
	
	public static final String PREFIX_MZK				= "mzk";
	public static final String PREFIX_NKP				= "nkp";
	public static final String PREFIX_NTK				= "ntk";
	public static final String PREFIX_TRE				= "tre";
	public static final String PREFIX_CASLIN			= "caslin";
	public static final String PREFIX_MZKNORMS			= "unmz";
	public static final String PREFIX_SFXJIBMZK			= "sfxjibmzk";
	public static final String PREFIX_SFXJIBNLK			= "sfxjibnlk";
	public static final String PREFIX_SFXJIBNLK_PERIODICALS	= "sfxnlkper";
	public static final String PREFIX_NLK				= "nlk";
	public static final String PREFIX_OPENLIB			= "openlib";
	public static final String PREFIX_KRAM_MZK			= "kram-mzk";
	public static final String PREFIX_KRAM_NTK			= "kram-ntk";
	public static final String PREFIX_KRAM_KNAV			= "kram-knav";
	public static final String PREFIX_KRAM_NKP			= "kram-nkp";
	
	public static final String DOCUMENT_AVAILABILITY_ONLINE     = "online";
	public static final String DOCUMENT_AVAILABILITY_PROTECTED  = "protected";
	public static final String DOCUMENT_AVAILABILITY_UNKNOWN    = "unknown";

	public static final String JOB_PARAM_RECORD_ID		= "recordId";

	public static final String COSMOTRON_RECORD_ID_CHAR = "_";

}	

