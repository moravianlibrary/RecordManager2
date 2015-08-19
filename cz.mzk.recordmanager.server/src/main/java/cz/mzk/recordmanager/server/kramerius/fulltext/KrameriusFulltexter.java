package cz.mzk.recordmanager.server.kramerius.fulltext;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.FulltextMonography;


@Component
public class KrameriusFulltexter {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	private String kramApiUrl;
	
/* musi umet:
 *  - ziskat uuid pro vsechny zaznamy pro dane confID => seznam UUID
 *  - na zaklade UUID podle modelu stahnout objekt obsahujici identifikatory a OCR => ulozit do DB
 */
	
	
/* TODO 
 * dostat seznam uuid z DB a vypsat je;-)
 */
	public void printUUIDs (Long confId) {
		String sql = "SELECT record_id FROM harvested_record WHERE import_conf_id ="+confId;
		
		System.out.println("asdasdasdads");
		if (jdbcTemplate == null) {			
			System.out.println("jdbcTemplate JE NULL");
		} else {
			System.out.println("jdbcTemplate NENI NULL");
		}
		System.out.println(jdbcTemplate.getQueryTimeout());
		List<String> uuids = (List<String>) jdbcTemplate.queryForList(sql, String.class);
		
		System.out.println("**************-------------UUIDS-----------**************" + uuids.toString());
	}
	
	public List<String> getUuids (Long confId) {
		String sql = "SELECT record_id FROM harvested_record WHERE import_conf_id ="+confId;
		List<String> uuids = (List<String>) jdbcTemplate.queryForList(sql, String.class);
		
		return uuids;
	}
	
/* TODO
 * popis: pro UUID (monography) stahnout seznam stranek (page)
 * vratit seznam UUID 
 * 
 * dostane uuid
 * podiva se na api na children -priklad http://k4.techlib.cz/search/api/v5.0/item/uuid:d3cf7ce9-1891-4e39-bb35-3b38b6eb0d60/children
 * sebere seznam stranek a vrati je
 */
	public List<String> getPagesUuids(String rootUuid) {
		JSONArray pagesJson; /*check it*/
		List<String> pagesUuidList = new ArrayList<String>();
		
		String pagesListUrl = kramApiUrl +"/item/" + rootUuid + "/children";
		System.out.println("jdu na URL pro children: " + pagesListUrl);
		
		try {
			 pagesJson = readKrameriusJSON(pagesListUrl);
		} catch (Exception e) {
			System.err.println("JSON exception: "+ e.getMessage());
			pagesJson= new JSONArray();
		}
		
		//System.out.println("--------------------------");
		//System.out.println("JSON object"+pagesJson.toString());
		
		/* pro kazdy JSONObject v poli vzit page id, vytahnout a vlozit do listu*/
		for (int i=0; i<pagesJson.length(); i++) {
			JSONObject obj = pagesJson.getJSONObject(i);
			System.out.println("jsem ve FOR, i="+i + " " );
			
			
			/* TODO tady by se mohlo filtrovat na public/model/datanode
			 * slo by to i na strance, ale je to zbytecny dalsi dotaz
			 */
			String model = (String) obj.get("model");
			if (!model.equals("page")) {
				System.out.println("---model neni page, model je ["+model+"]---" + (String) obj.get("pid"));
				continue;
			}
				
			String policy = (String) obj.get("policy");
			if (!policy.equals("public")) {
				System.out.println("---policy neni public, policy je ["+policy+"]---" + (String) obj.get("pid"));
				continue;
			}
			
			
			String pid = (String) obj.get("pid");
			pagesUuidList.add(pid);
			System.out.println("naslo se tohle PID: " + pid);
		} 
		
		return pagesUuidList;
	}
	
	
	
/* TODO
 * popis: pro kazdou stranku proverit jestli se ma stahovat a jak - pokud to jde tak stahnout a ulozit do DB
 * pro kazde uuid:
 * 		otevrit http://k4.techlib.cz/search/api/v5.0/item/uuid:7480c224-fda2-11e1-985e-001b63bd97ba
 * 		zkontrolovat: model, policy:public/private, 
 * 		stahnout OCR (auth/neauth) - podle policy
 * 		vytvorit objekt (podle modelu?) 
 */
	
