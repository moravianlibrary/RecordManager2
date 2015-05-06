package cz.mzk.recordmanager.server.marc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.mapping.Array;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;
import cz.mzk.recordmanager.server.model.Cnb;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat;
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
		
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getISBNs().toString(),
				isbnlist.toString());
		data.clear();

		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getISBNs(), Collections.EMPTY_LIST);
		data.clear();
	}
	
	@Test
	public void getDetectedFormatListTest() throws Exception {
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<String>();
		List<HarvestedRecordFormat> hrf = new ArrayList<HarvestedRecordFormat>();
		
		// Books
		data.add("000 00000000");
		data.add("007 t");
		hrf.add(HarvestedRecordFormat.BOOKS);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		// Periodicals
		data.add("000 0000000i");
		hrf.add(HarvestedRecordFormat.PERIODICALS);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		// Articles
		data.add("000 0000000a");
		hrf.add(HarvestedRecordFormat.ARTICLES);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		// Maps
		data.add("000 000000000");
		data.add("245 $hkartografický dokument");
		hrf.add(HarvestedRecordFormat.MAPS);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		// Musical
		data.add("000 00000000");
		data.add("336 $bntv");
		hrf.add(HarvestedRecordFormat.MUSICAL_SCORES);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();

		// Vusial documents
		data.add("000 00000000");
		data.add("338 $bgasd");
		hrf.add(HarvestedRecordFormat.VISUAL_DOCUMENTS);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		// Manuscripts
		data.add("000 00000000");
		data.add("245 $hrukOpis");
		hrf.add(HarvestedRecordFormat.MANUSCRIPTS);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		// Microforms
		data.add("000 0000000");
		data.add("337 $bh");
		hrf.add(HarvestedRecordFormat.MICROFORMS);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		//Large prints
		data.add("000 00000000");
		data.add("007 db");
		hrf.add(HarvestedRecordFormat.LARGE_PRINTS);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		// Braill
		data.add("000 00000000");
		data.add("007 fb");
		data.add("245 $hhmatové písmo");
		hrf.add(HarvestedRecordFormat.BRAILL);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);		
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		// Electronic
		data.add("000 000000m");
		data.add("006 plllllq");
		hrf.add(HarvestedRecordFormat.ELECTRONIC_SOURCE);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);		
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		// Audio
		data.add("000 00000000");
		data.add("300 $fanaloaSg$amagnetofonová kazeta");
		hrf.add(HarvestedRecordFormat.AUDIO_CASSETTE);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);		
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		// Video
		data.add("000 000000000");
		data.add("007 vlllv");
		hrf.add(HarvestedRecordFormat.VIDEO_DVD);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);		
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
				
		// Kit
		data.add("000 00000000");
		data.add("006 o");
		data.add("007 o");
		hrf.add(HarvestedRecordFormat.KIT);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);		
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		// Object
		data.add("000 00000000");
		data.add("008 zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzd");
		hrf.add(HarvestedRecordFormat.OBJECT);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);		
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		// Mix document
		data.add("000 000000p");
		hrf.add(HarvestedRecordFormat.MIX_DOCUMENT);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);		
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		// Unspecified
		data.add("000 00000000");
		data.add("337 $bx");
		hrf.add(HarvestedRecordFormat.UNSPECIFIED);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);		
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
		
		// Nothing
		data.add("000 00000000");
		hrf.add(HarvestedRecordFormat.UNSPECIFIED);
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);		
		Assert.assertEquals(metadataRecord.getDetectedFormatList().toString(), hrf.toString());
		data.clear();
		hrf.clear();
	}

}
