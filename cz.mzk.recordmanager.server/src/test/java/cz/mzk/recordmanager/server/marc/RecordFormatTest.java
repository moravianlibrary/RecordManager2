package cz.mzk.recordmanager.server.marc;

import cz.mzk.recordmanager.server.AbstractTest;
import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class RecordFormatTest extends AbstractTest {

	@Autowired
	private MetadataRecordFactory metadataFactory;

	@Test
	public void booksTest() {
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<>();

		data.add("000 000000Ac000");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertEquals(metadataRecord.getDetectedFormatList().get(0), HarvestedRecordFormatEnum.BOOKS);

		data.clear();
		data.add("000 00000000000");
		data.add("006 a");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertEquals(metadataRecord.getDetectedFormatList().get(0), HarvestedRecordFormatEnum.BOOKS);
	}

	@Test
	public void periodicalsTest() {
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<>();

		data.add("000 0000000I000");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertEquals(metadataRecord.getDetectedFormatList().get(0), HarvestedRecordFormatEnum.PERIODICALS);

		data.clear();
		data.add("000 0000000s000");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertEquals(metadataRecord.getDetectedFormatList().get(0), HarvestedRecordFormatEnum.PERIODICALS);
	}

	@Test
	public void articlesTest() {
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<>();

		data.add("000 0000000A000");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertEquals(metadataRecord.getDetectedFormatList().get(0), HarvestedRecordFormatEnum.ARTICLES);

		data.clear();
		data.add("000 0000000b000");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertEquals(metadataRecord.getDetectedFormatList().get(0), HarvestedRecordFormatEnum.ARTICLES);

		data.clear();
		data.add("000 00000000000");
		data.add("773 $atest");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertEquals(metadataRecord.getDetectedFormatList().get(0), HarvestedRecordFormatEnum.ARTICLES);
	}

	@Test
	public void mapsTest() {
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<>();

		data.add("000 000000E0000");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertEquals(metadataRecord.getDetectedFormatList().get(0), HarvestedRecordFormatEnum.MAPS);

		data.clear();
		data.add("000 000000f0000");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertEquals(metadataRecord.getDetectedFormatList().get(0), HarvestedRecordFormatEnum.MAPS);

		data.clear();
		data.add("000 00000000000");
		data.add("006 E test");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertEquals(metadataRecord.getDetectedFormatList().get(0), HarvestedRecordFormatEnum.MAPS);

		data.clear();
		data.add("000 00000000000");
		data.add("006 f test");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertEquals(metadataRecord.getDetectedFormatList().get(0), HarvestedRecordFormatEnum.MAPS);

		data.clear();
		data.add("000 00000000000");
		data.add("245 $htest kartografický dokument test");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertEquals(metadataRecord.getDetectedFormatList().get(0), HarvestedRecordFormatEnum.MAPS);

		data.clear();
		data.add("000 00000000000");
		data.add("007 A test");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertEquals(metadataRecord.getDetectedFormatList().get(0), HarvestedRecordFormatEnum.MAPS);

		data.clear();
		data.add("000 00000000000");
		data.add("336 $bcR test");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertEquals(metadataRecord.getDetectedFormatList().get(0), HarvestedRecordFormatEnum.MAPS);
	}

	@Test
	public void musicalScoresTest() {
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<>();

		data.add("000 000000C0000");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertEquals(metadataRecord.getDetectedFormatList().get(0), HarvestedRecordFormatEnum.MUSICAL_SCORES);

		data.clear();
		data.add("000 000000d0000");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertEquals(metadataRecord.getDetectedFormatList().get(0), HarvestedRecordFormatEnum.MUSICAL_SCORES);

		data.clear();
		data.add("000 00000000000");
		data.add("006 C test");
		data.add("245 $htest hudebnina test");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertEquals(metadataRecord.getDetectedFormatList().get(0), HarvestedRecordFormatEnum.MUSICAL_SCORES);

		data.clear();
		data.add("000 00000000000");
		data.add("006 d test");
		data.add("245 $htest hudebnina test");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertEquals(metadataRecord.getDetectedFormatList().get(0), HarvestedRecordFormatEnum.MUSICAL_SCORES);

		data.clear();
		data.add("000 00000000000");
		data.add("336 $btcm");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertEquals(metadataRecord.getDetectedFormatList().get(0), HarvestedRecordFormatEnum.MUSICAL_SCORES);

		data.clear();
		data.add("000 00000000000");
		data.add("336 $bntm");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertEquals(metadataRecord.getDetectedFormatList().get(0), HarvestedRecordFormatEnum.MUSICAL_SCORES);

		data.clear();
		data.add("000 00000000000");
		data.add("336 $bntv");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertEquals(metadataRecord.getDetectedFormatList().get(0), HarvestedRecordFormatEnum.MUSICAL_SCORES);

		data.clear();
		data.add("000 00000000000");
		data.add("336 $btcn");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertEquals(metadataRecord.getDetectedFormatList().get(0), HarvestedRecordFormatEnum.MUSICAL_SCORES);
	}

	@Test
	public void visualDocumentsTest() {
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<>();

		data.add("000 000000K0000");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.VISUAL_DOCUMENTS));

		data.clear();
		data.add("007 ktest");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.VISUAL_DOCUMENTS));

		data.clear();
		data.add("245 $htestGrafikatest");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.VISUAL_DOCUMENTS));

		data.clear();
		data.add("006 ktest");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.VISUAL_DOCUMENTS));

		for (String text336 : new String[]{"sti", "tci", "cri", "crt"}) {
			data.clear();
			data.add("336 $b" + text336);
			metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
			Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.VISUAL_DOCUMENTS));
		}

		data.clear();
		data.add("337 $bg");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.VISUAL_DOCUMENTS));

		data.clear();
		data.add("338 $bgtest");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.VISUAL_DOCUMENTS));
	}

	@Test
	public void microformsTest() {
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<>();

		data.add("000 000000a0000");
		data.add("008 -----------------------a");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.OTHER_MICROFORMS));

		data.clear();
		data.add("000 000000E0000");
		data.add("008 -----------------------------b");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.OTHER_MICROFORMS));

		data.clear();
		data.add("007 h");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.OTHER_MICROFORMS));

		data.clear();
		data.add("245 $htestMikrodokumenttest");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.OTHER_MICROFORMS));

		data.clear();
		data.add("337 $bh");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.OTHER_MICROFORMS));

		data.clear();
		data.add("338 $bhtest");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.OTHER_MICROFORMS));
	}

	@Test
	public void blindBrailleTest() {
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<>();

		data.add("007 fb test");
		data.add("245 $htestHmatové písmo test");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.BLIND_BRAILLE));

		data.clear();
		data.add("007 tC test");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.BLIND_BRAILLE));

		for (String test336b : new String[]{"tct", "tcm", "tci", "tcf"}) {
			data.clear();
			data.add("336 $b" + test336b);
			metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
			Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.BLIND_BRAILLE));
		}

		// BOOK AND (macan || ktn)
		for (String dfTag : new String[]{"260", "264"}) {
			data.clear();
			data.add("000 000000Ac000");
			data.add(dfTag + " $btestMAcan test");
			metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
			Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.BLIND_BRAILLE));
		}
	}

	@Test
	public void computerCarrierTest() {
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<>();

		data.add("245 $htest Elektronický zdroj test");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.OTHER_COMPUTER_CARRIER));

		for (char c : new char[]{'a', 'c', 'd', 'i', 'j', 'p', 't'}) {
			data.clear();
			data.add("000 000000" + c + "0000");
			data.add("008 -----------------------s");
			metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
			Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.OTHER_COMPUTER_CARRIER));
		}

		for (char[] testArray : new char[][]{{'a', 'c', 'd', 'i', 'j', 'p', 't'}, {'e', 'f', 'g', 'k', 'o', 'p', 'r'}}) {
			for (char c : testArray) {
				data.clear();
				data.add("006 " + c + "-----s");
				metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
				Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.OTHER_COMPUTER_CARRIER));
			}
		}

		for (char c : new char[]{'e', 'f', 'g', 'k', 'o', 'p', 'r'}) {
			data.clear();
			data.add("000 000000" + c + "0000");
			data.add("008 -----------------------------s");
			metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
			Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.OTHER_COMPUTER_CARRIER));
		}

		data.clear();
		data.add("000 000000m0000");
		data.add("006 M---");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.OTHER_COMPUTER_CARRIER));

		data.clear();
		data.add("000 000000m0000");
		data.add("245 $htest multimedium");
		data.add("300 $atest cd-Rom");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.OTHER_COMPUTER_CARRIER));

		data.clear();
		data.add("007 c-");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.OTHER_COMPUTER_CARRIER));

		data.clear();
		data.add("000 000000m0000");
		data.add("300 $atest diSketa");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.OTHER_COMPUTER_CARRIER));

		for (String test336b : new String[]{"COD", "cop"}) {
			data.clear();
			data.add("336 $b" + test336b);
			metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
			Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.OTHER_COMPUTER_CARRIER));
		}

		for (String test338b : new String[]{"ck", "cb", "cd", "CE", "ca", "cf", "ch", "cz"}) {
			data.clear();
			data.add("338 $b" + test338b);
			metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
			Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.OTHER_COMPUTER_CARRIER));
		}
	}

	@Test
	public void audioDvdTest() {
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<>();

		data.add("000 000000i0000");
		data.add("300 $atestDVD test");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.AUDIO_DVD));

		data.clear();
		data.add("000 000000j0000");
		data.add("300 $atestDVD test");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.AUDIO_DVD));
	}

	@Test
	public void audioCdTest() {
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<>();

		data.add("300 $atest Kompaktni disk test");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.AUDIO_CD));

		data.clear();
		data.add("000 00000000000");
		data.add("300 $atest Zvukove CD test");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.AUDIO_CD));

		data.clear();
		data.add("300 $atest CD test");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.AUDIO_CD));

		data.clear();
		data.add("300 $atest CD-ROM test");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertFalse(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.AUDIO_CD));

		data.clear();
		data.add("300 $atest CD-R test");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.AUDIO_CD));

		data.clear();
		data.add("300 $atest zvukova deska test digital");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.AUDIO_CD));
	}

	@Test
	public void audioLpTest() {
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<>();

		data.add("300 $atest Gramofonova deska test");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.AUDIO_LP));

		data.clear();
		data.add("300 $atest Zvukova deska test analoG");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.AUDIO_LP));

		data.clear();
		data.add("300 $atest LP");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.AUDIO_LP));

		data.clear();
		data.add("300 $atest SP test");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.AUDIO_LP));
	}

	@Test
	public void audioCassetteTest() {
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<>();

		data.add("007 s");
		data.add("338 $bSs");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.AUDIO_CASSETTE));

		for (char c : new char[]{'z', 'g', 'e', 'i', 'q', 't'}) {
			data.clear();
			data.add("007 s" + c);
			metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
			Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.AUDIO_CASSETTE));
		}

		data.clear();
		data.add("300 $atest Zvukova kazeta test");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.AUDIO_CASSETTE));

		for (String s : new String[]{"mc", "kz", "mgk"}) {
			data.clear();
			data.add("300 $test " + s + " test");
			metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
			Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.AUDIO_CASSETTE));
		}

		data.clear();
		data.add("300 $atest Magnetofonova kazeta test");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.AUDIO_CASSETTE));
	}

	@Test
	public void audioOtherTest() {
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<>();

		data.add("000 000000i0000");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.AUDIO_OTHER));

		data.clear();
		data.add("000 000000J0000");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.AUDIO_OTHER));

		data.clear();
		data.add("007 s");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.AUDIO_OTHER));

		data.clear();
		data.add("245 $htest Zvukový záznam");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.AUDIO_OTHER));

		data.clear();
		data.add("337 $bs");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.AUDIO_OTHER));

		data.clear();
		data.add("006 i");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.AUDIO_OTHER));

		data.clear();
		data.add("006 J");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.AUDIO_OTHER));

		data.clear();
		data.add("338 $bstest");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.AUDIO_OTHER));

		data.clear();
		data.add("007 i");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.AUDIO_OTHER));

		data.clear();
		data.add("336 $bspw");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.AUDIO_OTHER));

		data.clear();
		data.add("336 $bSND");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.AUDIO_OTHER));
	}

	@Test
	public void videoBluRayTest() {
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<>();

		data.add("000 000000g0000");
		data.add("300 $atest blu ray test");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.VIDEO_BLURAY));
	}

	@Test
	public void videoVhsTest() {
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<>();

		data.add("300 $atest vHs test");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.VIDEO_VHS));

		data.clear();
		data.add("007 v---b");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.VIDEO_VHS));

		data.clear();
		data.add("300 $atest videokazeta test");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.VIDEO_VHS));
	}

	@Test
	public void videoDvdTest() {
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<>();

		data.add("007 v---v");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.VIDEO_DVD));

		data.clear();
		data.add("300 $atest DVD video");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.VIDEO_DVD));

		data.clear();
		data.add("300 $atest videodisk video");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.VIDEO_DVD));

		data.clear();
		data.add("500 $atest videodisk video");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.VIDEO_DVD));

		data.clear();
		data.add("338 $bvd");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.VIDEO_DVD));

		// any VIDEO format + 300a
		data.clear();
		data.add("007 v");
		data.add("300 $atestDVD test");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.VIDEO_DVD));
	}

	@Test
	public void videoCDTest() {
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<>();

		data.add("000 000000g0000");
		data.add("300 $atest CD test");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.VIDEO_CD));
	}

	@Test
	public void videoOthersTest() {
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<>();

		data.add("007 v");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.VIDEO_OTHER));

		data.clear();
		data.add("007 m");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.VIDEO_OTHER));

		data.clear();
		data.add("245 $htest videozáznam test");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.VIDEO_OTHER));

		data.clear();
		data.add("337 $bv");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.VIDEO_OTHER));

		data.clear();
		data.add("000 000000g0000");
		data.add("008 ---------------------------------v");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.VIDEO_OTHER));

		data.clear();
		data.add("006 g---------------v");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.VIDEO_OTHER));

		data.clear();
		data.add("338 $bv---");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.VIDEO_OTHER));

		data.clear();
		data.add("336 $btdi");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.VIDEO_OTHER));

		for (String s : new String[]{"vr", "vz", "vc", "mc", "mf", "mr", "mo", "mz"}) {
			data.clear();
			data.add("338 $b" + s);
			metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
			Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.VIDEO_OTHER));
		}
	}

	@Test
	public void blindAudioTest() {
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<>();

		for (String dfTag : new String[]{"260", "264"}) {
			data.clear();
			data.add("000 000000i0000");
			data.add(dfTag + " $btestMAcan test");
			metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
			Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.BLIND_AUDIO));
		}

		for (String dfTag : new String[]{"260", "264"}) {
			data.clear();
			data.add("000 000000i0000");
			data.add(dfTag + " $btestktn test");
			metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
			Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.BLIND_AUDIO));
		}
	}

	@Test
	public void otherTest() {
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<>();

		data.add("000 000000o0000");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.OTHER_OTHER));

		data.clear();
		data.add("006 o");
		data.add("007 O");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.OTHER_OTHER));

		data.clear();
		data.add("000 000000p0000");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.OTHER_OTHER));

		data.clear();
		data.add("000 000000r0000");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.OTHER_OTHER));

		for (String f336b : new String[]{"tcf", "tdm", "tdf"}) {
			data.clear();
			data.add("000 000000i0000");
			data.add("336 $b" + f336b);
			metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
			Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.OTHER_OTHER));
		}

		data.clear();
		data.add("008 ---------------------------------d");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.OTHER_OTHER));

		data.clear();
		data.add("006 r test");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.OTHER_OTHER));

		data.clear();
		data.add("007 z test");
		data.add("336 $bzzz");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.OTHER_OTHER));

		data.clear();
		data.add("337 $bx");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.OTHER_OTHER));

		data.clear();
		data.add("337 $bz");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.OTHER_OTHER));

		data.clear();
		data.add("338 $bzu");
		metadataRecord = metadataFactory.getMetadataRecord(MarcRecordFactory.recordFactory(data));
		Assert.assertTrue(metadataRecord.getDetectedFormatList().contains(HarvestedRecordFormatEnum.OTHER_OTHER));
	}

	@Test
	public void getDetectedFormatListTest() throws Exception {
		MarcRecordImpl mri;
		MetadataRecord metadataRecord;
		List<String> data = new ArrayList<>();
		List<HarvestedRecordFormatEnum> hrf = new ArrayList<>();

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
}
