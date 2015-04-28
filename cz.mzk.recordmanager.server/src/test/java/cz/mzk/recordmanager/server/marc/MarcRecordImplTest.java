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
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat;
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
	}

	@Test
	public void getISSNsTest() throws Exception {
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<String>();

		data.add("022 $a2336-4815");
		data.add("022 $a1234-5678");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getISSNs().toString(),
				"[23364815, 12345678]");
		data.clear();

		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getISSNs(), Collections.EMPTY_LIST);
		data.clear();
	}

	@Test
	void getSeriesISSNTest() throws Exception {
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<String>();

		data.add("490 $x0023-6721");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getSeriesISSN(), "0023-6721");
		data.clear();

		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getSeriesISSN(), null);
		data.clear();
	}

	@Test
	void getPageCountTest() throws Exception {
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

		data.add("020 $a9788086026923");
		data.add("020 $a9788086026923");
		data.add("020 $a978-80-7250-482-4");
		data.add("020 $a80-200-0980-9");
		data.add("020 $a456");
		mri = MarcRecordFactory.recordFactory(data);
		metadataRecord = metadataFactory.getMetadataRecord(mri);
		Assert.assertEquals(metadataRecord.getISBNs().toString(),
				"[9788086026923, 9788072504824, 9788020009807]");
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
