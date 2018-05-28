package cz.mzk.recordmanager.server.util;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcRecordFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

public class CosmotronUtilsTest extends AbstractTest {

	@Test
	public void getParentIdArticleTest() {
		MarcRecord mr = MarcRecordFactory.recordFactory(Arrays.asList(
				"000 01089naa a22003253i 4500",
				"773 08$wli_us_cat*0637070"
		));
		Assert.assertEquals(CosmotronUtils.getParentId(mr), null);
	}

	@Test
	public void getParentIdBookTest() {
		MarcRecord mr = MarcRecordFactory.recordFactory(Arrays.asList(
				"000 02259cam a2200409 i 4500",
				"773 08$wli_us_cat*0637070"
		));
		Assert.assertEquals(CosmotronUtils.getParentId(mr), "LiUsCat_0637070");

		mr = MarcRecordFactory.recordFactory(Arrays.asList(
				"000 02259cam a2200409 i 4500",
				"773 0 $wli_us_cat*0637070$7nnas"
		));
		Assert.assertEquals(CosmotronUtils.getParentId(mr), "LiUsCat_0637070");

		mr = MarcRecordFactory.recordFactory(Collections.singletonList("000 022"));
		Assert.assertEquals(CosmotronUtils.getParentId(mr), null);
	}

	@Test
	public void existsFields996Test() {
		MarcRecord mr = MarcRecordFactory.recordFactory(Collections.singletonList("100 $aa"));
		Assert.assertFalse(CosmotronUtils.existsFields996(mr));
		mr = MarcRecordFactory.recordFactory(Collections.singletonList("996 $aa"));
		Assert.assertTrue(CosmotronUtils.existsFields996(mr));
	}
}
