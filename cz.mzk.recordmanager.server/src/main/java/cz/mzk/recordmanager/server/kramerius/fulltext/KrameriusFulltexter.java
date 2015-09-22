package cz.mzk.recordmanager.server.kramerius.fulltext;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.FulltextMonography;

@Component
public class KrameriusFulltexter {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private String kramApiUrl;

	private static Logger logger = LoggerFactory
			.getLogger(KrameriusFulltexter.class);

	/*
	 * gets list of ALL uuids from harvested_records for selected confId
	 */
	//TODO - musi umet brat harvested record pro dane id po castech... od + offset
	public List<String> getUuids(Long confId) {
		String sql = "SELECT record_id FROM harvested_record WHERE import_conf_id ="
				+ confId;
		List<String> uuids = (List<String>) jdbcTemplate.queryForList(sql,
				String.class);

		return uuids;
	}

	public List<FulltextMonography> getPagesMetadata(String rootUuid) {
		JSONArray pagesJson; /* check it */
		List<FulltextMonography> pagesMetadataList = new ArrayList<FulltextMonography>();
		
		String pagesListUrl = kramApiUrl + "/item/" + rootUuid + "/children";
		// System.out.println("jdu na URL pro children: " + pagesListUrl);
		
		try {
			pagesJson = readKrameriusJSON(pagesListUrl);
		} catch (JSONException e) {
			logger.warn(e.getMessage());
			pagesJson = new JSONArray();
		}
		
		// System.out.println("--------------------------");
		// System.out.println("JSON object"+pagesJson.toString());

		/* pro kazdy JSONObject v poli vzit page id, vytahnout a vlozit do listu */
		for (int i = 0; i < pagesJson.length(); i++) {
			FulltextMonography ftm = new FulltextMonography();
			
				JSONObject obj = pagesJson.getJSONObject(i);
				System.out.println("jsem ve FOR, i=" + i + " ");

				// model MUST equal "page"	
				String model = (String) obj.get("model");
				if (!model.equals("page")) {
					System.out.println("---model neni page, model je [" + model
						+ "]---" + (String) obj.get("pid"));
					continue;
				}

				String policy = (String) obj.get("policy");
				if (policy.equals("public")) {
					ftm.setPrivate(false);
				} else {
					ftm.setPrivate(true);
				}
			
//			if (!policy.equals("public")) {
//				System.out.println("---policy neni public, policy je ["
//						+ policy + "]---" + (String) obj.get("pid"));
//				continue;
//			}

				String pid = (String) obj.get("pid");
				ftm.setUuidPage(pid);
				System.out.println("naslo se tohle PID: " + pid);
			
			try {
				JSONObject details = (JSONObject) obj.get("details");
				String page = (String) details.get("pagenumber");
				page.trim();
				//			String page= (String) obj.get("title"); //same information is stored in description->pagenumber -- sometimes it is not :-)
				ftm.setPage(page);
			} catch (Exception e) {
				logger.warn(e.getMessage());
			}
				
			pagesMetadataList.add(ftm);
		}		
		return pagesMetadataList;
		
	}
	
	

