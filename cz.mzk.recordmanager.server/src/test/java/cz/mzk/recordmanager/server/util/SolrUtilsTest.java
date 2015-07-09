package cz.mzk.recordmanager.server.util;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

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

}
