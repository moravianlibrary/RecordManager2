package cz.mzk.recordmanager.server.imports.obalky;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.ObalkyKnihTOC;
import cz.mzk.recordmanager.server.oai.dao.*;
import org.hibernate.SessionFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ObalkyKnihRecordsWriter implements ItemWriter<ObalkyKnihTOC> {

	@Autowired
	private ObalkyKnihTOCDAO obalkyKnihTOCDao;

	@Autowired
	protected SessionFactory sessionFactory;

	@Autowired
	private IsbnDAO isbnDAO;

	@Autowired
	private CnbDAO cnbDAO;

	@Autowired
	private OclcDAO oclcDAO;

	@Autowired
	private EanDAO eanDAO;

	@Autowired
	private DedupRecordDAO dedupRecordDAO;

	@Override
	public void write(List<? extends ObalkyKnihTOC> items) throws Exception {
		for (ObalkyKnihTOC newToc : items) {
			if (newToc.getIsbn() == null && newToc.getNbn() == null && newToc.getOclc() == null)
				continue;
			List<ObalkyKnihTOC> tocs = obalkyKnihTOCDao.findByBookId(newToc.getBookId());
			if (tocs.isEmpty()) { // new toc
				obalkyKnihTOCDao.persist(newToc);
				updateHr(newToc);
			} else { //exists in db
				ObalkyKnihTOC toc = tocs.get(0);
				if (toc.getUpdated() == null || toc.getUpdated().compareTo(newToc.getUpdated()) != 0) {
					toc.setUpdated(newToc.getUpdated());
					toc.setNbn(newToc.getNbn());
					toc.setEan(newToc.getEan());
					toc.setOclc(newToc.getOclc());
					toc.setIsbn(newToc.getIsbn());
					toc.setToc(newToc.getToc());
					updateHr(toc);
				}
				toc.setLastHarvest(new Date());
				obalkyKnihTOCDao.persist(toc);
			}
		}
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
	}

	private void updateHr(ObalkyKnihTOC toc) {
		Set<HarvestedRecord> hrToUpdate = new HashSet<>();
		try {
			if (toc.getIsbn() != null) hrToUpdate.addAll(isbnDAO.findHrByIsbn(toc.getIsbn()));
		} catch (NumberFormatException ignore) {
		}
		if (toc.getOclc() != null) hrToUpdate.addAll(oclcDAO.findHrByOclc(toc.getOclc()));
		if (toc.getNbn() != null) hrToUpdate.addAll(cnbDAO.findHrByCnb(toc.getNbn()));
		if (toc.getEan() != null) hrToUpdate.addAll(eanDAO.findHrByEan(toc.getEan()));
		Set<DedupRecord> drToUpdate = hrToUpdate.stream().filter(hr -> hr.getDedupRecord() != null)
				.map(HarvestedRecord::getDedupRecord).collect(Collectors.toSet());
		for (DedupRecord dr : drToUpdate) {
			dr.setUpdated(new Date());
			dedupRecordDAO.persist(dr);
		}
	}

}
