package cz.mzk.recordmanager.server.util.identifier;

import cz.mzk.recordmanager.server.marc.marc4j.MarcFactoryImpl;
import cz.mzk.recordmanager.server.model.Ean;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EanUtilsTest {

	private static final MarcFactory MARC_FACTORY = MarcFactoryImpl.newInstance();

	private static final String VALID_EAN = "8593026341407";

	private static final String INVALID_EAN = "1234567891232";

	private static final String VALID_EAN_WITHOUT_ZERO = "615068902687";

	private static DataField eanDataField(final String sfA, final String sfQ) {
		DataField df = MARC_FACTORY.newDataField("024", '3', ' ', "a", sfA);
		if (!sfQ.isEmpty()) df.addSubfield(MARC_FACTORY.newSubfield('q', sfQ));
		return df;
	}

	@Test
	public void isValid() {
		Assert.assertTrue(EANUtils.isEAN13valid(VALID_EAN));
	}

	@Test
	public void isInvalid() {
		Assert.assertFalse(EANUtils.isEAN13valid(INVALID_EAN));
	}

	@Test
	public void isValidWithoutZero() {
		Assert.assertTrue(EANUtils.isEAN13valid(VALID_EAN_WITHOUT_ZERO));
	}

	@Test
	public void createEanObjectTest() {
		Ean ean = new Ean();
		ean.setEan(8593026341407L);
		ean.setOrderInRecord(1L);
		ean.setNote("note");
		Assert.assertEquals(Ean.create(8593026341407L, 1L, "note"), ean);
	}

	@Test
	public void createEanFromDataFieldTest() {
		Assert.assertEquals(EANUtils.createEan(eanDataField("8593026341407", "")),
				Ean.create(8593026341407L, 1L, ""));
		Assert.assertEquals(EANUtils.createEan(eanDataField("8593026341407 vaz", "")),
				Ean.create(8593026341407L, 1L, "vaz"));
		Assert.assertEquals(EANUtils.createEan(eanDataField("8593026341407 vaz", " (note)")),
				Ean.create(8593026341407L, 1L, "vaz note"));
	}

	@Test
	public void getValidEan() {
		// valid
		Long testValid = EANUtils.getEAN13Long(VALID_EAN);
		Assert.assertNotNull(testValid);
		Assert.assertEquals(testValid.longValue(), 8593026341407L);
		// not valid
		Assert.assertNull(EANUtils.getEAN13Long(INVALID_EAN), null);
	}
}
