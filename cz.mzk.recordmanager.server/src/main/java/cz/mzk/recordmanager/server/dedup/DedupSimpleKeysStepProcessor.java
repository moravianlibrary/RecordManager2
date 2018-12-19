package cz.mzk.recordmanager.server.dedup;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.DedupRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;

/**
 * Generic implementation of of ItemProcessor 
 *
 */
@Component
public class DedupSimpleKeysStepProcessor implements
		ItemProcessor<List<Long>, List<HarvestedRecord>> {

	private static Logger logger = LoggerFactory.getLogger(DedupSimpleKeysStepProcessor.class);

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;
	
	@Autowired
	private DedupRecordDAO dedupRecordDAO;
	
	@Override
	public List<HarvestedRecord> process(List<Long> idList) throws Exception {
		List<HarvestedRecord> hrList = harvestedRecordDao.findByIds(idList);
		// count of DedupRecord in current batch
		Multiset<DedupRecord> dedupMap = HashMultiset.create();
		hrList.stream().filter(rec -> rec.getDedupRecord() != null).forEach(rec -> dedupMap.add(rec.getDedupRecord()));
		// Map of records that shoul be updated after processing of batch
		// used in merging two different DedupRecords into one
		Map<DedupRecord, Set<DedupRecord>> updateDedupRecordsMap = new HashMap<>();

		for (int i = 0; i < hrList.size(); i++) {
			HarvestedRecord outerRec = hrList.get(i);
			for (int j = i + 1; j < hrList.size(); j++) {
				HarvestedRecord innerRec = hrList.get(j);
				
				if (matchRecords(outerRec,innerRec)) {
					//merge records, both already have assigned DedupRecord
					if (outerRec.getDedupRecord() != null && innerRec.getDedupRecord() != null) {
						if (sameDedupRecords(outerRec.getDedupRecord(), innerRec.getDedupRecord())) {
							// equal dedupRecord, nothing to do
						} else {

							DedupRecord moreFrequented = dedupMap.count(outerRec.getDedupRecord()) >= dedupMap.count(innerRec.getDedupRecord()) ? 
									outerRec.getDedupRecord() : innerRec.getDedupRecord();
							DedupRecord lessFrequented = sameDedupRecords(moreFrequented, outerRec.getDedupRecord()) ? 
									innerRec.getDedupRecord() : outerRec.getDedupRecord();
							
							outerRec.setDedupRecord(moreFrequented);
							innerRec.setDedupRecord(moreFrequented);
							lessFrequented.setUpdated(new Date());
							dedupMap.add(moreFrequented);
							dedupMap.remove(lessFrequented);
							
							// all occurrences of lessFrequented in database should be updated to moreFrequented later
							if (harvestedRecordDao.existsByDedupRecord(lessFrequented)) {
								updateDedupRecordsMap.computeIfAbsent(moreFrequented, key -> new HashSet<>()).add(lessFrequented);
							}
						}
						continue;
					}

					// any of records have assigned DedupRecord
					if (outerRec.getDedupRecord() == null && innerRec.getDedupRecord() == null) {
						DedupRecord newDr = new DedupRecord();
						newDr.setUpdated(new Date());
						newDr = dedupRecordDAO.persist(newDr);
						
						outerRec.setDedupRecord(newDr);
						innerRec.setDedupRecord(newDr);
						
						dedupMap.setCount(newDr, 2);
						continue;
					}
					
					// if we got this far, exactly one of records has assigned DedupRecord
					DedupRecord dr = outerRec.getDedupRecord() != null ? outerRec.getDedupRecord() : innerRec.getDedupRecord();
					dr.setUpdated(new Date());
					outerRec.setDedupRecord(dr);
					innerRec.setDedupRecord(dr);
					
					dedupMap.add(dr);
					
				}
			}
		}
		
		
		// walk through map and update references 
		for (Map.Entry<DedupRecord, Set<DedupRecord>> entry : updateDedupRecordsMap.entrySet()) {
		    for (DedupRecord updatedDR: entry.getValue()) {
		    	for(HarvestedRecord toBeUpdated: harvestedRecordDao.getByDedupRecord(updatedDR)) {
		    		toBeUpdated.setDedupRecord(entry.getKey());
		    	}
		    }
		}
		
		
		return hrList;
	}
	
	/**
	 * decide whether two records should match or not
	 * It's supposed to be overridden in children implementations
	 * 
	 * @param hrA {@link HarvestedRecord}
	 * @param hrB {@link HarvestedRecord}
	 * @return true
	 */
	protected boolean matchRecords(HarvestedRecord hrA, HarvestedRecord hrB) {
		return true;
	}
	
	protected boolean sameDedupRecords(DedupRecord d1, DedupRecord d2) {
		if (d1 == null || d1.getId() == null || d2 == null || d2.getId() == null) {
			return false;
		}
		
		return d1.getId().equals(d2.getId());
	}
	
}