	public String getOCR(String pageUuid) {
		/* vytvorit odkaz do API pro UUID stranky */
		String ocrUrl = kramApiUrl +"/item/" + pageUuid + "/streams/TEXT_OCR";
		System.out.println("Zkousim stahnout OCR z ["+ocrUrl+"] ....");
		String ocr = new String(); /*TODO check */
		
		
		try {
			ocr = readUrl(ocrUrl);
			System.out.println("****OCR from "+ocrUrl+"*****");
			System.out.println(ocr);
		} catch (Exception e) {
			System.err.println("Exception -- downloading of OCR from " + ocrUrl + " failed:" + e.getMessage());
		}
		
		return ocr;
	}
	
	public byte[] getOCRBytes(String pageUuid) {
		/* vytvorit odkaz do API pro UUID stranky */
		String ocrUrl = kramApiUrl +"/item/" + pageUuid + "/streams/TEXT_OCR";
		System.out.println("Zkousim stahnout OCR z ["+ocrUrl+"] ....");
		byte[] ocr = null; /*TODO check */
		
		
		try {
			ocr = readUrlBytes(ocrUrl);
			System.out.println("****OCR from "+ocrUrl+"*****");
	//		System.out.println(ocr);
		} catch (Exception e) {
			System.err.println("Exception -- downloading of OCR from " + ocrUrl + " failed:" + e.getMessage());
		}
		
		return ocr;
	}
	
	
	/* dostat fulltext monography objekty:
	 * dostane uuid
	 * ziska stranky
	 * ke kazde strance dostane OCR
	 * 	ulozi root uuid, pageuuid, OCR, order do fulltext monography
	 * vrati fulltextmonography
	 */
	
	public List<FulltextMonography> getFulltextObjects(String rootUuid) {
		List<String> pagesUuids = getPagesUuids(rootUuid);
		List<FulltextMonography> fms = new ArrayList<FulltextMonography>();
		
		Long pageOrder=0L;
		for (String pageUuid : pagesUuids) {
			pageOrder++;
			byte[] ocr = getOCRBytes(pageUuid);
			
			FulltextMonography fm = new FulltextMonography();
			fm.setFulltext(ocr);
			fm.setOrder(pageOrder);
			System.out.println("pageuuid ktere je mozna moc dlouhe: "+pageUuid);
			fm.setUuidPage(pageUuid);
			
			fms.add(fm);
		}
		
		return fms;
	}
	
	
	/* reads JSON from specified (complete) url */
	public JSONArray readKrameriusJSON(String url) throws JSONException, Exception {	
		String result = readUrl(url);
		//System.out.println("nacteno je: " + result);
		return new JSONArray(result); 
	}

	/* nacteni JSONu z URL adresy.. mozna by slo vyresit lip... */
	private static String readUrl(String urlString) throws Exception {
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
	    } finally {
	        if (reader != null)
	            reader.close();
	    }
	}
	
	private static byte[] readUrlBytes(String urlString) throws Exception {	    
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    InputStream is = null;
	    try {
	      URL url = new URL(urlString);
	      is = url.openStream ();
	      byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
	      int n;

	      while ( (n = is.read(byteChunk)) > 0 ) {
	        baos.write(byteChunk, 0, n);
	      }
	      return baos.toByteArray();
	    }
	    finally {
	      if (is != null) { is.close(); }
	    }
	}
	
	
	public void setKramApiUrl(String kau) {
		this.kramApiUrl = kau;
	}
	
	public String getKramApiUrl() {
		return this.kramApiUrl;
	}
}
