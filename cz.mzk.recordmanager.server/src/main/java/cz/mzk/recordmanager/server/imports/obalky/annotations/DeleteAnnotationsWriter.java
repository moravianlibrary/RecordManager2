package cz.mzk.recordmanager.server.imports.obalky.annotations;

import cz.mzk.recordmanager.server.model.ObalkyKnihAnnotation;
import cz.mzk.recordmanager.server.oai.dao.ObalkyKnihAnnotationDAO;
import org.hibernate.SessionFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class DeleteAnnotationsWriter extends AnnotationsAbstract implements ItemWriter<ObalkyKnihAnnotation> {

	@Autowired
	protected SessionFactory sessionFactory;

	@Autowired
	private ObalkyKnihAnnotationDAO annotationDAO;

	@Override
	public void write(List<? extends ObalkyKnihAnnotation> items) throws Exception {
		for (ObalkyKnihAnnotation annotation : items) {
			updateHr(annotation);
			annotationDAO.delete(annotation);
		}
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
	}


}
