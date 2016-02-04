package cz.mzk.recordmanager.server.imports;

import java.util.List;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.model.ObalkyKnihTOC;
import cz.mzk.recordmanager.server.oai.dao.ObalkyKnihTOCDAO;

public class ObalkyKnihRecordsWriter implements ItemWriter<ObalkyKnihTOC> {

	private static Logger logger = LoggerFactory.getLogger(ObalkyKnihRecordsWriter.class);

	@Autowired
	private ObalkyKnihTOCDAO obalkyKnihTOCDao;

	@Autowired
	protected SessionFactory sessionFactory;

	@Override
	public void write(List<? extends ObalkyKnihTOC> items) throws Exception {
		for (ObalkyKnihTOC item : items) {
			List<ObalkyKnihTOC> tocs = obalkyKnihTOCDao.findByExample(item, true, "toc");
			if (tocs.isEmpty()) {
				obalkyKnihTOCDao.persist(item);
			} else {
				ObalkyKnihTOC toc = tocs.get(0);
				toc.setToc(item.getToc());
				obalkyKnihTOCDao.persist(toc);
			}
		}
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
	}

}
