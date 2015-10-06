package cz.mzk.recordmanager.server.util;

import java.util.Arrays;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.testng.Assert;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;

public class SolrUtilsTest {

	@Test
	public void createHierarchicFacetValues1() {
		List<String> result = SolrUtils.createHierarchicFacetValues("Brno");
		Assert.assertEquals(result.size(), 1);
		Assert.assertEquals(result.get(0), "0/Brno/");
	}

	@Test
	public void createHierarchicFacetValues2() {
		List<String> result = SolrUtils.createHierarchicFacetValues("Brno", "Kounicova");
		Assert.assertEquals(result.size(), 2);
		Assert.assertEquals(result.get(0), "0/Brno/");
		Assert.assertEquals(result.get(1), "1/Brno/Kounicova/");
	}

	@Test
	public void createHierarchicFacetValues3() {
		List<String> result = SolrUtils.createHierarchicFacetValues("Brno", "Kounicova", "65");
		Assert.assertEquals(result.size(), 3);
		Assert.assertEquals(result.get(0), "0/Brno/");
		Assert.assertEquals(result.get(1), "1/Brno/Kounicova/");
		Assert.assertEquals(result.get(2), "2/Brno/Kounicova/65/");
	}

	@Test
	public void sort() {
		SolrInputDocument a = new SolrInputDocument();
		a.setField("id", "MZK-001");
		a.setField(SolrFieldConstants.WEIGHT, 12);

		SolrInputDocument b = new SolrInputDocument();
		b.setField("id", "MZK-002");
		b.setField(SolrFieldConstants.WEIGHT, 18);

		SolrInputDocument c = new SolrInputDocument();
		c.setField("id", "MZK-003");
		c.setField(SolrFieldConstants.WEIGHT, 15);

		List<SolrInputDocument> childs = Arrays.asList(a, b, c);
		SolrUtils.sortByWeight(childs);
		Assert.assertEquals((Object) childs.get(0), b);
		Assert.assertEquals((Object) childs.get(1), c);
		Assert.assertEquals((Object) childs.get(2), a);
	}

}
