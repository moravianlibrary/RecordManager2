package cz.mzk.recordmanager.server.imports.obalky.anotations;

import cz.mzk.recordmanager.server.model.ObalkyKnihAnotation;
import cz.mzk.recordmanager.server.oai.dao.ObalkyKnihAnotationDAO;
import org.hibernate.SessionFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class AnotationsWriter implements ItemWriter<ObalkyKnihAnotation> {

	@Autowired
	protected SessionFactory sessionFactory;

	@Autowired
	private ObalkyKnihAnotationDAO anotationDAO;

	@Override
	public void write(List<? extends ObalkyKnihAnotation> items) throws Exception {
		for (ObalkyKnihAnotation newAnotation : items) {
			List<ObalkyKnihAnotation> anotations = anotationDAO.findByExample(newAnotation, true, "updated", "anotation");
			if (anotations.isEmpty()) {
				anotationDAO.persist(newAnotation);
			} else { //exists in db
				ObalkyKnihAnotation anotation = anotations.get(0);
				if (anotation.getUpdated().equals(newAnotation.getUpdated())) continue;
				anotation.setUpdated(newAnotation.getUpdated());
				anotation.setAnotation(newAnotation.getAnotation());
				anotationDAO.persist(anotation);
			}
		}
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
	}

}
