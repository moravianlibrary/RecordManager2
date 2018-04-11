package cz.mzk.recordmanager.server.util;

import cz.mzk.recordmanager.server.marc.marc4j.MarcFactoryImpl;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MarcRecordUtilsTest {

	private static final MarcFactory factory = MarcFactoryImpl.newInstance();

	@Test
	public void parseSubfieldsTest() {
		DataField df = factory.newDataField("100", ' ', ' ', "a", "ahoj", "b", "blanko!", "c", "eva bernardinova");
		Assert.assertEquals(MarcRecordUtils.parseSubfields(df, "", 'a'), "ahoj");
		Assert.assertEquals(MarcRecordUtils.parseSubfields(df, "", 'a', 'b'), "ahojblanko!");
		Assert.assertEquals(MarcRecordUtils.parseSubfields(df, " ", 'a', 'b'), "ahoj blanko!");
	}
}
