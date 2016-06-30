package cz.mzk.recordmanager.server.imports.inspirations;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.model.Inspiration;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.ImportConfigurationDAO;
import cz.mzk.recordmanager.server.oai.dao.InspirationDAO;

@Component
@StepScope
public class InspirationImportWriter implements ItemWriter<Map<String, List<String>>> {
	
	private static Logger logger = LoggerFactory.getLogger(InspirationImportWriter.class);
	
	@Autowired
	private ImportConfigurationDAO confDao;
	
	@Autowired
	private HarvestedRecordDAO hrDao;
	
	@Autowired
	private InspirationDAO inspirationDao;
	
	private List<HarvestedRecord> hrWithInspiration;
	
	public InspirationImportWriter() {
	}
	
	private static final Pattern PATTERN_ID = Pattern.compile("([^\\.]*)\\.(.*)");
	
	private static final String TEXT_INFO = "Importing inspiration '%s' with %s records";
	private static final String TEXT_ADD = "Added %s inspirations";
	private static final String TEXT_EXISTS = "%s inspirations is already exists in db";
	private static final String TEXT_RECORD_NOT_EXIST = "%s harvested records not found";
	private static final String TEXT_DELETE = "Deleted %s inspirations";
	private static final String TEXT_COMPLETE = "Importing inspiration %s is COMPLETED";
	
	@Override
	public void write(List<? extends Map<String, List<String>>> items)
			throws Exception {
		for(Map<String, List<String>> map: items){
			for (Entry<String, List<String>> entry : map.entrySet()){
				String inspiration_name = entry.getKey();
				logger.info(String.format(TEXT_INFO, inspiration_name, entry.getValue().size()));
				// actual list of records with inspiration in db
				hrWithInspiration = inspirationDao.fingHrByInspiraion(inspiration_name);
				int added_ins = 0; // added inspiration records
				int exists_ins = 0; // inspirations in db
				int not_exists_rec = 0; // hr not in db
			    for(String id: entry.getValue()){
			    	Matcher matcher = PATTERN_ID.matcher(id);
			    	if(matcher.matches()){
			    		String id_prefix = matcher.group(1);
			    		String record_id = matcher.group(2);
			    		List<ImportConfiguration> confs = confDao.findByIdPrefix(id_prefix);
			    		int counter = 0;
			    		for(ImportConfiguration conf: confs){
				    		if(conf == null) continue;
				    		HarvestedRecord hr = hrDao.findByIdAndHarvestConfiguration(record_id, conf);
				    		if(hr == null){
				    			if(++counter == confs.size()) ++not_exists_rec;
				    			continue;
				    		}
				    		
				    		if(hrWithInspiration.contains(hr)){
				    			// inspiration is already in db
				    			hrWithInspiration.remove(hr);
				    			++exists_ins;
				    		}
				    		else{ // add inspiration to hr 
				    			List<Inspiration> result = hr.getInspiration();
				    			Inspiration newInspiration = new Inspiration(entry.getKey());
				    			newInspiration.setHarvestedRecordId(hr.getId());
				    			result.add(newInspiration);
				    			hr.setInspiration(result);
				    			hr.setUpdated(new Date());
				    			hrDao.persist(hr);
				    			++added_ins;
				    		}
			    		}
			    	}
			    }
			    // rest of records - delete inspiration
			    for(HarvestedRecord hr: hrWithInspiration){
			    	Inspiration delete = inspirationDao.findByHrIdAndName(hr.getId(), inspiration_name);
					List<Inspiration> inspirations = hr.getInspiration();
					inspirations.remove(delete);
					hr.setInspiration(inspirations);
					hr.setUpdated(new Date());
					hrDao.persist(hr);
					inspirationDao.delete(delete);
			    }
			    logger.info(String.format(TEXT_EXISTS, exists_ins));
			    logger.info(String.format(TEXT_ADD, added_ins));
			    logger.info(String.format(TEXT_RECORD_NOT_EXIST, not_exists_rec));
			    logger.info(String.format(TEXT_DELETE, hrWithInspiration.size()));
			    logger.info(String.format(TEXT_COMPLETE, inspiration_name));
			}
		}
	}
}
