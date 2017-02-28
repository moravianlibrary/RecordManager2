package cz.mzk.recordmanager.server.adresar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.marc4j.MarcException;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.marc.marc4j.AdresarStreamReader;
import cz.mzk.recordmanager.server.util.HttpClient;

public class AdresarRecordsReader implements ItemReader<List<Record>> {

	private static Logger logger = LoggerFactory.getLogger(AdresarRecordsReader.class);
	
	@Autowired
	private HttpClient httpClient;

	private MarcReader reader;
	
	private static final int START_ID = 1;
	private static final int LAST_ID = 10000;
	
	private int recordId;
	
	private int batchSize = 20;
	
	private static final String ADRESAR_HARVEST_URL = "http://aleph.nkp.cz/X?op=find-doc&doc_num=%s&base=ADR";
	
	public AdresarRecordsReader(){
		recordId = START_ID;
	}
	
	@Override
	public List<Record> read(){
		List<Record> batch = new ArrayList<Record>();
		if(reader == null || !reader.hasNext()) {
			getNextRecord();
		}
		while (reader.hasNext()) {
			try {
				batch.add(reader.next());
			} catch (MarcException e) {
				logger.warn(e.getMessage());
			}
			if (batch.size() >= batchSize) {
				break;
			}
		}
		return batch.isEmpty() ? null : batch;
	}

	protected void getNextRecord(){
		try {
			if(recordId <= LAST_ID){
				logger.info("Harvesting: " + String.format(ADRESAR_HARVEST_URL, StringUtils.leftPad(Integer.toString(recordId), 9, '0')));
				reader = new AdresarStreamReader(httpClient.executeGet(String.format(ADRESAR_HARVEST_URL, StringUtils.leftPad(Integer.toString(recordId), 9, '0'))));
			}
		} catch (IOException e) {
			logger.warn(e.getMessage());
		}
		
		recordId++;
	}

}
