package cz.mzk.recordmanager.server.util.identifier;

import cz.mzk.recordmanager.server.marc.marc4j.MarcFactoryImpl;
import cz.mzk.recordmanager.server.model.Isbn;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ISBNUtilsTest {

	private static final MarcFactory marcFactory = MarcFactoryImpl.newInstance();

	private static DataField isbnDataField(final String sfA, final String sfQ) {
		DataField df = marcFactory.newDataField("020", ' ', ' ', "a", sfA);
		if (!sfQ.isEmpty()) df.addSubfield(marcFactory.newSubfield('q', sfQ));
		return df;
	}

	@Test
	public void toIsbn13Test() {
		// valid isbn 10
		Assert.assertEquals(ISBNUtils.toISBN13StringThrowing("80-200-0980-9"), "9788020009807");
		// valid isbn 10 with x
		Assert.assertEquals(ISBNUtils.toISBN13StringThrowing("3-596-26393-x"), "9783596263936");
		// valid isbn 13
		Assert.assertEquals(ISBNUtils.toISBN13StringThrowing("9788086026923"), "9788086026923");
	}

	@Test
	public void toIsbn13LongTest() {
		// valid isbn 10
		Assert.assertEquals(ISBNUtils.toISBN13LongThrowing("80-200-0980-9").longValue(), 9788020009807L);
	}

	@Test(expectedExceptions = NumberFormatException.class)
	public void noValidISBN10StringThrowingTest() {
		ISBNUtils.toISBN13StringThrowing("80-200-0980-2");
	}

	@Test(expectedExceptions = NumberFormatException.class)
	public void noValidISBN13StringThrowingTest() {
		ISBNUtils.toISBN13StringThrowing("9788086026920");
	}

	@Test(expectedExceptions = NumberFormatException.class)
	public void noValidISBN13LongThrowingTest() {
		ISBNUtils.toISBN13LongThrowing("9788086026920");
	}

	@Test
	public void noValidISBN13Test() {
		Assert.assertNull(ISBNUtils.toISBN13String("9788086026920"));
		Assert.assertNull(ISBNUtils.toISBN13Long("9788086026920"));
	}

	@Test
	public void createISBNTest() {
		Assert.assertEquals(Isbn.create(9788020009807L, 1L, ""),
				ISBNUtils.createIsbn(isbnDataField("80-200-0980-9", "")));
		Assert.assertEquals(Isbn.create(9783596263936L, 1L, "váz"),
				ISBNUtils.createIsbn(isbnDataField("3-596-26393-x", "(váz)")));
		Assert.assertEquals(Isbn.create(9783596263936L, 1L, "note note2"),
				ISBNUtils.createIsbn(isbnDataField("3-596-26393-x note", "(note2)")));
	}

}
