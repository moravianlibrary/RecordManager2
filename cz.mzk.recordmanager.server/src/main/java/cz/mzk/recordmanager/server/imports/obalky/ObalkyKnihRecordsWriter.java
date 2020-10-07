package cz.mzk.recordmanager.server.imports.obalky;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.ObalkyKnihTOC;
import cz.mzk.recordmanager.server.oai.dao.*;
import cz.mzk.recordmanager.server.util.CleaningUtils;
import cz.mzk.recordmanager.server.util.MetadataUtils;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
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

	private static final Logger logger = LoggerFactory.getLogger(ObalkyKnihRecordsWriter.class);

	private static final Pattern RN = Pattern.compile("\\\\[nr]");
	private static final Pattern WHITE_SPACES = Pattern.compile("\\s+");
	private static final Pattern DOT = Pattern.compile("\\.{3,}");

	@Override
	public void write(List<? extends ObalkyKnihTOC> items) throws Exception {
		for (ObalkyKnihTOC newToc : items) {
			if (newToc.getIsbn() == null && newToc.getNbn() == null && newToc.getOclc() == null)
				continue;
			List<ObalkyKnihTOC> tocs = obalkyKnihTOCDao.findByBookId(newToc.getBookId());
			if (newToc.getToc() != null) newToc.setToc(cleanText(newToc.getToc()));
			if (tocs.isEmpty()) { // new toc
				obalkyKnihTOCDao.persist(newToc);
				updateHr(newToc);
			} else { //exists in db
				ObalkyKnihTOC toc = tocs.get(0);
				try {
					if (toc.getUpdated() == null || toc.getUpdated().compareTo(newToc.getUpdated()) != 0
							|| !toc.getToc().equals(newToc.getToc())) {
						if (toc.getBibInfo() == null) toc.setBibInfo();
						toc.setUpdated(newToc.getUpdated());
						toc.setNbn(newToc.getNbn());
						toc.setEan(newToc.getEan());
						toc.setIsbn(newToc.getIsbn());
						toc.setOclc(newToc.getOclc());
						toc.setToc(newToc.getToc());
						updateHr(toc);
					}
					toc.setLastHarvest(new Date());
					obalkyKnihTOCDao.saveOrUpdate(toc);
				} catch (Exception e) {
					logger.info(toc.toString());
					logger.error(e.toString());
				}
			}
		}
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
	}

	private void updateHr(ObalkyKnihTOC toc) {
		Set<HarvestedRecord> hrToUpdate = new HashSet<>();
		if (toc.getIsbn() != null) hrToUpdate.addAll(isbnDAO.findHrByIsbn(toc.getIsbn()));
		if (toc.getOclc() != null) hrToUpdate.addAll(oclcDAO.findHrByOclc(toc.getOclc()));
		if (toc.getNbn() != null) hrToUpdate.addAll(cnbDAO.findHrByCnb(toc.getNbn()));
		if (toc.getEan() != null) hrToUpdate.addAll(eanDAO.findHrByEan(toc.getEan()));
		Set<DedupRecord> drToUpdate = hrToUpdate.stream().filter(hr -> hr.getDedupRecord() != null)
				.map(HarvestedRecord::getDedupRecord).collect(Collectors.toSet());
		for (DedupRecord dr : drToUpdate) {
			dr.setUpdated(new Date());
			dedupRecordDAO.saveOrUpdate(dr);
		}
	}

	private static String cleanText(String text) {
		text = CleaningUtils.replaceAll(text, RN, " ");
		text = CleaningUtils.replaceAll(text, WHITE_SPACES, " ");
		text = CleaningUtils.replaceAll(text, DOT, "...");
		text = text.trim();
		return text;
	}

}
