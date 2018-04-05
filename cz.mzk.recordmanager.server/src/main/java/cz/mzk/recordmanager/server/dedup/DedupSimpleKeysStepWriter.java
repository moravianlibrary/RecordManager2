package cz.mzk.recordmanager.server.dedup;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;

public class DedupSimpleKeysStepWriter implements
		ItemWriter<List<HarvestedRecord>> {

	private static Logger logger = LoggerFactory.getLogger(DedupSimpleKeysStepWriter.class);

	private static int LOG_PERIOD = 1000;
	
	private int totalCount = 0;
	
	@Autowired
	private HarvestedRecordDAO harvestedRecordDAO;

	@Autowired
	protected SessionFactory sessionFactory;

	long startTime;
	
	@PostConstruct
	public void initialize() throws SQLException  {
		startTime = Calendar.getInstance().getTimeInMillis();
	}
	
	@Override
	public void write(List<? extends List<HarvestedRecord>> arg0)
			throws Exception {
		for (List<HarvestedRecord> hrList: arg0) {
			for (HarvestedRecord hr: hrList) {
				
				// check whether is need to store current record
				if (checkIfUpdateIsNeeded(hr)) {
					String query = "UPDATE harvested_record SET dedup_record_id = ? WHERE id = ? ";
					sessionFactory.getCurrentSession().createSQLQuery(query)
							.setLong(0, hr.getDedupRecord().getId())
							.setLong(1, hr.getId())
							.executeUpdate();
				}

				totalCount++;
				logProgress(false);
			}
		}
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
	}
	
	/**
	 * @param hr {@link HarvestedRecord}
	 * @return False if harvested record was updated before it's dedupRecord updated time
	 */
	protected boolean checkIfUpdateIsNeeded(HarvestedRecord hr) {
		
		if (hr == null) {
			return false;
		}
		
		DedupRecord dr = hr.getDedupRecord();
		if (dr == null || dr.getUpdated() == null) {
			return false;
		}
		
		Date drUpdate = dr.getUpdated();
		
		if (hr.getUpdated() != null) {
			return drUpdate.compareTo(hr.getUpdated()) > 0;
		}
		
		return true;
	}
	
	protected void logProgress(boolean force) {
		if (force || totalCount % LOG_PERIOD == 0) {
			long elapsedSecs = (Calendar.getInstance().getTimeInMillis() - startTime) / 1000;
			if (elapsedSecs == 0)
				elapsedSecs = 1;
			logger.info(String.format("Deduplicated %,9d, processing speed %4d records/s",
							totalCount, totalCount / elapsedSecs));
		}
	}

}
