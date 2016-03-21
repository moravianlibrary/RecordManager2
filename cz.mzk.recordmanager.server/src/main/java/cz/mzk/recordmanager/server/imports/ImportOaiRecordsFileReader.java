package cz.mzk.recordmanager.server.imports;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import cz.mzk.recordmanager.server.oai.harvest.OaiErrorException;
import cz.mzk.recordmanager.server.oai.model.OAIRecord;
import cz.mzk.recordmanager.server.oai.model.OAIRoot;

public class ImportOaiRecordsFileReader implements ItemReader<List<OAIRecord>> {
	
	private InputStream is;

	private final Unmarshaller unmarshaller;
	
	private Deque<String> files = null;
	
	private String pathName = null;

	public ImportOaiRecordsFileReader(String filename) throws FileNotFoundException {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(OAIRoot.class);
			this.unmarshaller = jaxbContext.createUnmarshaller();
		} catch (JAXBException je) {
			throw new RuntimeException(je);
		}
		getFileNames(filename);
	}

	@Override
	public List<OAIRecord> read() throws Exception, UnexpectedInputException,
			ParseException, NonTransientResourceException {
		if(!files.isEmpty()){
			return parseRecords();
		}
		
		return null;
	}

	private List<OAIRecord> parseRecords(){
		initializeInputStream();
		try{
			if (is.markSupported()) {
				is.mark(Integer.MAX_VALUE);
				is.reset();
			}

			OAIRoot oaiRoot = (OAIRoot) unmarshaller.unmarshal(is);
			is.close();
			if (oaiRoot.getOaiError() != null) {
				throw new OaiErrorException(oaiRoot.getOaiError().getMessage());
			}
			
			if(!oaiRoot.getListRecords().getRecords().isEmpty()){
				return oaiRoot.getListRecords().getRecords();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return Collections.emptyList();
	}
	
	private void getFileNames(String filename){
		files = new ArrayDeque<String>();
		File f = new File(filename);
		if(f.isFile()){ // file
			pathName = f.getParent()+"/";
			files.push(f.getName());
		}
		else{ // directory
			pathName = f.getPath()+"/";
			for(File file: f.listFiles()){
				files.push(file.getName());
			}
		}
	}
	
	private void initializeInputStream(){
		try {
			is = new FileInputStream(pathName+files.pop()); // next file
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
}
