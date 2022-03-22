package cz.mzk.recordmanager.server.dedup;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

public class DedupDisadvantagedKeysStepWriter extends DedupSimpleKeysStepWriter {

	@Autowired
	protected SessionFactory sessionFactory;

	@PostConstruct
	public void initialize() throws SQLException {
		startTime = Calendar.getInstance().getTimeInMillis();
	}

	@Override
	public void write(List<? extends List<HarvestedRecord>> arg0) throws Exception {
		for (List<HarvestedRecord> hrList : arg0) {
			for (HarvestedRecord hr : hrList) {

				// check whether is need to store current record
				if (checkIfUpdateIsNeeded(hr)) {
					String query = "UPDATE harvested_record SET dedup_record_id = :dedupRecordId WHERE id = :id ";
					sessionFactory.getCurrentSession().createSQLQuery(query)
							.setParameter("dedupRecordId", hr.getDedupRecord().getId())
							.setParameter("id", hr.getId())
							.executeUpdate();
				}

				totalCount++;
				logProgress(false);
			}
		}
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
	}

}
