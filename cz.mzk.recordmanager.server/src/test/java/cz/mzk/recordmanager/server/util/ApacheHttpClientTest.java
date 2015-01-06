package cz.mzk.recordmanager.server.util;

import java.io.IOException;
import java.io.InputStream;
import org.testng.annotations.Test;

public class ApacheHttpClientTest {

	@Test
	public void executeGetTest() throws IOException {
		try (ApacheHttpClient client = new ApacheHttpClient()) {
			try (InputStream is = client.executeGet("https://vufind.mzk.cz/")) {
				is.read();
			}
		}
	}

}
