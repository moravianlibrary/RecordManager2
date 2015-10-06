package cz.mzk.recordmanager.server.kramerius.fulltext;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.apache.commons.codec.binary.Base64;

import cz.mzk.recordmanager.server.kramerius.harvest.KrameriusHarvesterParams;
import cz.mzk.recordmanager.server.model.FulltextMonography;
import cz.mzk.recordmanager.server.model.KrameriusConfiguration;
import cz.mzk.recordmanager.server.oai.dao.KrameriusConfigurationDAO;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer.SessionBinder;

@Component
public class KrameriusFulltexter {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private String kramApiUrl;
	private String authToken;
	private boolean downloadPrivateFulltexts = false; // defaults to false,
														// processor may set up
														// true

	private static Logger logger = LoggerFactory
			.getLogger(KrameriusFulltexter.class);

	/*
	 * gets basic page metadata from JSON received from Kramerius API (for
	 * specified rootUuid) returns list of FulltextMonographies
	 */
	public List<FulltextMonography> getPagesMetadata(String rootUuid) {
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
			if (policy.equals("public")) {
				ftm.setPrivate(false);
			} else {
				ftm.setPrivate(true);
			}

			String pid = (String) obj.get("pid");
			ftm.setUuidPage(pid);

			try {
				JSONObject details = (JSONObject) obj.get("details");
				String page = (String) details.get("pagenumber");
				page.trim();
				// String page= (String) obj.get("title"); //information in
				// "title" is sometimes malformed in Kramerius' JSON
				ftm.setPage(page);
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
	public List<FulltextMonography> getFulltextObjects(String rootUuid) {
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
	public JSONArray readKrameriusJSON(String url) throws JSONException {
		String result = readUrl(url);
		return new JSONArray(result);
	}

	// used for reading JSON's contents - returns String
	private static String readUrl(String urlString) {
		BufferedReader reader = null;
		try {
			URL url = new URL(urlString);
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			StringBuffer buffer = new StringBuffer();
			int read;
			char[] chars = new char[1024];
			while ((read = reader.read(chars)) != -1)
				buffer.append(chars, 0, read);

			return buffer.toString();
		} catch (IOException e) {
			logger.warn(e.getMessage());
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					logger.warn(e.getMessage());
				}
		}
		return urlString;
	}

	// used for reading OCR stream - returns byte[]
	private static byte[] readUrlBytes(String urlString, boolean isPrivate,
			String authToken, boolean allowHarvestingOfPrivateFulltexts) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream is = null;
		HttpURLConnection yc;
		try {

			URL url = new URL(urlString);
			yc = (HttpURLConnection) url.openConnection();

			/*
			 * use auth token only when page is private, indexing is allowed and
			 * token is set
			 */
			if (isPrivate && allowHarvestingOfPrivateFulltexts
					&& authToken != null) {
				yc.setRequestProperty("Authorization", " Basic " + authToken);
			}

			yc.connect();
			is = yc.getInputStream();

			byte[] byteChunk = new byte[4096];
			int n;

			while ((n = is.read(byteChunk)) > 0) {
				baos.write(byteChunk, 0, n);
			}
			return baos.toByteArray();
		} catch (IOException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			logger.error(e.getMessage());
			logger.error(exceptionAsString);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					logger.warn(e.getMessage());
				}
			}
		}

		return null;
	}

	public void setKramApiUrl(String kau) {
		this.kramApiUrl = kau;
	}

	public String getKramApiUrl() {
		return this.kramApiUrl;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public boolean isDownloadPrivateFulltexts() {
		return downloadPrivateFulltexts;
	}

	public void setDownloadPrivateFulltexts(boolean downloadPrivateFulltexts) {
		this.downloadPrivateFulltexts = downloadPrivateFulltexts;
	}

}
