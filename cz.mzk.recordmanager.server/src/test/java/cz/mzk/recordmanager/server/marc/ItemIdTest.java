package cz.mzk.recordmanager.server.marc;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.marc.marc4j.MarcFactoryImpl;
import cz.mzk.recordmanager.server.model.ItemId;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ItemIdTest extends AbstractTest {

	private static DataField createField996(String... subfields) {
		return MarcFactoryImpl.newInstance().newDataField("996", ' ', ' ', subfields);
	}

	@Test
	public void alephItemIdTest() {
		ItemId type = ItemId.ALEPH;
		String record_id = "MZK01-000891773";
		String sigla = "BOA001";
		DataField df = createField996();

		// empty 996
		Assert.assertNull(ItemId.getItemIdSubfield(type, df, sigla, record_id));

		df = createField996("w", "000891773", "u", "000050", "j", "MZK50");
		Subfield sf = ItemId.getItemIdSubfield(type, df, sigla, record_id);
		Assert.assertNotNull(sf);
		Assert.assertEquals(sf.getCode(), 't');
		Assert.assertEquals(sf.getData(), "BOA001.MZK01000891773.MZK50000891773000050");
	}

	@Test
	public void kohaItemIdTest() {
		ItemId type = ItemId.KOHA;
		String record_id = "80364";
		String sigla = "UOG505";
		DataField df = createField996();

		// empty 996
		Assert.assertNull(ItemId.getItemIdSubfield(type, df, sigla, record_id));

		df = createField996("w", "80364");
		Subfield sf = ItemId.getItemIdSubfield(type, df, sigla, record_id);
		Assert.assertNotNull(sf);
		Assert.assertEquals(sf.getCode(), 't');
		Assert.assertEquals(sf.getData(), "UOG505.80364");
	}

	@Test
	public void dawinciItemIdTest() {
		ItemId type = ItemId.DAWINCI;
		String record_id = "K0174879";
		String sigla = "ABA008";
		DataField df = createField996();

		// empty 996
		Assert.assertNull(ItemId.getItemIdSubfield(type, df, sigla, record_id));

		df = createField996("a", "K0174879/a");
		Subfield sf = ItemId.getItemIdSubfield(type, df, sigla, record_id);
		Assert.assertNotNull(sf);
		Assert.assertEquals(sf.getCode(), 't');
		Assert.assertEquals(sf.getData(), "ABA008.K0174879%2Fa");
	}

	@Test
	public void otherItemIdTest() {
		ItemId type = ItemId.OTHER;
		String record_id = "317000263953";
		String sigla = "HBG001";
		DataField df = createField996();

		// empty 996
		Assert.assertNull(ItemId.getItemIdSubfield(type, df, sigla, record_id));

		df = createField996("b", "317000263953");
		Subfield sf = ItemId.getItemIdSubfield(type, df, sigla, record_id);
		Assert.assertNotNull(sf);
		Assert.assertEquals(sf.getCode(), 't');
		Assert.assertEquals(sf.getData(), "HBG001.317000263953");
	}
}
