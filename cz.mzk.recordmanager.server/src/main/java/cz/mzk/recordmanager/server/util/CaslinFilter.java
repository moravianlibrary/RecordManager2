package cz.mzk.recordmanager.server.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.LRUMap;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.oai.dao.SiglaDAO;

@Component
public class CaslinFilter {

	@Autowired
	private SiglaDAO siglaDao;
	
	private final int LRU_CACHE_SIZE = 100;
	
	private final Map<String, Boolean> siglaCache = Collections.synchronizedMap(new LRUMap<String, Boolean>(LRU_CACHE_SIZE));

	public List<DataField> filter(List<DataField> input){
		List<DataField> result = new ArrayList<DataField>();
		for(DataField df: input){
			Subfield sf = df.getSubfield('e');
			if(sf == null) continue;
			if(siglaCache.containsKey(sf.getData())){
				if(!siglaCache.get(sf.getData())) result.add(df);
			}
			else{
				if(siglaDao.findSiglaByName(sf.getData()).isEmpty()){
					result.add(df);
					siglaCache.put(sf.getData(), false);
				}
				else siglaCache.put(sf.getData(), true);
			}
		}
		return result;
	}
}
