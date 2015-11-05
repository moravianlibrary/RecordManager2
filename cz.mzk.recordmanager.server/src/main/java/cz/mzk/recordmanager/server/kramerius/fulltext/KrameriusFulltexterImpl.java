package cz.mzk.recordmanager.server.kramerius.fulltext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;

import cz.mzk.recordmanager.server.model.FulltextMonography;
import cz.mzk.recordmanager.server.util.HttpClient;

public class KrameriusFulltexterImpl implements KrameriusFulltexter {

	private static Logger logger = LoggerFactory
			.getLogger(KrameriusFulltexterImpl.class);

	@Autowired
	private HttpClient httpClient;

	private String kramApiUrl;

	private String authToken;

	// defaults to false, processor may set up true
	private boolean downloadPrivateFulltexts = false;

	public KrameriusFulltexterImpl(String kramApiUrl, String authToken,
			boolean downloadPrivateFulltexts) {
		super();
		this.kramApiUrl = kramApiUrl;
		this.authToken = authToken;
		this.downloadPrivateFulltexts = downloadPrivateFulltexts;
	}

	/*
	 * gets basic page metadata from JSON received from Kramerius API (for
	 * specified rootUuid) returns list of FulltextMonographies
	 */
	public List<FulltextMonography> getPagesMetadata(String rootUuid) throws IOException {
		JSONArray pagesJson; /* check it */
		List<FulltextMonography> pagesMetadataList = new ArrayList<FulltextMonography>();

		String pagesListUrl = kramApiUrl + "/item/" + rootUuid + "/children";
		logger.debug("Going to read pages metadata from: " + pagesListUrl);

		try {
			pagesJson = readKrameriusJSON(pagesListUrl);
		} catch (JSONException e) {
			logger.warn(e.getMessage());
			pagesJson = new JSONArray();
		}

		/*
		 * for each JSONObject in array - extract page id, page number, policy &
		 * check model
		 */
		for (int i = 0; i < pagesJson.length(); i++) {
			FulltextMonography ftm = new FulltextMonography();

			JSONObject obj = pagesJson.getJSONObject(i);

			// model MUST equal "page" or it will be ignored
			String model = (String) obj.get("model");
			if (!model.equals("page")) {
				logger.debug("Model is not \"page\", Model is  \"" + model
						+ "\" for uuid:" + (String) obj.get("pid"));
				continue;
			}

			String policy = (String) obj.get("policy");
			ftm.setPrivate(!policy.equals("public"));

			String pid = (String) obj.get("pid");
			ftm.setUuidPage(pid);

			try {
				JSONObject details = (JSONObject) obj.get("details");
				String page = (String) details.get("pagenumber");
				// String page= (String) obj.get("title"); //information in
				// "title" is sometimes malformed in Kramerius' JSON
				ftm.setPage(page.trim());
			} catch (Exception e) {
				logger.warn(e.getMessage());
			}

			pagesMetadataList.add(ftm);
		}
		return pagesMetadataList;

	}

	/*
	 * create OCR URL, download OCR and return it as byte[]
	 */
	public byte[] getOCRBytes(String pageUuid, boolean isPrivate) {
		/* vytvorit odkaz do API pro UUID stranky */
		String ocrUrl = kramApiUrl + "/item/" + pageUuid + "/streams/TEXT_OCR";
		logger.debug("Trying to download OCR from [" + ocrUrl + "] ....");
		byte[] ocr = null; /* TODO check */

		try {
			ocr = readUrlBytes(ocrUrl, isPrivate, authToken,
					downloadPrivateFulltexts);
		} catch (Exception e) {
			logger.warn("Exception -- downloading of OCR from " + ocrUrl
					+ " failed:" + e.getMessage());
		}
		return ocr;
	}

	/*
	 * gets some page metadata read from JSON for given rootUuid, loads OCR,
	 * modifies FulltextMonography and returns them in list
	 */
	@Override
	public List<FulltextMonography> getFulltextObjects(String rootUuid) throws IOException {
		List<FulltextMonography> fms = getPagesMetadata(rootUuid);
		Long pageOrder = 0L;

		for (FulltextMonography fm : fms) {
			pageOrder++;
			String pageUuid = fm.getUuidPage();

			/*
			 * really try to get OCR only if page is not private(=is public), or
			 * download of private fulltext is allowed and authToken is set)
			 */
			if (!fm.isPrivate()
					|| (downloadPrivateFulltexts && authToken != null)) {
				byte[] ocr = getOCRBytes(pageUuid, fm.isPrivate());
				fm.setFulltext(ocr);
			}
			fm.setOrder(pageOrder);
		}
		return fms;
	}

	/* reads JSON from specified (complete) url */
	public JSONArray readKrameriusJSON(String url) throws JSONException, IOException {
		String result = readUrl(url);
		return new JSONArray(result);
	}

	// used for reading JSON's contents - returns String
	private String readUrl(String url) throws IOException {
		try (InputStream content = httpClient.executeGet(url)) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ByteStreams.copy(content, baos);
			return new String(baos.toByteArray(), Charsets.UTF_8);
		}
	}

	// used for reading OCR stream - returns byte[]
	private byte[] readUrlBytes(String url, boolean isPrivate,
			String authToken, boolean allowHarvestingOfPrivateFulltexts) throws IOException {
		/*
		 * use auth token only when page is private, indexing is allowed and
		 * token is set
		 */
		Map<String, String> headers = Collections.emptyMap();
		if (isPrivate && allowHarvestingOfPrivateFulltexts && authToken != null) {
			headers = ImmutableMap.of("Authorization", " Basic " + authToken);
		}
		try (InputStream content = httpClient.executeGet(url, headers)) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ByteStreams.copy(content, baos);
			return baos.toByteArray();
		}
	}

}
