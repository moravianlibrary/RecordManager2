package cz.mzk.recordmanager.server.dedup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.Title;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.StringUtils;

/**
 * Processor implementation for steps that uses deduplication data from Skat.
 * 
 * Records should be merged if they were merged in Skat and have at least simmilar
 * title to Skat record
 *
 */

@Component
public class DedupSkatKeysProcessor extends DedupSimpleKeysStepProcessor implements
		ItemProcessor<List<Long>, List<HarvestedRecord>> {

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;
	
	@Override
	public List<HarvestedRecord> process(List<Long> item) throws Exception {
		if (item == null || item.size() < 2) {
			return Collections.emptyList();
		}
		
		// get skatRecord from list
		HarvestedRecord skatRec = harvestedRecordDao.get(item.get(0));
		
		// get other records from list
		Set<HarvestedRecord> ordinaryRecords = new HashSet<>();
		item.subList(1, item.size()).stream().forEach(i -> ordinaryRecords.add(harvestedRecordDao.get(i)));
		
		List<Title> expectedTitles = skatRec.getTitles();
		
		Set<HarvestedRecord> toBeMerged = new HashSet<>();
		
		// separate records that should be actually merged
		// decision is based on similarity of titles
		for (HarvestedRecord currentRec: ordinaryRecords) {
			for (Title currentTitle: currentRec.getTitles()) {
				for (Title expectedTitle: expectedTitles) {
					if (StringUtils.simmilarTitleMatch(currentTitle, expectedTitle, 70, 8)) {
						toBeMerged.add(currentRec);
					}
				}
			}
		}
		
		if (toBeMerged.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<Long> tobeMergedIds = new ArrayList<>();
		tobeMergedIds.add(skatRec.getId());
		toBeMerged.stream().forEach(r -> tobeMergedIds.add(r.getId()));
		
		// pass ids to be merged into parent
		return super.process(tobeMergedIds);
	}
	
}
