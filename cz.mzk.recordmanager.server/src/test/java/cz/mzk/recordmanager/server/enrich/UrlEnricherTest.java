package cz.mzk.recordmanager.server.enrich;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.testng.Assert;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.index.enrich.UrlDedupRecordEnricher;
import cz.mzk.recordmanager.server.model.DedupRecord;

public class UrlEnricherTest extends AbstractTest {

	private static final String MZK_ONLINE_MZK_URL = "MZK|online|http://mzk.cz|";
	private static final String MZK_PROTECTED_BRNO_URL = "MZK|protected|http://brno.cz|";
	private static final String MZK_PROTECTED_MZK_URL = "MZK|protected|http://mzk.cz|";
	private static final String MZK_UNKNOWN_BRNO_URL = "MZK|unknown|http://brno.cz|";
	private static final String MZK_UNKNOWN_MZK_TEXT_URL = "MZK|unknown|http://mzk.cz|text";
	private static final String MZK_UNKNOWN_MZK_URL = "MZK|unknown|http://mzk.cz|";
	private static final String MZK_UNKNOWN_TRE_URL = "MZK|unknown|http://tre.cz|";

	private static final String TRE_ONLINE_MZK_URL = "TRE|online|http://mzk.cz|";
	private static final String TRE_UNKNOWN_BRNO_URL = "TRE|unknown|http://brno.cz|";
	private static final String TRE_UNKNOWN_MZK_TEXT_URL = "TRE|unknown|http://mzk.cz|text";
	private static final String TRE_UNKNOWN_MZK_URL = "TRE|unknown|http://mzk.cz|";

	private static final String UNKNOWN_BRNO_URL = "unknown|unknown|http://brno.cz|";
	private static final String UNKNOWN_MZK_TEXT_URL = "unknown|unknown|http://mzk.cz|text";

	private static final String MZKKRAM_PROT_URL = "MZK-KRAM|protected|http://kramerius.mzk.cz/search/i.jsp?pid=uuid:df686290-a590-11e2-8b87-005056827e51|";
	private static final String MZKKRAM_UNKN_URL = "MZK|unknown|http://kramerius.mzk.cz/search/handle/uuid:df686290-a590-11e2-8b87-005056827e51|";

	@Test
	public void notDuplicitUrlTest() {
		DedupRecord dr = new DedupRecord();
		SolrInputDocument merged = new SolrInputDocument();
		List<SolrInputDocument> local = new ArrayList<>();
		local.add(EnricherUtils.createDocument(SolrFieldConstants.URL, MZK_ONLINE_MZK_URL));
		local.add(EnricherUtils.createDocument(SolrFieldConstants.URL, MZK_UNKNOWN_TRE_URL));
		local.add(EnricherUtils.createDocument(SolrFieldConstants.URL, MZK_PROTECTED_BRNO_URL));

		List<String> result = new ArrayList<>();
		result.add(MZK_ONLINE_MZK_URL);
		result.add(MZK_UNKNOWN_TRE_URL);
		result.add(MZK_PROTECTED_BRNO_URL);

		UrlDedupRecordEnricher ue = new UrlDedupRecordEnricher();
		ue.enrich(dr, merged, local);

		Assert.assertTrue(merged.getFieldValues(SolrFieldConstants.URL).containsAll(result));
		// removed URL field from all local records
		local.forEach(l -> Assert.assertNull(l.getFieldValues(SolrFieldConstants.URL)));
	}

	@Test
	public void onlineUrlTest() {
		DedupRecord dr = new DedupRecord();
		SolrInputDocument merged = new SolrInputDocument();
		List<SolrInputDocument> local = new ArrayList<>();
		local.add(EnricherUtils.createDocument(SolrFieldConstants.URL, MZK_ONLINE_MZK_URL));
		local.add(EnricherUtils.createDocument(SolrFieldConstants.URL, TRE_ONLINE_MZK_URL));
		local.add(EnricherUtils.createDocument(SolrFieldConstants.URL, MZK_UNKNOWN_MZK_URL));
		local.add(EnricherUtils.createDocument(SolrFieldConstants.URL, MZK_PROTECTED_MZK_URL));

		List<String> result = new ArrayList<>();
		result.add(MZK_ONLINE_MZK_URL);
		result.add(TRE_ONLINE_MZK_URL);

		UrlDedupRecordEnricher ue = new UrlDedupRecordEnricher();
		ue.enrich(dr, merged, local);

		Assert.assertEquals(merged.getFieldValues(SolrFieldConstants.URL).toArray(), result.toArray());
	}

	@Test
	public void unknownUrlTest() {
		DedupRecord dr = new DedupRecord();
		SolrInputDocument merged = new SolrInputDocument();
		List<SolrInputDocument> local = new ArrayList<>();
		local.add(EnricherUtils.createDocument(SolrFieldConstants.URL, MZK_UNKNOWN_MZK_TEXT_URL));
		local.add(EnricherUtils.createDocument(SolrFieldConstants.URL, TRE_UNKNOWN_MZK_TEXT_URL));
		local.add(EnricherUtils.createDocument(SolrFieldConstants.URL, MZK_UNKNOWN_BRNO_URL));
		local.add(EnricherUtils.createDocument(SolrFieldConstants.URL, TRE_UNKNOWN_BRNO_URL));
		local.add(EnricherUtils.createDocument(SolrFieldConstants.URL, MZK_UNKNOWN_TRE_URL));

		List<String> result = new ArrayList<>();
		result.add(MZK_UNKNOWN_TRE_URL);
		result.add(UNKNOWN_MZK_TEXT_URL);
		result.add(UNKNOWN_BRNO_URL);

		UrlDedupRecordEnricher ue = new UrlDedupRecordEnricher();
		ue.enrich(dr, merged, local);

		Assert.assertEquals(merged.getFieldValues(SolrFieldConstants.URL).toArray(), result.toArray());
	}

	@Test
	public void unknownProtectedUrlTest() {
		DedupRecord dr = new DedupRecord();
		SolrInputDocument merged = new SolrInputDocument();
		List<SolrInputDocument> local = new ArrayList<>();
		local.add(EnricherUtils.createDocument(SolrFieldConstants.URL, TRE_UNKNOWN_MZK_URL));
		local.add(EnricherUtils.createDocument(SolrFieldConstants.URL, MZK_PROTECTED_MZK_URL));

		List<String> result = new ArrayList<>();
		result.add(MZK_PROTECTED_MZK_URL);

		UrlDedupRecordEnricher ue = new UrlDedupRecordEnricher();
		ue.enrich(dr, merged, local);

		Assert.assertEquals(merged.getFieldValues(SolrFieldConstants.URL).toArray(), result.toArray());
	}

	@Test
	public void krameriusUrlTest() {
		DedupRecord dr = new DedupRecord();
		List<SolrInputDocument> local = new ArrayList<>();
		SolrInputDocument merged = new SolrInputDocument();
		local.add(EnricherUtils.createDocument(SolrFieldConstants.URL, MZKKRAM_PROT_URL));
		local.add(EnricherUtils.createDocument(SolrFieldConstants.URL, MZKKRAM_UNKN_URL));

		List<String> result = new ArrayList<>();
		result.add(MZKKRAM_PROT_URL);
		UrlDedupRecordEnricher ue = new UrlDedupRecordEnricher();
		ue.enrich(dr, merged, local);
		Assert.assertEquals(merged.getFieldValues(SolrFieldConstants.URL).toArray(), result.toArray());
	}
}
