package cz.mzk.recordmanager.server.enrich;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.index.enrich.DedupRecordEnricher;
import cz.mzk.recordmanager.server.index.enrich.UrlDedupRecordEnricher;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.util.Constants;
import org.apache.solr.common.SolrInputDocument;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

public class AvailabilitiFacetEnricherTest extends AbstractTest {

	private static final String URL = "mzk|%s|mzk.cz|comment";
	private static final String DIFFERENT_URL = "mzk|%s|mzk1.cz|comment";
	private static final String DIFFERENT_URL2 = "mzk|%s|mzk2.cz|comment";

	@Test
	public void onlineTest() {
		DedupRecord dr = new DedupRecord();
		SolrInputDocument merged = new SolrInputDocument();
		List<SolrInputDocument> locals = new ArrayList<>();

		SolrInputDocument doc1 = EnricherUtils.createDocument(SolrFieldConstants.URL,
				String.format(URL, Constants.DOCUMENT_AVAILABILITY_ONLINE));
		SolrInputDocument doc2 = EnricherUtils.createDocument();
		locals.add(doc1);
		locals.add(doc2);

		new UrlDedupRecordEnricher().enrich(dr, merged, locals);

		// merged must containts online status
		Assert.assertTrue(merged.getFieldValues(SolrFieldConstants.STATUSES_FACET).contains(Constants.DOCUMENT_AVAILABILITY_ONLINE));

	}

	@Test
	public void unknownTest() {
		DedupRecord dr = new DedupRecord();
		SolrInputDocument merged = new SolrInputDocument();
		List<SolrInputDocument> locals = new ArrayList<>();

		SolrInputDocument doc1 = EnricherUtils.createDocument(SolrFieldConstants.URL,
				String.format(URL, Constants.DOCUMENT_AVAILABILITY_UNKNOWN));
		SolrInputDocument doc2 = EnricherUtils.createDocument();
		locals.add(doc1);
		locals.add(doc2);

		new UrlDedupRecordEnricher().enrich(dr, merged, locals);

		// merged must containts unknown status
		Assert.assertTrue(merged.getFieldValues(SolrFieldConstants.STATUSES_FACET).contains(Constants.DOCUMENT_AVAILABILITY_UNKNOWN));
	}

	@Test
	public void onlineUnknownTest() {
		DedupRecord dr = new DedupRecord();
		SolrInputDocument merged = new SolrInputDocument();
		List<SolrInputDocument> locals = new ArrayList<>();

		SolrInputDocument doc1 = EnricherUtils.createDocument(SolrFieldConstants.URL,
				String.format(URL, Constants.DOCUMENT_AVAILABILITY_ONLINE));
		SolrInputDocument doc2 = EnricherUtils.createDocument(SolrFieldConstants.URL,
				String.format(DIFFERENT_URL, Constants.DOCUMENT_AVAILABILITY_UNKNOWN));
		SolrInputDocument doc3 = EnricherUtils.createDocument();
		locals.add(doc1);
		locals.add(doc2);
		locals.add(doc3);

		new UrlDedupRecordEnricher().enrich(dr, merged, locals);

		// both must containts online/unknown status
		Set<String> expected = new HashSet<>();
		expected.add(Constants.DOCUMENT_AVAILABILITY_ONLINE);
		expected.add(Constants.DOCUMENT_AVAILABILITY_UNKNOWN);
		Assert.assertTrue(merged.getFieldValues(SolrFieldConstants.STATUSES_FACET).containsAll(expected));
	}

	@Test
	public void availabilityTest() {
		SolrInputDocument merged = new SolrInputDocument();
		List<SolrInputDocument> locals = new ArrayList<>();
		Collection<String> resultAvailabilities = Arrays.asList(
				Constants.DOCUMENT_AVAILABILITY_UNKNOWN,
				Constants.DOCUMENT_AVAILABILITY_ONLINE,
				Constants.DOCUMENT_AVAILABILITY_PROTECTED
		);

		SolrInputDocument doc1 = EnricherUtils.createDocument(SolrFieldConstants.URL,
				String.format(URL, Constants.DOCUMENT_AVAILABILITY_ONLINE));
		SolrInputDocument doc2 = EnricherUtils.createDocument(SolrFieldConstants.URL,
				String.format(DIFFERENT_URL, Constants.DOCUMENT_AVAILABILITY_PROTECTED));
		SolrInputDocument doc3 = EnricherUtils.createDocument(SolrFieldConstants.URL,
				String.format(DIFFERENT_URL2, Constants.DOCUMENT_AVAILABILITY_UNKNOWN));
		locals.add(doc1);
		locals.add(doc2);
		locals.add(doc3);

		DedupRecordEnricher hre = new UrlDedupRecordEnricher();
		hre.enrich(new DedupRecord(), merged, locals);

		// local records not contains statuses field
		for (SolrInputDocument local : locals) {
			Assert.assertFalse(local.containsKey(SolrFieldConstants.STATUSES_FACET));
		}

		// statuses field in merged record
		Assert.assertTrue(merged.containsKey(SolrFieldConstants.STATUSES_FACET));
		Assert.assertTrue(merged.getFieldValues(SolrFieldConstants.STATUSES_FACET).containsAll(resultAvailabilities));
	}

}
