package cz.mzk.recordmanager.server.kramerius.harvest;

import org.testng.Assert;
import org.testng.annotations.Test;

public class KrameriusHarvesterEnumTest {

	@Test
	public void krameriusHarvesterEnumTest() {
		Assert.assertEquals(KrameriusHarvesterEnum.SORTING, KrameriusHarvesterEnum.stringToHarvesterEnum("sorting"));
	}

}
