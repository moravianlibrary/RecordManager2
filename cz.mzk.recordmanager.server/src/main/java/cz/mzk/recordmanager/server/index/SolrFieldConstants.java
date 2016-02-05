package cz.mzk.recordmanager.server.index;

public class SolrFieldConstants {

	public static final String ID_FIELD = "id";

	public static final String LOCAL_INSTITUTION_FIELD = "local_institution_facet_str_mv";

	public static final String INSTITUTION_FIELD = "institution";

	public static final String LOCAL_IDS_FIELD = "local_ids_str_mv";

	public static final String MERGED_FIELD = "merged_boolean";

	public static final String MERGED_CHILD_FIELD = "merged_child_boolean";

	public static final String WEIGHT = "weight_str";
	
	public static final String HOLDINGS_996_FIELD = "holdings_996_str_mv";
	
	public static final String EXTERNAL_LINKS_FIELD = "external_links_str_mv";
	
	public static final String PARENT_ID = "parent_id_str";

	public static final String UNKNOWN_INSTITUTION = "unknown";
	
	public static final String RECORD_FORMAT = "cpk_detected_format_facet_str_mv";
	
	public static final String RECORD_FORMAT_DISPLAY = "format_display_mv";
	
	public static final String AUTHOR_FIELD = "author";
	
	public static final String AUTHOR_FIELD_SEARCH = "author_search";
	
	public static final String URL = "url";

	public static final String ISBN = "isbn";

	public static final String NBN = "nbn_display";

	public static final String LOCAL_STATUSES_FACET = "local_statuses_facet_str_mv";
	
	public static final String SFX_LINKS_FIELD = "sfx_links";
	
	public static final String LOAN_RELEVANCE_FIELD = "loanRelevance";

	public static final String FULLTEXT_FIELD = "fulltext";

	public static final String SOLR_HIDDEN_FIELD_PREFIX = "_hidden";

	public static final String FIELD_996b = "996b";

	public static final String TOC = "toc_txt_mv";
	
	public static final String ID_001_SEARCH = "id001_search";

	/**
	 * dummy fields used for processing purposes only, shouldn't be indexed. Fields are deleted in AuthorityEnricher. 
	 */
	public static final String AUTHOR_AUTHORITY_DUMMY_FIELD = "_hidden_authority_dummy_field";

	public static final String AUTHOR_DUMMY_FIELD = "_hidden_authors_dummy";
	
	/**
	 * dummy field for passing kramerius policy, removed in UrlEnricher
	 */
	public static final String KRAMERIUS_DUMMY_RIGTHS = "kramerius_dummy_rights";

}
