package cz.mzk.recordmanager.server.enrich;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.index.enrich.AvailabilityFacetEnricher;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.SolrUtils;
import org.apache.solr.common.SolrInputDocument;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AvailabilitiFacetEnricherTest extends AbstractTest {

	private static final String PRESENT = "0/present/";
	private static final List<String> ONLINE_STATUSES = SolrUtils.createHierarchicFacetValues(
			Constants.DOCUMENT_AVAILABILITY_ONLINE, Constants.DOCUMENT_AVAILABILITY_ONLINE);
	private static final List<String> ONLINE_UNKNOWN_STATUSES = SolrUtils.createHierarchicFacetValues(
			Constants.DOCUMENT_AVAILABILITY_ONLINE, Constants.DOCUMENT_AVAILABILITY_UNKNOWN);

	@Test
	public void onlineTest() {
		DedupRecord dr = new DedupRecord();
		SolrInputDocument merged = new SolrInputDocument();
		List<SolrInputDocument> locals = new ArrayList<>();

		SolrInputDocument doc1 = EnricherUtils.createDocument(SolrFieldConstants.LOCAL_STATUSES_FACET, ONLINE_STATUSES.toArray(new String[0]));
		SolrInputDocument doc2 = EnricherUtils.createDocument(SolrFieldConstants.LOCAL_STATUSES_FACET, PRESENT);
		locals.add(doc1);
		locals.add(doc2);

		new AvailabilityFacetEnricher().enrich(dr, merged, locals);

		// present only in doc2
		Assert.assertFalse(doc1.getFieldValues(SolrFieldConstants.LOCAL_STATUSES_FACET).contains(PRESENT));
		Assert.assertTrue(doc2.getFieldValues(SolrFieldConstants.LOCAL_STATUSES_FACET).contains(PRESENT));

		// size test
		Assert.assertEquals(doc1.getFieldValues(SolrFieldConstants.LOCAL_STATUSES_FACET).size(), 2);
		Assert.assertEquals(doc2.getFieldValues(SolrFieldConstants.LOCAL_STATUSES_FACET).size(), 3);

		// both must containts online status
		for (SolrInputDocument local : locals) {
			Assert.assertTrue(local.getFieldValues(SolrFieldConstants.LOCAL_STATUSES_FACET).containsAll(ONLINE_STATUSES));
		}
	}

	@Test
	public void unknownTest() {
		DedupRecord dr = new DedupRecord();
		SolrInputDocument merged = new SolrInputDocument();
		List<SolrInputDocument> locals = new ArrayList<>();

		SolrInputDocument doc1 = EnricherUtils.createDocument(SolrFieldConstants.LOCAL_STATUSES_FACET, ONLINE_UNKNOWN_STATUSES.toArray(new String[0]));
		SolrInputDocument doc2 = EnricherUtils.createDocument(SolrFieldConstants.LOCAL_STATUSES_FACET, PRESENT);
		locals.add(doc1);
		locals.add(doc2);

		new AvailabilityFacetEnricher().enrich(dr, merged, locals);

		// present only in doc2
		Assert.assertFalse(doc1.getFieldValues(SolrFieldConstants.LOCAL_STATUSES_FACET).contains(PRESENT));
		Assert.assertTrue(doc2.getFieldValues(SolrFieldConstants.LOCAL_STATUSES_FACET).contains(PRESENT));

		// size test
		Assert.assertEquals(doc1.getFieldValues(SolrFieldConstants.LOCAL_STATUSES_FACET).size(), 2);
		Assert.assertEquals(doc2.getFieldValues(SolrFieldConstants.LOCAL_STATUSES_FACET).size(), 3);

		// both must containts online/unknown status
		for (SolrInputDocument local : locals) {
			Assert.assertTrue(local.getFieldValues(SolrFieldConstants.LOCAL_STATUSES_FACET).containsAll(ONLINE_UNKNOWN_STATUSES));
		}
	}

	@Test
	public void onlineUnknownTest() {
		DedupRecord dr = new DedupRecord();
		SolrInputDocument merged = new SolrInputDocument();
		List<SolrInputDocument> locals = new ArrayList<>();

		SolrInputDocument doc1 = EnricherUtils.createDocument(SolrFieldConstants.LOCAL_STATUSES_FACET, ONLINE_STATUSES.toArray(new String[0]));
		SolrInputDocument doc2 = EnricherUtils.createDocument(SolrFieldConstants.LOCAL_STATUSES_FACET, ONLINE_UNKNOWN_STATUSES.toArray(new String[0]));
		SolrInputDocument doc3 = EnricherUtils.createDocument(SolrFieldConstants.LOCAL_STATUSES_FACET, PRESENT);
		locals.add(doc1);
		locals.add(doc2);
		locals.add(doc3);

		new AvailabilityFacetEnricher().enrich(dr, merged, locals);

		// present only in doc3
		Assert.assertFalse(doc1.getFieldValues(SolrFieldConstants.LOCAL_STATUSES_FACET).contains(PRESENT));
		Assert.assertFalse(doc2.getFieldValues(SolrFieldConstants.LOCAL_STATUSES_FACET).contains(PRESENT));
		Assert.assertTrue(doc3.getFieldValues(SolrFieldConstants.LOCAL_STATUSES_FACET).contains(PRESENT));

		// size test
		Assert.assertEquals(doc1.getFieldValues(SolrFieldConstants.LOCAL_STATUSES_FACET).size(), 3);
		Assert.assertEquals(doc2.getFieldValues(SolrFieldConstants.LOCAL_STATUSES_FACET).size(), 3);
		Assert.assertEquals(doc3.getFieldValues(SolrFieldConstants.LOCAL_STATUSES_FACET).size(), 4);

		// both must containts online/unknown status
		Set<String> expected = new HashSet<>(ONLINE_STATUSES);
		expected.addAll(ONLINE_UNKNOWN_STATUSES);
		for (SolrInputDocument local : locals) {
			Assert.assertTrue(local.getFieldValues(SolrFieldConstants.LOCAL_STATUSES_FACET).containsAll(expected));
		}
	}

}
