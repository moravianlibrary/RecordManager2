package cz.mzk.recordmanager.server.marc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;
import cz.mzk.recordmanager.server.model.Cnb;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.model.Isbn;
import cz.mzk.recordmanager.server.model.Issn;
import cz.mzk.recordmanager.server.model.Title;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;

public class MarcRecordImplTest extends AbstractTest {

	@Autowired
	private HarvestedRecordDAO hrdao;

	@Autowired
	private MarcXmlParser parser;

	@Autowired
	private MetadataRecordFactory metadataFactory;

	@BeforeMethod
	public void init() throws Exception {
		dbUnitHelper.init("dbunit/OAIHarvestTest.xml");
	}

	@Test
	public void getPublicationYearTest() throws Exception {
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<String>();
		data.add("260 $c1977");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getPublicationYear().longValue(),
				1977);
		data.clear();

		data.add("260 $c[1977]");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getPublicationYear().longValue(),
				1977);
		data.clear();

		data.add("260 $cc1977");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getPublicationYear().longValue(),
				1977);
		data.clear();

		data.add("260 $cc[1977]");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getPublicationYear().longValue(),
				1977);
		data.clear();

		data.add("260 $cp1977");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getPublicationYear().longValue(),
				1977);
		data.clear();

		data.add("260 $cp[1977]");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getPublicationYear().longValue(),
				1977);
		data.clear();

		data.add("260 $c1977-2003");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getPublicationYear().longValue(),
				1977);
		data.clear();

		data.add("260 $c1977 printing, c1975");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getPublicationYear().longValue(),
				1977);
		data.clear();

		data.add("260 $c1977-");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getPublicationYear().longValue(),
				1977);
		data.clear();

		data.add("260 $c1977, c1978");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getPublicationYear().longValue(),
				1977);
		data.clear();

		data.add("260 $cApril 15, 1977");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getPublicationYear().longValue(),
				1977);
		data.clear();

		data.add("260 $c1977[i.e. 1971]");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getPublicationYear().longValue(),
				1977);
		data.clear();

		data.add("260 $c<1977->");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getPublicationYear().longValue(),
				1977);
		data.clear();

		data.add("260 $c197-");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertNull(metadataRecord.getPublicationYear());
		data.clear();

		data.add("260 $casdba");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertNull(metadataRecord.getPublicationYear());
		data.clear();
		
		data.add("264 $c<1977->");
		data.add("260 $c<1978->");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getPublicationYear().longValue(),
				1977);
		data.clear();
		
		data.add("008 950928s1981----xr ||||e||||||||||||cze||");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getPublicationYear().longValue(), 1981);
		data.clear();
	}
	
	@Test
	public void getTitleTest() throws Exception{
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<String>();

		data.add("245 $nn$aa$pp$bb");
		data.add("240 $aa$nn$bb$pp");
		data.add("240 $aDeutsche Bibliographie.$pWöchentliches Verzeichnis."
				+ "$nReihe B,$pBeilage, Erscheinungen ausserhalb des Verlags"
				+ "buchhandels :$bAmtsblatt der Deutschen Bibliothek.$kadasd");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		
		Title expectedTitle1 = new Title();
		expectedTitle1.setTitleStr("napb");
		expectedTitle1.setOrderInRecord(1L);
		Title expectedTitle2 = new Title();
		expectedTitle2.setTitleStr("anbp");
		expectedTitle2.setOrderInRecord(2L);
		Title expectedTitle3 = new Title();
		expectedTitle3.setTitleStr("Deutsche Bibliographie.Wöchentliches Verzeichnis."
				+ "Reihe B, Beilage, Erscheinungen ausserhalb des Verlags"
				+ "buchhandels : Amtsblatt der Deutschen Bibliothek.");
		expectedTitle3.setOrderInRecord(3L);
		
		Assert.assertEquals(3, metadataRecord.getTitle().size());
		Assert.assertEquals(metadataRecord.getTitle().get(0),expectedTitle1);
		Assert.assertEquals(metadataRecord.getTitle().get(1),expectedTitle2);
		Assert.assertEquals(metadataRecord.getTitle().get(2),expectedTitle3);
		data.clear();

		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(1, metadataRecord.getTitle().size());
		Assert.assertTrue(metadataRecord.getTitle().get(0).getTitleStr().isEmpty());
		data.clear();
		
	}
	
	@Test
	public void getISSNsTest() throws Exception {
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<String>();
		List<Issn> issns = new ArrayList<Issn>();
		Long issnCounter = 0L;
		
		data.add("022 $a2336-4815");
		Issn issn = new Issn();
		issn.setIssn("2336-4815");
		issn.setOrderInRecord(++issnCounter);
		issn.setNote("");
		issns.add(issn);
		data.add("022 $a1214-4029 (pozn)");
		issn = new Issn();
		issn.setIssn("1214-4029");
		issn.setOrderInRecord(++issnCounter);
		issn.setNote("pozn");
		issns.add(issn);
		data.add("022 $a0231-858X");
		issn = new Issn();
		issn.setIssn("0231-858X");
		issn.setOrderInRecord(++issnCounter);
		issn.setNote("");
		issns.add(issn);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getISSNs().toString(),
				issns.toString());
		data.clear();

		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getISSNs(), Collections.EMPTY_LIST);
		data.clear();
	}
	
	@Test
	public void getCNBsTest() throws Exception {
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<String>();
		List<Cnb> cnbs = new ArrayList<Cnb>();
	
		data.add("015 $acnb001816378");
		Cnb cnb = new Cnb();
		cnb.setCnb("cnb001816378");
		cnbs.add(cnb);
		data.add("015 $acnb001723289$acnb001723290");
		cnb = new Cnb();
		cnb.setCnb("cnb001723289");
		cnbs.add(cnb);
		cnb = new Cnb();
		cnb.setCnb("cnb001723290");
		cnbs.add(cnb);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getCNBs().toString(),
				cnbs.toString());
		data.clear();
	}

	@Test
	public void getPageCountTest() throws Exception {
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<String>();

		data.add("300 $a257 s.");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getPageCount().longValue(), 257);
		data.clear();

		data.add("300 $a1 zvuková deska (78:24)");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getPageCount().longValue(), 1);
		data.clear();

		data.add("300 $a[14] s.");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getPageCount().longValue(), 14);
		data.clear();

		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getPageCount(), null);
		data.clear();
	}
	
	@Test
	public void getISBNsTest() throws Exception {
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<String>();
		List<Isbn> isbnlist = new ArrayList<Isbn>();

		data.add("020 $a9788086026923 (váz)");
		Isbn isbn = new Isbn();
		isbn.setIsbn(9788086026923L);
		isbn.setOrderInRecord(1L);
		isbn.setNote("váz");
		isbnlist.add(isbn);
		data.add("020 $a978-80-7250-482-4$q(váz)$q(q1)$qq2");
		
		isbn = new Isbn();
		isbn.setIsbn(9788072504824L);
		isbn.setOrderInRecord(2L);
		isbn.setNote("váz q1 q2");
		isbnlist.add(isbn);
		data.add("020 $a80-200-0980-9");
		
		isbn = new Isbn();
		isbn.setIsbn(9788020009807L);
		isbn.setOrderInRecord(3L);
		isbnlist.add(isbn);
		data.add("020 $a456");
		
		isbn = new Isbn();
		isbn.setIsbn(9782011668554L);
		isbn.setOrderInRecord(4L);
		isbnlist.add(isbn);
		data.add("020 $a2-01-16-6855-7");
		
		isbn = new Isbn();
		isbn.setIsbn(9782980406003L);
		isbn.setOrderInRecord(5L);
		isbnlist.add(isbn);
		data.add("020 $a2-9804060-07");
		
		isbn = new Isbn();
		isbn.setIsbn(9783925967214L);
		isbn.setOrderInRecord(6L);
		isbnlist.add(isbn);
		data.add("020 $a3-925 967-21-4");
		
		isbn = new Isbn();
		isbn.setIsbn(9783925967214L);
		isbn.setOrderInRecord(7L);
		isbnlist.add(isbn);
		data.add("020 $a  3-925 967-21-4");
		
		isbn = new Isbn();
		isbn.setIsbn(9780521376679L);
		isbn.setOrderInRecord(9L);
		isbnlist.add(isbn);
		data.add("020 $a052137667x");
		
		isbn = new Isbn();
		isbn.setIsbn(9783596263936L);
		isbn.setOrderInRecord(10L);
		isbnlist.add(isbn);
		data.add("020 $a3-596-26393-x");
		
		isbn = new Isbn();
		isbn.setIsbn(9783596263936L);
		isbn.setOrderInRecord(10L);
		isbnlist.add(isbn);
		data.add("020 $a5-268-01286-x");
		
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getISBNs().size(), isbnlist.size());
		for (int i = 0; i < isbnlist.size(); i++) {
			Assert.assertEquals(metadataRecord.getISBNs().get(i), isbnlist.get(i), "ISBN on position " + i + " differs.");
		}
		data.clear();

		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getISBNs(), Collections.EMPTY_LIST);
		data.clear();
	}
	
	@Test
	public void getWeightTest() throws Exception{
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<String>();
		
		data.add("000 01234567890123456a");
		data.add("008 asd");
		data.add("020 $a80-200-0980-9");
		data.add("040 $erda");
		data.add("080 456");
		data.add("100 $aasd");
		data.add("245 00$asasd");
		data.add("300 $asd");
		data.add("752 $7asd");
		data.add("964 $asd");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getWeight(0L).longValue(), 4L);
		data.clear();
	}
	
	@Test
	public void getAuthorAuthStringTest() throws Exception{
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<String>();
		
		data.add("100 $aEliska");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getAuthorString(), "Eliska");
		data.clear();
		
		data.add("700 $aEliska");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getAuthorString(), "Eliska");
		data.clear();
	}
	
	@Test
	public void getAuthorAuthKeyTest() throws Exception{
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<String>();
		
		data.add("100 $7aaa");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getAuthorAuthKey(), "aaa");
		data.clear();
		
		data.add("700 $7bbb");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getAuthorAuthKey(), "bbb");
		data.clear();
	}
	
	@Test
	public void getDetectedFormatListTest() throws Exception {
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<String>();
		List<HarvestedRecordFormatEnum> hrf = new ArrayList<HarvestedRecordFormatEnum>();
		
		// Books
		data.add("000 00000000");
		data.add("006 a");
		hrf.add(HarvestedRecordFormatEnum.BOOKS);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		// Periodicals
		data.add("000 0000000i");
		hrf.add(HarvestedRecordFormatEnum.PERIODICALS);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		// Articles
		data.add("000 0000000a");
		hrf.add(HarvestedRecordFormatEnum.ARTICLES);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		// Maps
		data.add("000 000000000");
		data.add("245 $hkartografický dokument");
		hrf.add(HarvestedRecordFormatEnum.MAPS);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		// Musical
		data.add("000 00000000");
		data.add("336 $bntv");
		hrf.add(HarvestedRecordFormatEnum.MUSICAL_SCORES);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();

		// Vusial documents
		data.add("000 00000000");
		data.add("338 $bgasd");
		hrf.add(HarvestedRecordFormatEnum.VISUAL_DOCUMENTS);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		// Microforms
		data.add("000 0000000");
		data.add("337 $bh");
		hrf.add(HarvestedRecordFormatEnum.OTHER_MICROFORMS);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		// Braill
		data.add("000 00000000");
		data.add("007 fb");
		data.add("245 $hhmatové písmo");
		hrf.add(HarvestedRecordFormatEnum.OTHER_BRAILLE);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);		
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		// Electronic
		data.add("000 000000m");
		data.add("006 plllllq");
		hrf.add(HarvestedRecordFormatEnum.ELECTRONIC_SOURCE);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);		
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		// Audio
		data.add("000 00000000");
		data.add("300 $fanaloaSg$amagnetofonová kazeta");
		hrf.add(HarvestedRecordFormatEnum.AUDIO_CASSETTE);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);		
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		// Video
		data.add("000 000000000");
		data.add("007 vlllv");
		hrf.add(HarvestedRecordFormatEnum.VIDEO_DVD);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);		
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
				
		// Kit
		data.add("000 00000000");
		data.add("006 o");
		data.add("007 o");
		hrf.add(HarvestedRecordFormatEnum.OTHER_KIT);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);		
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		// Object
		data.add("000 00000000");
		data.add("008 zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzd");
		hrf.add(HarvestedRecordFormatEnum.OTHER_OBJECT);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);		
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		// Mix document
		data.add("000 000000p");
		hrf.add(HarvestedRecordFormatEnum.OTHER_MIX_DOCUMENT);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);		
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		// Unspecified
		data.add("000 00000000");
		data.add("337 $bx");
		hrf.add(HarvestedRecordFormatEnum.OTHER_UNSPECIFIED);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);		
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		// Nothing
		data.add("000 00000000");
		hrf.add(HarvestedRecordFormatEnum.OTHER_UNSPECIFIED);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);		
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
	}
	
	@Test
	public void getScaleTest() throws Exception {
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<String>();
		
		data.add("255 $aMěřítko 1:250 000");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertTrue(metadataRecord.getScale().equals(250000L));
		data.clear();
		
		data.add("255 $aMěřítko 1:50^000");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertTrue(metadataRecord.getScale().equals(50000L));
		data.clear();
		
		data.add("255 $$$aMěřítko 1:30^000 (14°59'v.d.-15°33'v.d./50°40's.š.-50°25's.š.)");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertTrue(metadataRecord.getScale().equals(30000L));
		data.clear();
		
		data.add("255 $aMěřítko neuvedeno");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertNull(metadataRecord.getScale());
		data.clear();
		
		data.add("255 $aMěřítko 1:20^000 a 1:150^000");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertTrue(metadataRecord.getScale().equals(20000L));
		data.clear();
		
		data.add("255 $aMěřítko 1:150000");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertTrue(metadataRecord.getScale().equals(150000L));
		data.clear();
	}

	@Test
	public void getUUIDtest() throws Exception {
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<String>();
		
		data.add("856 $uhttp://kramerius.nkp.cz/kramerius/handle/ABA001/1339741$yDigitalizovaný dokument");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertNull(metadataRecord.getUUId());
		data.clear();

		data.add("856 $uhttp://kramerius4.nkp.cz/search/handle/uuid:1b891670-00e4-11e4-89c6-005056827e51$yDigitalizovaný dokument");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getUUId(), "1b891670-00e4-11e4-89c6-005056827e51");
		data.clear();
		
		data.add("856 $uhttp://kramerius4.nkp.cz/search/handle/uuid:abbc47e0-421f-11e4-8113-005056827e52$yDigitalizovaný dokument");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getUUId(), "abbc47e0-421f-11e4-8113-005056827e52");
		data.clear();
	}
	
	@Test
	public void getSeriesISSNtests() throws Exception {
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<String>();
		
		data.add("490 $aVědecké spisy Vysokého učení technického v Brně. PhD Thesis,$x1213-4198 ;$vsv. 744");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getISSNSeries(), "1213-4198 ;");
		Assert.assertEquals(metadataRecord.getISSNSeriesOrder(), "sv. 744");
		data.clear();
		
		data.add("490 $aEkonomika, právo, politika,$x1213-3299 ;$vč. 97/2012");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getISSNSeries(), "1213-3299 ;");
		Assert.assertEquals(metadataRecord.getISSNSeriesOrder(), "č. 97/2012");
		data.clear();
	}
	
	@Test
	public void getClusterIdTest() throws Exception{
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<String>();
		
		data.add("001 0011");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getClusterId(), null);

	}
	
	@Test
	public void getOclcsTest() throws Exception{
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<>();
		
		final String oclc1 = "11608569";
		final String oclc2 = "ocn123456789";
		data.add("035 $a(OCoLC)" + oclc1);
		data.add("035 $a(OCoLC)" + oclc2);
		
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getOclcs().size(), 2);
		Assert.assertEquals(metadataRecord.getOclcs().get(0).getOclcStr(), oclc1);
		Assert.assertEquals(metadataRecord.getOclcs().get(1).getOclcStr(), oclc2);
	}
	
	@Test
	public void getLanguagesTest() throws Exception{
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<>();
		data.add("041 $aeng$acze");
		
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getLanguages().size(), 2);
		
		data = new ArrayList<>();
		data.add("041 $abel");
		
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getLanguages().size(), 1);
		Assert.assertEquals(metadataRecord.getLanguages().get(0).getLangStr(), "oth");
		
		data = new ArrayList<>();
		data.add("008 960925s1891    gw ||||| |||||||||||eng|d");		
		
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getLanguages().size(), 1);
		Assert.assertEquals(metadataRecord.getLanguages().get(0).getLangStr(), "eng");
		
		data = new ArrayList<>();
		data.add("008 960925s1891    gw ||||| |||||||||||bel|d");		
		
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getLanguages().size(), 0);
		

	}
}