	/*
	 * returns List of all pages uuids for specified document uuid
	 * 
	 * gets document uuid gets documents' children via API
	 * http://k4.techlib.cz/search/api/v5.0/item/uuid:d3cf7ce9-1891-4e39-bb35-3b38b6eb0d60/children
	 * retrieves & returns list of pages
	 */
	@Deprecated
	public List<String> getPagesUuids(String rootUuid) {
		JSONArray pagesJson; /* check it */
		List<String> pagesUuidList = new ArrayList<String>();

		String pagesListUrl = kramApiUrl + "/item/" + rootUuid + "/children";
		// System.out.println("jdu na URL pro children: " + pagesListUrl);

		try {
			pagesJson = readKrameriusJSON(pagesListUrl);
		} catch (JSONException e) {
			logger.warn(e.getMessage());
			pagesJson = new JSONArray();
		}

		// System.out.println("--------------------------");
		// System.out.println("JSON object"+pagesJson.toString());

		/* pro kazdy JSONObject v poli vzit page id, vytahnout a vlozit do listu */
		for (int i = 0; i < pagesJson.length(); i++) {
			JSONObject obj = pagesJson.getJSONObject(i);
			System.out.println("jsem ve FOR, i=" + i + " ");

			/*
			 * TODO tady by se mohlo filtrovat na privacy/model/datanode slo by
			 * to i na strance, ale je to zbytecny dalsi dotaz
			 */
			String model = (String) obj.get("model");
			if (!model.equals("page")) {
				System.out.println("---model neni page, model je [" + model
						+ "]---" + (String) obj.get("pid"));
				continue;
			}

			String policy = (String) obj.get("policy");
			if (!policy.equals("public")) {
				System.out.println("---policy neni public, policy je ["
						+ policy + "]---" + (String) obj.get("pid"));
				continue;
			}

			String pid = (String) obj.get("pid");
			pagesUuidList.add(pid);
			System.out.println("naslo se tohle PID: " + pid);
		}

		return pagesUuidList;
	}

	/*
	 * TODO popis: pro kazdou stranku proverit jestli se ma stahovat a jak -
	 * pokud to jde tak stahnout a ulozit do DB pro kazde uuid: otevrit
	 * http://k4
	 * .techlib.cz/search/api/v5.0/item/uuid:7480c224-fda2-11e1-985e-001
	 * b63bd97ba zkontrolovat: model, policy:public/private, stahnout OCR
	 * (auth/neauth) - podle policy vytvorit objekt (podle modelu?)
	 */

	public String getOCR(String pageUuid) {
		/* vytvorit odkaz do API pro UUID stranky */
		String ocrUrl = kramApiUrl + "/item/" + pageUuid + "/streams/TEXT_OCR";
		System.out.println("Zkousim stahnout OCR z [" + ocrUrl + "] ....");
		String ocr = new String(); /* TODO check */

		try {
			ocr = readUrl(ocrUrl);
			System.out.println("****OCR from " + ocrUrl + "*****");
			System.out.println(ocr);
		} catch (Exception e) {
			System.err.println("Exception -- downloading of OCR from " + ocrUrl
					+ " failed:" + e.getMessage());
		}
		return ocr;
	}

	public byte[] getOCRBytes(String pageUuid) {
		/* vytvorit odkaz do API pro UUID stranky */
		String ocrUrl = kramApiUrl + "/item/" + pageUuid + "/streams/TEXT_OCR";
		System.out.println("Zkousim stahnout OCR z [" + ocrUrl + "] ....");
		byte[] ocr = null; /* TODO check */

		try {
			ocr = readUrlBytes(ocrUrl);
			System.out.println("****OCR from " + ocrUrl + "*****");
			// System.out.println(ocr);
		} catch (Exception e) {
			System.err.println("Exception -- downloading of OCR from " + ocrUrl
					+ " failed:" + e.getMessage());
		}
		return ocr;
	}


	/* gets some page metadata read from JSON
	 * loads OCR, modifies FulltextMonography and returns them in list
	 */
	public List<FulltextMonography> getFulltextObjects(String rootUuid) {
		List<FulltextMonography> fms = getPagesMetadata(rootUuid);
		Long pageOrder = 0L;
		
		for (FulltextMonography fm : fms) {
			pageOrder++;
			String pageUuid = fm.getUuidPage();
				
			//TODO temporary condition... doesn't work with private docs at the moment
			if (!fm.isPrivate()) {
				byte[] ocr = getOCRBytes(pageUuid);
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
	private static byte[] readUrlBytes(String urlString) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream is = null;
		try {
			URL url = new URL(urlString);
			is = url.openStream();
			byte[] byteChunk = new byte[4096];
			int n;

			while ((n = is.read(byteChunk)) > 0) {
				baos.write(byteChunk, 0, n);
			}
			return baos.toByteArray();
		} catch (IOException e) {
			logger.warn(e.getMessage());

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
}
