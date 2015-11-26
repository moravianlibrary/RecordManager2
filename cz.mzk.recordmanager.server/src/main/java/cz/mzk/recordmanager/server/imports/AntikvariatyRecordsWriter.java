package cz.mzk.recordmanager.server.imports;

import java.util.Date;
import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.model.AntikvariatyRecord;
import cz.mzk.recordmanager.server.oai.dao.AntikvariatyRecordDAO;

public class AntikvariatyRecordsWriter implements ItemWriter<AntikvariatyRecord>{

	@Autowired
	private AntikvariatyRecordDAO antikvariatyRecordDao;
	
	@Override
	public void write(List<? extends AntikvariatyRecord> items)
			throws Exception {
		
			for (AntikvariatyRecord newRecord: items) {
				
				Long recId = newRecord.getId();
				if (recId == null) {
					continue;
				}
				
				AntikvariatyRecord oldRecord = antikvariatyRecordDao.get(recId);
				if (oldRecord == null) {
					// persist new record
					antikvariatyRecordDao.persist(newRecord);
					continue;
				}
				
				Date newUpdated = newRecord.getUpdated() == null ? new Date(0L) : newRecord.getUpdated();
				if (oldRecord.getUpdated() == null || newUpdated.compareTo(oldRecord.getUpdated()) > 0) {
					// replace existing record
					antikvariatyRecordDao.delete(oldRecord);
					antikvariatyRecordDao.persist(newRecord);
				}
			}
		
	}

}
