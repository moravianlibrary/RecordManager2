package cz.mzk.recordmanager.server.dc;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;
import cz.mzk.recordmanager.server.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class DublinCoreRecordImplTest extends AbstractTest {

	@Autowired
	private MetadataRecordFactory metadataFactory;

	/* --- DublinCoreRecordImpl Tests --- */

	@Test
	public void getDateTest() {
		DublinCoreRecord rec = new DublinCoreRecordImpl();
		String date = rec.getFirstDate();
		Assert.assertEquals(null, date);
		Assert.assertTrue(rec.getDates().isEmpty());

		String testDate = "1984";
		String testDate2 = "2015";

		rec.addDate(testDate);
		rec.addDate(testDate2);

		date = rec.getFirstDate();
		Assert.assertEquals(testDate, date);

		String dateFromList = rec.getDates().get(0);
		Assert.assertEquals(testDate, dateFromList);

		dateFromList = rec.getDates().get(1);
		Assert.assertEquals(testDate2, dateFromList);
	}

	@Test
	public void getTitleTest() {
		DublinCoreRecord rec = new DublinCoreRecordImpl();

		String title = rec.getFirstTitle();
		Assert.assertEquals(null, title);
		Assert.assertTrue(rec.getTitles().isEmpty());

		String testTitle = "Babička";
		String testTitle2 = "Dědeček hříbeček";

		rec.addTitle(testTitle);
		rec.addTitle(testTitle2);

		title = rec.getFirstTitle();
		Assert.assertEquals(testTitle, title);

		String titleFromList = rec.getTitles().get(0);
		Assert.assertEquals(testTitle, titleFromList);

		titleFromList = rec.getTitles().get(1);
		Assert.assertEquals(testTitle2, titleFromList);
	}

	/**
	 * no short titles in Dublin Core
	 */
	@Test
	public void getShortTitleTest() {
		Assert.assertTrue(metadataFactory.getMetadataRecord(new DublinCoreRecordImpl()).getShortTitles().isEmpty());
	}

	@Test
	public void getIdentifierTest() {
		DublinCoreRecord rec = new DublinCoreRecordImpl();

		String identifier = rec.getFirstIdentifier();
		Assert.assertEquals(null, identifier);

		String testIdentifier = "isbn:9780091949808";
		String testIdentifier2 = "uuid:ee7a8da0-a925-11e3-a5e2-0800200c9a66";

		rec.addIdentifier(testIdentifier);
		rec.addIdentifier(testIdentifier2);

		String identifierFromList = rec.getIdentifiers().get(0);
		Assert.assertEquals(testIdentifier, identifierFromList);

		identifierFromList = rec.getIdentifiers().get(1);
		Assert.assertEquals(testIdentifier2, identifierFromList);

	}

	/* --- _Metadata_ DublinCoreRecord tests --- */
	@Test
	public void getPublicationYearTest() throws Exception {

		DublinCoreRecord dcr = new DublinCoreRecordImpl();
		MetadataRecord metadataRecord;

		dcr.addDate("1982");
		metadataRecord = metadataFactory.getMetadataRecord(dcr);
		Assert.assertEquals(metadataRecord.getPublicationYear().longValue(),
				1982);
	}

	@Test
	public void getPublicationTitleTest() throws Exception {

		DublinCoreRecord dcr = new DublinCoreRecordImpl();
		MetadataRecord metadataRecord;

		String titleStr = "DesIgnatIo IConographICa OberLeVtensDorfenses PannarIas OffICInas"
				+ " VVLgo FabrICas PenICILLI arbItrIo Representans";
		dcr.addTitle(titleStr);

		metadataRecord = metadataFactory.getMetadataRecord(dcr);

		Assert.assertTrue(metadataRecord.getTitle().contains(Title.create(titleStr, 1L, false)));
	}

	@Test
	public void getISSNsTest() throws Exception {
		DublinCoreRecord dcr = new DublinCoreRecordImpl();
		MetadataRecord metadataRecord;

		String issn1str = "issn:0322-9580";
		String issn2str = "ISSN:1211-068X";
		String notIssn = "hi:I:am:very:specific:identifier";
		dcr.addIdentifier(issn1str);
		dcr.addIdentifier(notIssn);
		dcr.addIdentifier(issn2str);

		metadataRecord = metadataFactory.getMetadataRecord(dcr);
		List<Issn> issns = metadataRecord.getISSNs();

		Assert.assertTrue(issns.contains(Issn.create("0322-9580", 1L, "")));
		Assert.assertFalse(issns.contains(Issn.create(notIssn, 2L, "")));
		Assert.assertTrue(issns.contains(Issn.create("1211-068X", 2L, "")));
	}

	@Test
	public void getCNBsTest() throws Exception {
		DublinCoreRecord dcr = new DublinCoreRecordImpl();
		MetadataRecord metadataRecord;

		// from K4 NDK NK - uuid:2bc5f0f0-987e-11e2-9a08-005056827e52
		String cnb1str = "ccnb:cnb000121063";
		//from Kram MZK - uuid:b602bb03-da24-4724-893d-23f9f3344498
		String cnb2str = "ccnb: cnb000790921";
		String notCnbStr = "uuid:not:cnb:identifier";
		dcr.addIdentifier(cnb1str);
		dcr.addIdentifier(notCnbStr);
		dcr.addIdentifier(cnb2str);

		metadataRecord = metadataFactory.getMetadataRecord(dcr);
		List<Cnb> cnbs = metadataRecord.getCNBs();

		Cnb cnb1 = Cnb.create("cnb000121063");
		Assert.assertTrue(cnbs.contains(cnb1));

		Cnb cnbN = Cnb.create(notCnbStr);
		Assert.assertFalse(cnbs.contains(cnbN));

		Cnb cnb2 = Cnb.create("cnb000790921");
		Assert.assertTrue(cnbs.contains(cnb2));
	}

	@Test
	public void getISBNsTest() throws Exception {
		DublinCoreRecord dcr = new DublinCoreRecordImpl();
		MetadataRecord metadataRecord;

		String isbnstr = "isbn:80-214-1182-1";
		String isbn2str = "0-582-53020-2";
		String notIsbnStr = "uuid:not:cnb:identifier";
		String noValidIsbnStr = "0-582-53020-255555";
		dcr.addIdentifier(isbnstr);
		dcr.addIdentifier(notIsbnStr);
		dcr.addIdentifier(isbn2str);
		dcr.addIdentifier(noValidIsbnStr);

		metadataRecord = metadataFactory.getMetadataRecord(dcr);
		List<Isbn> isbns = metadataRecord.getISBNs();

		Isbn isbn1 = Isbn.create(9788021411821L, 1L, "");
		Assert.assertTrue(isbns.contains(isbn1));

		Isbn isbn2 = Isbn.create(9780582530201L, 2L, "");
		Assert.assertTrue(isbns.contains(isbn2));

		Assert.assertTrue(isbns.size() == 2);
	}

	@Test
	public void getISMNsTest() throws Exception {
		DublinCoreRecord dcr = new DublinCoreRecordImpl();
		MetadataRecord metadataRecord;

		String ismn1str = "ismn:M-66051-073-5(comment)";
		String ismn2str = "ismn:M-66051-0753-5"; // bad
		dcr.addIdentifier(ismn1str);
		dcr.addIdentifier(ismn2str);

		metadataRecord = metadataFactory.getMetadataRecord(dcr);
		List<Ismn> ismns = metadataRecord.getISMNs();

		Assert.assertTrue(ismns.contains(Ismn.create(9790660510735L, 1L, "")));
		Assert.assertTrue(ismns.size() == 1);
	}

	/**
	 * no Ean in DublinCore
	 */
	@Test
	public void getEANsTest() {
		DublinCoreRecord dcr = new DublinCoreRecordImpl();
		MetadataRecord metadataRecord;

		metadataRecord = metadataFactory.getMetadataRecord(dcr);
		List<Ean> eans = metadataRecord.getEANs();

		Assert.assertTrue(eans.size() == 0);
	}

	/**
	 * no {@link PublisherNumber} in Dublin Core
	 */
	@Test
	public void getPublisherNumberTest() {
		Assert.assertTrue(metadataFactory.getMetadataRecord(new DublinCoreRecordImpl()).getPublisherNumber().isEmpty());
	}

	/**
	 * return first identifier
	 */
	@Test
	public void getUniqueIdTest() {
		DublinCoreRecord dcr = new DublinCoreRecordImpl();

		String ID1str = "ID1";
		String ID2str = "ID2";
		dcr.addIdentifier(ID1str);
		dcr.addIdentifier(ID2str);

		Assert.assertEquals(metadataFactory.getMetadataRecord(dcr).getUniqueId(), ID1str);
	}

}
