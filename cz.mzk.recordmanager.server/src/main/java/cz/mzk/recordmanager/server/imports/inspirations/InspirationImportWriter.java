package cz.mzk.recordmanager.server.imports.inspirations;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.model.Inspiration;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.ImportConfigurationDAO;

@Component
@StepScope
public class InspirationImportWriter implements ItemWriter<Map<String, List<String>>> {
	
	@Autowired
	private ImportConfigurationDAO confDao;
	
	@Autowired
	private HarvestedRecordDAO hrDao;
	
	public InspirationImportWriter() {
	}
	
	private static final Pattern PATTERN_ID = Pattern.compile("([^\\.]*)\\.(.*)");
	
	@Override
	public void write(List<? extends Map<String, List<String>>> items)
			throws Exception {
		for(Map<String, List<String>> map: items){
			for (Entry<String, List<String>> entry : map.entrySet()){
				String new_inspiration = entry.getKey();
			    for(String id: entry.getValue()){
			    	Matcher matcher = PATTERN_ID.matcher(id);
			    	if(matcher.matches()){
			    		String id_prefix = matcher.group(1);
			    		String record_id = matcher.group(2);
			    		for(ImportConfiguration conf: confDao.findByIdPrefix(id_prefix)){
				    		if(conf == null) continue;
				    		HarvestedRecord hr = hrDao.findByIdAndHarvestConfiguration(record_id, conf);
				    		if(hr == null) continue;
				    		if(!inspirationExist(hr, new_inspiration)){ 
				    			List<Inspiration> result = hr.getInspiration();
				    			result.add(new Inspiration(entry.getKey()));
				    			hr.setInspiration(result);
				    			hr.setUpdated(new Date());
				    			hrDao.persist(hr);
				    		}
			    		}
			    	}
			    }
			}
		}
	}
	
	private Boolean inspirationExist(HarvestedRecord hr, String new_inspiration){
		Boolean exist = false;		
		for(Inspiration ins: hr.getInspiration()){
			if(ins.getName().equals(new_inspiration)) exist = true;
		}
		return exist;
	}
}
