package cz.mzk.recordmanager.server.imports.obalky.anotations;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.ObalkyKnihAnotation;
import cz.mzk.recordmanager.server.oai.dao.*;
import org.hibernate.SessionFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AnotationsWriter implements ItemWriter<ObalkyKnihAnotation> {

	@Autowired
	protected SessionFactory sessionFactory;

	@Autowired
	private ObalkyKnihAnotationDAO anotationDAO;

	@Autowired
	private IsbnDAO isbnDAO;

	@Autowired
	private CnbDAO cnbDAO;

	@Autowired
	private OclcDAO oclcDAO;

	@Autowired
	private HarvestedRecordDAO harvestedRecordDAO;

	@Autowired
	private DedupRecordDAO dedupRecordDAO;

	@Override
	public void write(List<? extends ObalkyKnihAnotation> items) throws Exception {
		for (ObalkyKnihAnotation newAnotation : items) {
			List<ObalkyKnihAnotation> anotations = anotationDAO.findByExample(newAnotation, true, "updated", "anotation");
			if (anotations.isEmpty()) { // new anotation
				anotationDAO.persist(newAnotation);
				updateHr(newAnotation);
			} else { //exists in db
				ObalkyKnihAnotation anotation = anotations.get(0);
				if (anotation.getUpdated().compareTo(newAnotation.getUpdated()) == 0) continue; // same
				anotation.setUpdated(newAnotation.getUpdated());
				anotation.setAnotation(newAnotation.getAnotation());
				anotationDAO.persist(anotation);
				updateHr(anotation);
			}
		}
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
	}

	private void updateHr(ObalkyKnihAnotation anotation) {
		Set<HarvestedRecord> hrToUpdate = new HashSet<>();
		try {
			if (anotation.getIsbn() != null) hrToUpdate.addAll(isbnDAO.findHrByIsbn(anotation.getIsbn()));
		} catch (NumberFormatException ignore) {
		}
		if (anotation.getOclc() != null) hrToUpdate.addAll(oclcDAO.findHrByOclc(anotation.getOclc()));
		if (anotation.getNbn() != null) hrToUpdate.addAll(cnbDAO.findHrByCnb(anotation.getNbn()));
		Set<DedupRecord> drToUpdate = hrToUpdate.stream().filter(hr -> hr.getDedupRecord() != null)
				.map(HarvestedRecord::getDedupRecord).collect(Collectors.toSet());
		for (DedupRecord dr : drToUpdate) {
			dr.setUpdated(new Date());
			dedupRecordDAO.persist(dr);
		}
	}
}
