package cz.mzk.recordmanager.server.marc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.mzk.recordmanager.server.model.*;
import org.hamcrest.core.Is;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.metadata.CitationRecordType;
import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
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
		
		List<Title> titles = metadataRecord.getTitle();
		Assert.assertEquals(3, titles.size());
		Assert.assertEquals(titles.get(0),expectedTitle1);
		Assert.assertEquals(titles.get(1),expectedTitle2);
		Assert.assertEquals(titles.get(2),expectedTitle3);
		data.clear();

		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(0, metadataRecord.getTitle().size());
		data.clear();
	}
	
	@Test
	public void shortTitleTest() throws Exception {
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<String>();

		data.add("245 $nn$aa$pp$bb");
		data.add("240 $aa$nn$bb$pp");
		data.add("245 $aa$pp$nn");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		
		ShortTitle expectedST1 = new ShortTitle();
		expectedST1.setShortTitleStr("nap");
		expectedST1.setOrderInRecord(1L);
		ShortTitle expectedST2 = new ShortTitle();
		expectedST2.setShortTitleStr("anp");
		expectedST2.setOrderInRecord(2L);
		
		Assert.assertEquals(2, metadataRecord.getShortTitles().size());
		Assert.assertEquals(metadataRecord.getShortTitles().get(0),expectedST1);
		Assert.assertEquals(metadataRecord.getShortTitles().get(1),expectedST2);
	}
	
	@Test
	public void getISSNsTest() throws Exception {
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<>();
		List<Issn> issns = new ArrayList<>();
		Long issnCounter = 0L;

		// no issn test
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getISSNs(), Collections.EMPTY_LIST);
		data.clear();

		// create issn
		data.add("022 $a2336-4815");
		issns.add(Issn.create("2336-4815", ++issnCounter, ""));

		data.add("022 $a1214-4029 (pozn)");
		issns.add(Issn.create("1214-4029", ++issnCounter, "pozn"));

		data.add("022 $a0231-858X");
		issns.add(Issn.create("0231-858X", ++issnCounter, ""));

		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getISSNs().toString(), issns.toString());
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
		Assert.assertEquals(metadataRecord.getPageCount().longValue(), 78);
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
		List<String> data = new ArrayList<>();
		List<Isbn> isbnlist = new ArrayList<>();

		// no isbn test
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getISBNs(), Collections.EMPTY_LIST);

		// create isbns
		data.add("020 $a9788086026923 (váz)");
		isbnlist.add(Isbn.create(9788086026923L, 1L, "váz"));

		data.add("020 $a978-80-7250-482-4$q(váz)$q(q1)$qq2");
		isbnlist.add(Isbn.create(9788072504824L, 2L, "váz q1 q2"));

		data.add("020 $a80-200-0980-9");
		isbnlist.add(Isbn.create(9788020009807L, 3L, ""));

		// invalid isbn
		data.add("020 $a456");

		isbnlist.add(Isbn.create(9782011668554L, 4L, ""));
		data.add("020 $a2-01-16-6855-7");

		isbnlist.add(Isbn.create(9782980406003L, 5L, ""));
		data.add("020 $a2-9804060-07");

		isbnlist.add(Isbn.create(9783925967214L, 6L, ""));
		data.add("020 $a3-925 967-21-4");

		isbnlist.add(Isbn.create(9783925967214L, 7L, ""));
		data.add("020 $a  3-925 967-21-4");

		isbnlist.add(Isbn.create(9780521376679L, 8L, ""));
		data.add("020 $a052137667x");

		isbnlist.add(Isbn.create(9783596263936L, 9L, ""));
		data.add("020 $a3-596-26393-x");

		isbnlist.add(Isbn.create(9785268012866L, 10L, ""));
		data.add("020 $a5-268-01286-x");

		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		List<Isbn> results = metadataRecord.getISBNs();
		Assert.assertEquals(results.size(), isbnlist.size());
		for (int i = 0; i < isbnlist.size(); i++) {
			Assert.assertEquals(results.get(i), isbnlist.get(i), String.format("ISBN on position %d differs.", i));
		}
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
		Assert.assertEquals(metadataRecord.getWeight(0L).longValue(), 5L);
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
		hrf.add(HarvestedRecordFormatEnum.BLIND_BRAILLE);
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
		hrf.add(HarvestedRecordFormatEnum.OTHER_OTHER);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);		
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		// Object
		data.add("000 00000000");
		data.add("008 zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzd");
		hrf.add(HarvestedRecordFormatEnum.OTHER_OTHER);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);		
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		// Mix document
		data.add("000 000000p");
		hrf.add(HarvestedRecordFormatEnum.OTHER_OTHER);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);		
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		// Unspecified
		data.add("000 00000000");
		data.add("337 $bx");
		hrf.add(HarvestedRecordFormatEnum.OTHER_OTHER);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);		
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		// Nothing
		data.add("000 00000000");
		hrf.add(HarvestedRecordFormatEnum.OTHER_OTHER);
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
		Assert.assertTrue(metadataRecord.getLanguages().contains("oth"));
		
		data = new ArrayList<>();
		data.add("008 960925s1891    gw ||||| |||||||||||eng|d");		
		
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getLanguages().size(), 1);
		Assert.assertTrue(metadataRecord.getLanguages().get(0).contains("eng"));
		
		data = new ArrayList<>();
		data.add("008 960925s1891    gw ||||| |||||||||||bel|d");		
		
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getLanguages().size(), 0);
		

	}
	
	@Test
	public void getCitationFormatAcademicWork() throws Exception{
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<String>();
		
		data.add("502 $atest");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getCitationFormat(), CitationRecordType.ACADEMIC_WORK);
		data.clear();
	}
	
	@Test
	public void getCitationFormatBook() throws Exception{
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<String>();
		
		data.add("000 000000ac");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getCitationFormat(), CitationRecordType.BOOK);
		data.clear();
	}
	
	@Test
	public void getCitationFormatElectronicBook() throws Exception{
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<String>();
		
		data.add("000 000000td");
		data.add("856 41$atest");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getCitationFormat(), CitationRecordType.ELECTRONIC_BOOK);
		data.clear();
	}
	
	@Test
	public void getCitationFormatPeriodical() throws Exception{
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<String>();
		
		data.add("000 0000000i");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getCitationFormat(), CitationRecordType.PERIODICAL);
		data.clear();
	}
	
	@Test
	public void getCitationFormatElectronicPeriodical() throws Exception{
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<String>();
		
		data.add("000 0000000s");
		data.add("856 41$atest");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getCitationFormat(), CitationRecordType.ELECTRONIC_PERIODICAL);
		data.clear();
	}
	
	@Test
	public void getCitationFormatContribution() throws Exception{
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<String>();
		
		data.add("000 000000000");
		data.add("773 $asborník");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getCitationFormat(), CitationRecordType.CONTRIBUTION_PROCEEDINGS);
		data.clear();
	}
	
	@Test
	public void getCitationFormatElectronicContribution() throws Exception{
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<String>();
		
		data.add("000 000000000");
		data.add("773 $aproceedings");
		data.add("856 41$atest");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getCitationFormat(), CitationRecordType.ELECTRONIC_CONTRIBUTION_PROCEEDINGS);
		data.clear();
	}
	
	@Test
	public void getCitationFormatArticle() throws Exception{
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<String>();
		
		data.add("000 0000000a");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getCitationFormat(), CitationRecordType.ARTICLE);
		data.clear();
	}
	
	@Test
	public void getCitationFormatElectronicArticles() throws Exception{
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<String>();
		
		data.add("000 0000000b");
		data.add("856 41$atest");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getCitationFormat(), CitationRecordType.ELECTRONIC_ARTICLE);
		data.clear();
	}
	
	@Test
	public void getCitationFormatMap() throws Exception{
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<String>();
		
		data.add("000 000000e");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getCitationFormat(), CitationRecordType.MAPS);
		data.clear();
		
		data.add("000 000000f");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getCitationFormat(), CitationRecordType.MAPS);
		data.clear();
	}
	
	@Test
	public void getCitationFormatOthers() throws Exception{
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<String>();
		
		data.add("000 000000c");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getCitationFormat(), CitationRecordType.OTHERS);
		data.clear();
	}
	
	@Test
	public void getCitationFormatError() throws Exception{
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<String>();
		
		data.add("000 00000000");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getCitationFormat(), CitationRecordType.ERROR);
		data.clear();
	}
	
	@Test
	public void getISMNsTest() throws Exception {
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<>();
		List<Ismn> ismnlist = new ArrayList<>();

		// no ismn test
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getISMNs(), Collections.EMPTY_LIST);
		data.clear();

		// create ismn
		data.add("024 2 $aM-2600-0224-1$q(brož.)");
		ismnlist.add(Ismn.create(9790260002241L, 1L, "brož."));

		data.add("024 2 $a979-0-2600-0125-1$q(sešity v obálce)");
		ismnlist.add(Ismn.create(9790260001251L, 2L, "sešity v obálce"));

		data.add("024 2 $aM-66056-061-7$q(Praha ;$qv hudebnině neuvedeno ;$qbrož.)");
		ismnlist.add(Ismn.create(9790660560617L, 3L, "Praha v hudebnině neuvedeno ; brož."));

		data.add("024 2 $aM-66056-061-7 (Brno)$q(Praha)");
		ismnlist.add(Ismn.create(9790660560617L, 4L, "Brno Praha"));

		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		List<Ismn> results = metadataRecord.getISMNs();
		Assert.assertEquals(results.size(), ismnlist.size());
		for (int i = 0; i < ismnlist.size(); i++) {
			Assert.assertEquals(results.get(i), ismnlist.get(i), String.format("ISMN on position %d differs.", i));
		}
	}

	@Test
	public void getEANsTest() throws Exception {
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<>();
		List<Ean> eanList = new ArrayList<>();
		data.add("024 3 $a4006381333931$q(brož.)");
		Ean ean = new Ean();
		ean.setEan(4006381333931L);
		ean.setOrderInRecord(1L);
		ean.setNote("brož.");
		eanList.add(ean);

		data.add("024 3 $a4006381333931 note$q(sešity v obálce)");
		ean = new Ean();
		ean.setEan(4006381333931L);
		ean.setOrderInRecord(2L);
		ean.setNote("note sešity v obálce");
		eanList.add(ean);

		data.add("024 3 $a4006381333931$q(Praha ;$qv hudebnině neuvedeno ;$qbrož.)");
		ean = new Ean();
		ean.setEan(4006381333931L);
		ean.setOrderInRecord(3L);
		ean.setNote("Praha v hudebnině neuvedeno ; brož.");
		eanList.add(ean);

		// invalid
		data.add("024 3 $a73513536 (Brno)$q(Praha)");

		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		List<Ean> results = metadataRecord.getEANs();
		Assert.assertEquals(results.size(), eanList.size());
		for (int i = 0; i < eanList.size(); i++) {
			Assert.assertEquals(results.get(i), eanList.get(i), "EAN on position " + i + " differs.");
		}

		data.clear();
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getEANs(), Collections.EMPTY_LIST);
		data.clear();
	}
}
