package cz.mzk.recordmanager.server.imports;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.marc4j.MarcException;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.marc.marc4j.ZakonyProLidiMetadataXmlStreamReader;
import cz.mzk.recordmanager.server.util.HttpClient;

public class ZakonyProLidiRecordsReader implements ItemReader<List<Record>> {

	private static Logger logger = LoggerFactory.getLogger(ZakonyProLidiRecordsReader.class);
	
	@Autowired
	private HttpClient httpClient;

	private MarcReader reader;
	
	private static final int START_YEAR = 1945;
	private static final int ACTUAL_YEAR = Calendar.getInstance().get(Calendar.YEAR);
	
	private int year;
	private int end_year;
	
	private int batchSize = 20;
	
	private static final String ZAKONY_METADATA_URL = "http://www.zakonyprolidi.cz/api/v1/data.xml/YearDocList?apikey=test&Collection=cs&Year=";
	
	public ZakonyProLidiRecordsReader(){
		year = START_YEAR;
		end_year = ACTUAL_YEAR;
	}
	
	@Override
	public List<Record> read(){
		List<Record> batch = new ArrayList<Record>();
		
		if(reader == null || !reader.hasNext()) {
			getNextYearData();
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

	protected void getNextYearData(){
		try {
			if(year <= end_year){
				logger.info("Harvesting: " + ZAKONY_METADATA_URL+year);
				reader = new ZakonyProLidiMetadataXmlStreamReader(httpClient.executeGet(ZAKONY_METADATA_URL+year));
			}
		} catch (IOException e) {
			logger.warn(e.getMessage());
		}
		
		year++;
	}

}
