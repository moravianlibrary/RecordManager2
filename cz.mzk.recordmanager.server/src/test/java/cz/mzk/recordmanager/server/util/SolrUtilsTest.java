package cz.mzk.recordmanager.server.util;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.marc.marc4j.MarcFactoryImpl;
import org.apache.solr.common.SolrInputDocument;
import org.marc4j.marc.MarcFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SolrUtilsTest {

	private static final MarcFactory MARC_FACTORY = MarcFactoryImpl.newInstance();

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

	@Test
	public void removeEndParenthesesTest() {
		Assert.assertTrue(SolrUtils.removeEndParentheses(Collections.singletonList("(test))")).contains("(test)"));
	}

	@Test
	public void getInd2AsIntTest() {
		Assert.assertEquals(SolrUtils.getInd2AsInt(MARC_FACTORY.newDataField("100", '0', '5')), 5);
		Assert.assertEquals(SolrUtils.getInd2AsInt(MARC_FACTORY.newDataField("100", '0', ' ')), 0);
	}

	@Test
	public void cleanPublisherNameTest() {
		Assert.assertEquals(SolrUtils.cleanPublisherName("  t<es>t n[a]me  ?"), "test name");
	}

	@Test
	public void toUpperCaseFirstCharTest() {
		Assert.assertEquals(SolrUtils.toUpperCaseFirstChar("test name"), "Test name");
	}

	@Test
	public void removeEndPunctuationTest() {
		Assert.assertEquals(SolrUtils.removeEndPunctuation("test name,:"), "test name");
		Assert.assertEquals(SolrUtils.removeEndPunctuation("test name."), "test name.");
		Assert.assertEquals(SolrUtils.removeEndPunctuation("test name.."), "test name.");
	}

	@Test
	public void getNameForDisplay() {
		Assert.assertEquals(SolrUtils.getNameForDisplay(MARC_FACTORY.newDataField(
				"100", '1', ' ', "a", "Name, Test ,", "b", "testb", "c", "testc")),
				"Test Name, testb testc");
	}

	@Test
	public void getNameForExact() {
		Assert.assertEquals(SolrUtils.getNameForExact(MARC_FACTORY.newDataField(
				"100", '1', ' ', "a", "Name, Test ,", "b", "testb", "c", "testc")),
				"Test Name, testb");
	}

	@Test
	public void getVizFieldCode() {
		Assert.assertEquals(SolrUtils.getVizFieldCode("source", "tag", "value"), "source|tag|value");
	}

	@Test
	public void getSubfieldAsString() {
		Assert.assertEquals(SolrUtils.getSubfieldAsString(MARC_FACTORY.newDataField(
				"100", '1', ' ', "a", "Name, Test ,"), 'a'), "Name, Test ,");
		Assert.assertEquals(SolrUtils.getSubfieldAsString(MARC_FACTORY.newDataField(
				"100", '1', ' ', "a", "Name, Test ,"), 'b'), "");
	}

}
