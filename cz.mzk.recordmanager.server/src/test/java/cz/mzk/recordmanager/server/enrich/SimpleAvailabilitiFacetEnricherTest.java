package cz.mzk.recordmanager.server.enrich;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.index.enrich.FromLocalToDedupEnricher;
import cz.mzk.recordmanager.server.index.enrich.HarvestedRecordEnricher;
import cz.mzk.recordmanager.server.index.enrich.SimpleAvailabilityFacetHarvestedRecordEnricher;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.util.Constants;
import org.apache.solr.common.SolrInputDocument;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class SimpleAvailabilitiFacetEnricherTest extends AbstractTest {

	@Test
	public void availabilityTest() {
		DedupRecord dr = new DedupRecord();
		SolrInputDocument merged = new SolrInputDocument();
		List<SolrInputDocument> locals = new ArrayList<>();
		Collection<String> resultAvailabilities = Arrays.asList(
				Constants.DOCUMENT_AVAILABILITY_UNKNOWN,
				Constants.DOCUMENT_AVAILABILITY_ONLINE,
				Constants.DOCUMENT_AVAILABILITY_PROTECTED
		);

		SolrInputDocument doc1 = EnricherUtils.createDocument(SolrFieldConstants.STATUSES_FACET, Constants.DOCUMENT_AVAILABILITY_ONLINE);
		SolrInputDocument doc2 = EnricherUtils.createDocument(SolrFieldConstants.STATUSES_FACET, Constants.DOCUMENT_AVAILABILITY_PROTECTED);
		SolrInputDocument doc3 = EnricherUtils.createDocument(SolrFieldConstants.STATUSES_FACET, Constants.DOCUMENT_AVAILABILITY_UNKNOWN);
		locals.add(doc1);
		locals.add(doc2);
		locals.add(doc3);

		HarvestedRecordEnricher hre = new SimpleAvailabilityFacetHarvestedRecordEnricher();
		hre.enrich(new HarvestedRecord(), doc1);
		hre.enrich(new HarvestedRecord(), doc2);
		hre.enrich(new HarvestedRecord(), doc3);
		new FromLocalToDedupEnricher().enrich(dr, merged, locals);

		// local records not contains statuses field
		for (SolrInputDocument local : locals) {
			Assert.assertFalse(local.containsKey(SolrFieldConstants.STATUSES_FACET));
		}

		// statuses field in merged record
		Assert.assertTrue(merged.containsKey(SolrFieldConstants.STATUSES_FACET));
		Assert.assertTrue(merged.getFieldValues(SolrFieldConstants.STATUSES_FACET).containsAll(resultAvailabilities));
	}

}
