package cz.mzk.recordmanager.server.util.identifier;

import cz.mzk.recordmanager.server.marc.marc4j.MarcFactoryImpl;
import cz.mzk.recordmanager.server.model.Ismn;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ISMNUtilsTest {

	private static final MarcFactory MARC_FACTORY = MarcFactoryImpl.newInstance();

	private static DataField ismnDataField(final String sfA, final String sfQ) {
		DataField df = MARC_FACTORY.newDataField("024", '2', ' ', "a", sfA);
		if (!sfQ.isEmpty()) df.addSubfield(MARC_FACTORY.newSubfield('q', sfQ));
		return df;
	}

	@Test
	public void createIsmnObjectTest() {
		Ismn ismn = new Ismn();
		ismn.setIsmn(9790260001251L);
		ismn.setOrderInRecord(1L);
		ismn.setNote("note");
		Assert.assertEquals(Ismn.create(9790260001251L, 1L, "note"), ismn);
	}

	@Test
	public void createIsmnFromDataFieldTest() {
		Assert.assertEquals(ISMNUtils.createIsmn(ismnDataField("M-66056-061-7", "")),
				Ismn.create(9790660560617L, 1L, ""));
		Assert.assertEquals(ISMNUtils.createIsmn(ismnDataField("M-66056-061-7 vaz", "")),
				Ismn.create(9790660560617L, 1L, "vaz"));
		Assert.assertEquals(ISMNUtils.createIsmn(ismnDataField("M-66056-061-7 vaz", " (note)")),
				Ismn.create(9790660560617L, 1L, "vaz note"));
	}

	@Test
	public void toIsmn13LongThrowing() {
		Assert.assertEquals(ISMNUtils.toIsmn13LongThrowing("M-66056-061-7").longValue(), 9790660560617L);
	}

	@Test(expectedExceptions = NumberFormatException.class)
	public void toIsmn13LongThrowingNoValid() {
		ISMNUtils.toIsmn13LongThrowing("M-66056-06a-8");
	}
}
