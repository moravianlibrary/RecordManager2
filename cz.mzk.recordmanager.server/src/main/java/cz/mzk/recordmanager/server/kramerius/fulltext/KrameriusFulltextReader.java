package cz.mzk.recordmanager.server.kramerius.fulltext;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.KrameriusConfiguration;
import cz.mzk.recordmanager.server.oai.dao.KrameriusConfigurationDAO;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer.SessionBinder;


@Component
@StepScope
public class KrameriusFulltextReader implements ItemReader<List<String>>,
ItemStream, StepExecutionListener {

	@Autowired
	private KrameriusConfigurationDAO configDao;
	
	@Autowired
	private HibernateSessionSynchronizer sync;

	@Autowired
	private HibernateSessionSynchronizer hibernateSync;
	
	@Autowired
	private KrameriusFulltexter kf;
	
	private KrameriusConfiguration conf;
	
	// configuration
	private Long confId;
	
	
	public KrameriusFulltextReader(Long confId) {
		super();
		this.confId = confId;
		
		
		
	}
	
	@Override
	public void beforeStep(StepExecution stepExecution) {
		try (SessionBinder sess = hibernateSync.register()) {
			conf = configDao.get(confId);
			kf.setKramApiUrl(conf.getUrl());
		}

	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void open(ExecutionContext executionContext)
			throws ItemStreamException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(ExecutionContext executionContext)
			throws ItemStreamException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() throws ItemStreamException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<String> read() throws Exception,
			UnexpectedInputException, ParseException,
			NonTransientResourceException {
		
		kf.printUUIDs(confId);
		List<String> topUuids = kf.getUuids(confId);
		
		for (String uuid : topUuids) {
			List<String> pagesUuids = kf.getPagesUuids(uuid);
			String fileName = "./fulltext/"+confId+"-"+uuid+".txt";
			
			
			try {
				FileWriter fileWriter = new FileWriter(fileName);
	            BufferedWriter bufferedWriter =
	                new BufferedWriter(fileWriter);
				
				for (String pUuid: pagesUuids) {
					String ocr = kf.getOCR(pUuid);
					bufferedWriter.newLine();
					bufferedWriter.write(pUuid);
					bufferedWriter.newLine();
					bufferedWriter.write(ocr);
		        }
			
	            bufferedWriter.close();
	            
			} catch(IOException ex) {
		            System.out.println("Error writing to file: " +fileName);								
				/*TODO - OCR by melo vratit text a ten by se tu mel zapsat do souboru (pozdeji do DB)*/
			}
		}
		
		return null;
	}

}
