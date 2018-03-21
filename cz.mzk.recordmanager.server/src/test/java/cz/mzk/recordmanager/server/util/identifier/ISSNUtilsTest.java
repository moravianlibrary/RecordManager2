package cz.mzk.recordmanager.server.util.identifier;

import cz.mzk.recordmanager.server.marc.marc4j.MarcFactoryImpl;
import cz.mzk.recordmanager.server.model.Issn;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ISSNUtilsTest {

	private static final MarcFactory MARC_FACTORY = MarcFactoryImpl.newInstance();

	private static final String VALID_ISSN_EXAMPLE = "0317-8471";

	private static final String INVALID_ISSN_EXAMPLE = "1317-8471";

	private static DataField issnDataField(final String sfA, final String sfQ) {
		DataField df = MARC_FACTORY.newDataField("022", ' ', ' ', "a", sfA);
		if (!sfQ.isEmpty()) df.addSubfield(MARC_FACTORY.newSubfield('q', sfQ));
		return df;
	}

	@Test
	public void validISSN() {
		Assert.assertTrue(ISSNUtils.isValid(VALID_ISSN_EXAMPLE));
	}

	@Test
	public void invalidISSN() {
		Assert.assertFalse(ISSNUtils.isValid(INVALID_ISSN_EXAMPLE));
	}

	@Test
	public void createIssnObjectTest() {
		Issn issn = new Issn();
		issn.setIssn("0430-859X");
		issn.setOrderInRecord(1L);
		issn.setNote("note");
		Assert.assertEquals(Issn.create("0430-859X", 1L, "note"), issn);
	}

	@Test
	public void createIssnFromDataFieldTest() {
		Assert.assertEquals(ISSNUtils.createIssn(issnDataField("0430-859X", "")),
				Issn.create("0430-859X", 1L, ""));
		Assert.assertEquals(ISSNUtils.createIssn(issnDataField("0430-859X vaz", "")),
				Issn.create("0430-859X", 1L, "vaz"));
		Assert.assertEquals(ISSNUtils.createIssn(issnDataField("0430-859X vaz", " (note)")),
				Issn.create("0430-859X", 1L, "vaz note"));
	}

	@Test(expectedExceptions = NumberFormatException.class)
	public void getValidIssnThrowingNotValid() {
		// not valid
		Assert.assertNull(ISSNUtils.getValidIssnThrowing(INVALID_ISSN_EXAMPLE));
	}

	@Test(expectedExceptions = NoDataException.class)
	public void getValidIssnThrowingEmpty() {
		// empty string
		Assert.assertNull(ISSNUtils.getValidIssnThrowing(""));
	}
}
