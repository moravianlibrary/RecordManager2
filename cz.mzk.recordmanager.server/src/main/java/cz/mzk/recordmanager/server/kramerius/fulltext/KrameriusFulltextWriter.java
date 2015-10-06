package cz.mzk.recordmanager.server.kramerius.fulltext;

import java.util.List;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer.SessionBinder;

public class KrameriusFulltextWriter implements ItemWriter<HarvestedRecord>,StepExecutionListener {

	@Autowired
	HarvestedRecordDAO recordDao;
	
	@Autowired
	private HibernateSessionSynchronizer sync;
	
	Long confId;
	
	@Override
	public void write(List<? extends HarvestedRecord> items) throws Exception {

		for (HarvestedRecord hr : items) {
			
//			System.out.println("//// mame id: " +hr.getUniqueId().getRecordId());
//			System.out.println("\\\\ mame confId: "+confId);
//x			HarvestedRecord rec = recordDao.findByIdAndHarvestConfiguration(
//x					hr.getUniqueId().getRecordId(), confId);
			
			//delete old List of FulltextMonography from rec
//x			System.out.println("----preparing to delete fulltext monography pages ----");
//x			recordDao.deleteFulltextMonography(rec);
			
			//add new List of FulltextMonography to rec
//x			System.out.println("----setting new fulltext monography pages ----");
//x			rec.setFulltextMonography(hr.getFulltextMonography()); // tady je zakopany pes - puvodni fulltext monography zustane v DB, jen ztrati identifikator
			
//x			recordDao.persist(rec);
			recordDao.persist(hr);
		}
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		try (SessionBinder session = sync.register()) {
		  confId = stepExecution.getJobParameters().getLong(
					"configurationId");
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		// TODO Auto-generated method stub
		return null;
	}
	
}