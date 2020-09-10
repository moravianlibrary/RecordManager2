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
	
	@Test 
	public void replacing2Extractor() {
		RegexpExtractor extractor = new RegexpExtractor("s/^[^:]+:[^:]+:([^\\/]+)\\/(.+)/$1_$2/");
		Assert.assertEquals(extractor.extract("oai:kramerius.nkp.cz:p-ABA001/1"), "p-ABA001_1");
	}

	@Test
	public void replacingCosmotronCbvkExtractor() {
		RegexpExtractor extractor = new RegexpExtractor("s/[^:]+:[^:]+:[^\\\\/]+\\\\/([^\\\\/]+)/cbvk_us_cat*$1/");
		Assert.assertEquals(extractor.extract("oai:katalog.cbvk.cz:CbvkUsCat/m0000003"), "cbvk_us_cat*m0000003");
	}

}
