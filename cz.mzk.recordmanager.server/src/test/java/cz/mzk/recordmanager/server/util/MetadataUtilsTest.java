package cz.mzk.recordmanager.server.util;

import org.testng.Assert;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.model.Title;

public class MetadataUtilsTest {

	@Test
	public void testNormalize() {
		Assert.assertEquals(MetadataUtils.normalize("Nechť již hříšné saxofony ďáblů "
				+ "rozzvučí síň úděsnými tóny waltzu, tanga a quickstepu."),
				"nechtjizhrisnesaxofonydablurozzvucisinudesnymitonywaltzutangaaquickstepu");
		Assert.assertEquals(MetadataUtils.normalize("Verbüß öd’ Joch, kämpf Qual, zwing Styx!"),
				"verbuodjochkampfqualzwingstyx");
	}
	
	@Test
	public void testHasTrailingPunctuation() {
		Assert.assertEquals(MetadataUtils.hasTrailingPunctuation("aaa:"), true);
		Assert.assertEquals(MetadataUtils.hasTrailingPunctuation("aaa;"), true);
		Assert.assertEquals(MetadataUtils.hasTrailingPunctuation("aaa,"), true);
		Assert.assertEquals(MetadataUtils.hasTrailingPunctuation("aaa="), true);
		Assert.assertEquals(MetadataUtils.hasTrailingPunctuation("aaa("), true);
		Assert.assertEquals(MetadataUtils.hasTrailingPunctuation("aaa["), true);
		Assert.assertEquals(MetadataUtils.hasTrailingPunctuation("aaa ."), true);

		Assert.assertEquals(MetadataUtils.hasTrailingPunctuation("aaa"), false);
	}

	@Test
	public void testSimilarityEnabled() {
		Title title = new Title();
		// book is forbidden word
		title.setTitleStr("book drg asd");
		Assert.assertFalse(MetadataUtils.similarityEnabled(title));
		title.setTitleStr("asd bOoK drg asd");
		Assert.assertFalse(MetadataUtils.similarityEnabled(title));
		title.setTitleStr("asD aSd BooK");
		Assert.assertFalse(MetadataUtils.similarityEnabled(title));
		title.setTitleStr("bookrg asd");
		Assert.assertTrue(MetadataUtils.similarityEnabled(title));

		// number
		title.setTitleStr("asdrg8 asd");
		Assert.assertFalse(MetadataUtils.similarityEnabled(title));

		title.setTitleStr("asdrg asd asd");
		Assert.assertTrue(MetadataUtils.similarityEnabled(title));
	}

}
