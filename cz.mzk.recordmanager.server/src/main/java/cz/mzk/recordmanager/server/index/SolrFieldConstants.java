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
	
	public static final String AUTHOR_VIZ_FIELD = "author_viz";
	
	public static final String AUTHOR_SORT_STR = "author_sort_str";
	
	public static final String AUTHOR_FIND = "author_find";
	
	public static final String AUTHOR_FACET = "author_facet_str_mv";
	
	public static final String CORPORATION_VIZ_FIELD = "corporation_viz";
	
	public static final String SUBJECT_VIZ_FIELD = "subject_viz";
	
	public static final String GENRE_VIZ_FIELD = "genre_viz";
	
	public static final String URL = "url";

	public static final String ISBN = "isbn";

	public static final String NBN = "nbn_display";

	public static final String LOCAL_STATUSES_FACET = "local_statuses_facet_str_mv";
	
	public static final String SFX_LINKS_FIELD = "sfx_links";
	
	public static final String LOAN_RELEVANCE_FIELD = "loanRelevance";

	public static final String FULLTEXT_FIELD = "fulltext";

	public static final String SOLR_HIDDEN_FIELD_PREFIX = "_hidden";

	public static final String TOC = "toc_txt_mv";
	
	public static final String ID_001_SEARCH = "id001_search";
	
	public static final String INSPIRATION = "inspiration";
	
	public static final String SUBJECT_KEYWORDS_SEARCH = "subjectKeywords_search_txt_mv";
	
	public static final String PEOPLE_SEARCH = "people_search_txt_mv";
	
	public static final String SUBJECT_FACET = "subject_facet_str_mv";
	
	public static final String ID_AUTHORITY = "id_authority";
	
	public static final String AUTHOR_CORPORATION_SEARCH = "authorCorporation_search_txt_mv";
	
	public static final String HEADING = "heading";
	
	public static final String USE_FOR = "use_for";
	
	public static final String RECORDTYPE = "recordtype";
	
	public static final String TITLE = "title";
	
	public static final String BARCODES = "barcodes";
	
	public static final String CONSPECTUS_STR_MV = "conspectus_str_mv";
	
	public static final String MERGED_RECORDS = "merged_records_boolean";
	
	public static final String GENRE_FACET = "genre_facet_str_mv";
	
	public static final String INDEXING_WHEN_MERGED = "_hidden_index_when_merged_boolean";

	public static final String SFX_SOURCE_TXT = "sfx_source_txt";
	
	public static final String ITEM_ID_TXT_MV = "item_id_txt_mv";

	/**
	 * dummy fields used for processing purposes only, shouldn't be indexed. Fields are deleted in AuthorityEnricher. 
	 */

	public static final String VIZ_DUMMY_FIELD = "_hidden_viz_dummy_field";

}
