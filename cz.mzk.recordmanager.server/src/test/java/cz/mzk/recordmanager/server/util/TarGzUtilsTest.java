package cz.mzk.recordmanager.server.util;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TarGzUtilsTest {

	private static final String EXPECTED_STR = "Hello World!!!";
	private static final String TXT_FILE_PATH = "target/test/test.txt";
	private static final String TAR_GZ_FILE_PATH = "target/test/test.tar.gz";
	private static final String EXTRACT_DIR = "target/test/extract/";
	private static final String EXTRACT_FILE = "target/test/extract/test.txt";

	@AfterClass
	@BeforeClass
	public void cleanUp() {
		for (String filename : new String[]{TXT_FILE_PATH, TAR_GZ_FILE_PATH, EXTRACT_FILE, EXTRACT_DIR}) {
			File file = new File(filename);
			if (file.delete()) System.out.println("");
		}
	}

	@Test
	public void tarTest() throws IOException {
		// new file with test String
		File file = new File(TXT_FILE_PATH);
		FileUtils.writeStringToFile(file, EXPECTED_STR, "UTF-8");
		// compress
		File tarGzFile = ExtractTarGz.compress(TXT_FILE_PATH, TAR_GZ_FILE_PATH);
		// extract
		File destTestDir = new File(EXTRACT_DIR);
		ExtractTarGz.extractTarGz(tarGzFile, destTestDir);
		// extracted file to string
		String actualStr = FileUtils.readFileToString(new File(EXTRACT_FILE), StandardCharsets.UTF_8);
		// actual x expexted
		Assert.assertEquals(actualStr, EXPECTED_STR);
	}

}
