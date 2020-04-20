package cz.mzk.recordmanager.server.imports.antikvariaty;

import cz.mzk.recordmanager.server.model.AntikvariatyRecord;
import cz.mzk.recordmanager.server.oai.dao.AntikvariatyRecordDAO;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

public class AntikvariatyRecordsWriter implements ItemWriter<AntikvariatyRecord> {

	@Autowired
	private AntikvariatyRecordDAO antikvariatyRecordDao;

	@Override
	public void write(List<? extends AntikvariatyRecord> items) {
		for (AntikvariatyRecord newRecord : items) {
			Long recId = newRecord.getId();
			if (recId == null) {
				continue;
			}
			newRecord.setLastHarvest(new Date());
			newRecord.setUpdated(new Date());
			AntikvariatyRecord oldRecord = antikvariatyRecordDao.get(recId);
			if (oldRecord == null) {
				// persist new record
				antikvariatyRecordDao.persist(newRecord);
				continue;
			}
			if (!oldRecord.equals(newRecord)) {
				// replace existing record
				antikvariatyRecordDao.delete(oldRecord);
				antikvariatyRecordDao.persist(newRecord);
			} else {
				oldRecord.setLastHarvest(new Date());
				oldRecord.setUpdatedOriginal(newRecord.getUpdatedOriginal());
				antikvariatyRecordDao.saveOrUpdate(oldRecord);
			}
		}
	}
}
