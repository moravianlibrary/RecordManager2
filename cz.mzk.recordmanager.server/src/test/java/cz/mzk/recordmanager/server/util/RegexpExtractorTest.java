package cz.mzk.recordmanager.server.util;

import org.testng.Assert;
import org.testng.annotations.Test;

public class RegexpExtractorTest {

	@Test
	public void groupExtractor() {
		RegexpExtractor extractor = new RegexpExtractor("[^:]+:[^:]+:MZK04-(.*)");
		Assert.assertEquals(extractor.extract("oai:aleph.mzk.cz:MZK04-000000001"), "000000001");
	}

	@Test
	public void replacingExtractor() {
		RegexpExtractor extractor = new RegexpExtractor("s/^(.*)/EBSCO-$1/");
		Assert.assertEquals(extractor.extract("ocn11155777"), "EBSCO-ocn11155777");
	}

}
