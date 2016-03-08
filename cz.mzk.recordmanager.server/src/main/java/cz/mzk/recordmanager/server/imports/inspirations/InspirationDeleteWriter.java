package cz.mzk.recordmanager.server.imports.inspirations;

import java.util.Date;
import java.util.List;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.Inspiration;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.InspirationDAO;

@Component
@StepScope
public class InspirationDeleteWriter implements ItemWriter<Long> {

	@Autowired
	private HarvestedRecordDAO hrDao;

	@Autowired
	private InspirationDAO insDao;
	
	@Override
	public void write(List<? extends Long> items) throws Exception {
		for(Long id: items){
			Inspiration ins = insDao.get(id);
			if(ins == null) continue;
			HarvestedRecord hr = hrDao.get(ins.getHarvestedRecordId());
			if(hr == null) continue;
			List<Inspiration> inspirations = hr.getInspiration();
			inspirations.remove(ins);
			hr.setInspiration(inspirations);
			hr.setUpdated(new Date());
			hrDao.persist(hr);
			insDao.delete(ins);
		}
	}
}
