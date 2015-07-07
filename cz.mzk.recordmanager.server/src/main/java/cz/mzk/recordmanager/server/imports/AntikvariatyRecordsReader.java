package cz.mzk.recordmanager.server.imports;

import java.io.DataInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.AntikvariatyRecord;
import cz.mzk.recordmanager.server.model.DownloadImportConfiguration;
import cz.mzk.recordmanager.server.oai.dao.DownloadImportConfigurationDAO;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.HttpClient;
import cz.mzk.recordmanager.server.util.MetadataUtils;

@Component
public class AntikvariatyRecordsReader implements ItemReader<List<AntikvariatyRecord>>, StepExecutionListener {

	private Long configId;
	
	@Autowired 
	private HttpClient httpClient;
	
	@Autowired
	private DownloadImportConfigurationDAO configDao;
	
	
	private static Logger logger = LoggerFactory.getLogger(AntikvariatyRecordsReader.class);
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	

	private static final Pattern YEAR_PATTERN = Pattern.compile("\\d{4}");
	
	private static final String ELEMENT_RECORD = "record";
	
	private static final String ELEMENT_IDENTIFIER = "identifier";
	
	private static final String ELEMENT_URL = "url";
	
	private static final String ELEMENT_DATESTAMP = "datestamp";
	
	private static final String ELEMENT_DATE = "date";
	
	private static final String ELEMENT_TITLE = "title";
	
	private static final String ELEMENT_CTLNO = "ctlno";
	
	private DataInputStream input;

	private int chunkSize = 20;

	private XMLInputFactory xmlFactory = XMLInputFactory.newInstance();

	private XMLStreamReader xmlReader;


	
	@Override
	public void beforeStep(StepExecution stepExecution) {
		this.configId = stepExecution.getJobParameters().getLong(Constants.JOB_PARAM_CONF_ID);
	}



	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected boolean initializeReader() {

		DownloadImportConfiguration config = configDao.get(configId);
		if (config == null) {
			logger.warn(String.format("Configuration with id '%s' not found.", configId.toString()));
			return false;
		}
		
		String url = config.getUrl();
		if (url == null) {
			logger.warn(String.format("Missing url."));
			return false;
		}
		
		
		try {
			input = new DataInputStream(httpClient.executeGet(url));
			xmlReader = xmlFactory.createXMLStreamReader(input);
		} catch (Exception e) {
			logger.error("Error occurred during initialization");
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	

	@Override
	public List<AntikvariatyRecord> read() throws Exception,
			UnexpectedInputException, ParseException,
			NonTransientResourceException {
		
		if (xmlReader == null) {
			if (!initializeReader()) {
				logger.error("Initialization failed, exiting...");
				return null;
			}
		}


		List<AntikvariatyRecord> result = new ArrayList<>();

		String element = "";
		AntikvariatyRecord currentRec = new AntikvariatyRecord();
		Set<String> catalogIds = new HashSet<>();
		while (xmlReader.hasNext() && result.size() < chunkSize) {

			switch (xmlReader.getEventType()) {
			
			case XMLStreamConstants.START_ELEMENT:
				element = xmlReader.getName().getLocalPart();
				if (element.equals(ELEMENT_RECORD)) {
					currentRec = new AntikvariatyRecord();
					catalogIds = new HashSet<>();
				}
				break;
			
			case XMLStreamConstants.CHARACTERS:
				switch (element) {
				case ELEMENT_IDENTIFIER:
					try {
						currentRec.setId(Long.valueOf(xmlReader.getText()));
					} catch (NumberFormatException e) {
						//invalid identifier, ignore for now
					}
					break;
				case ELEMENT_URL:
					currentRec.setUrl(xmlReader.getText());
					break;
				case ELEMENT_DATESTAMP:
					String date = xmlReader.getText();
					if (date != null) {
						try {
							currentRec.setUpdated(DATE_FORMAT.parse(date.trim()));
						} catch (java.text.ParseException e) {
							//invalid Date, ignore
						}
					}
					break;
				case ELEMENT_TITLE:
					currentRec.setTitle(MetadataUtils.normalizeAndShorten(xmlReader.getText(), 255));
					break;
				case ELEMENT_DATE:
					Matcher matcher = YEAR_PATTERN.matcher(xmlReader.getText());
					try {
						if (matcher.find()) {
							currentRec.setPublicationYear(Long.parseLong(matcher.group(0)));
						}
					} catch (NumberFormatException e) {
						//invalid year, ignore
					}
					break;
				case ELEMENT_CTLNO:
					String text = xmlReader.getText();
					int pos = text.indexOf(")");
					if (pos > 0) {
						String id = text.substring(++pos);
						if (!id.isEmpty()) {
							catalogIds.add(id);
						}
					}
					break;
					
				}
				element = "";
				break;

			case XMLStreamConstants.END_ELEMENT:
				if (xmlReader.getName().getLocalPart().equals(ELEMENT_RECORD)) {
					if (currentRec.getId() != null) {
						currentRec.setCatalogueIds(new ArrayList<>(catalogIds));
						result.add(currentRec);
					} else {
						logger.warn("Missing identifier for record");
					}
					currentRec = null;
					catalogIds = null;
				}
				break;

			}
			xmlReader.next();
		}

		return result.isEmpty() ? null : result;
	}





}
