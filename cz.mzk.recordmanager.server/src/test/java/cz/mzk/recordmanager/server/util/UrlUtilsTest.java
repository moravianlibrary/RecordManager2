package cz.mzk.recordmanager.server.util;

import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;

public class UrlUtilsTest {
	
	@Test
	public void buildUrlTest() {
		String expected = "http://aleph.mzk.cz/OAI?metadataPrefix=marc21&verb=ListRecords";
		Map<String, String> params = ImmutableMap.<String, String>builder().put("metadataPrefix", "marc21").put("verb", "ListRecords").build();
		String url = UrlUtils.buildUrl("http://aleph.mzk.cz/OAI", params);
		Assert.assertEquals(url, expected);
	}

}
