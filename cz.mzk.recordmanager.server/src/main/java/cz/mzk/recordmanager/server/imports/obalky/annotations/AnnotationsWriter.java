package cz.mzk.recordmanager.server.imports.obalky.annotations;

import cz.mzk.recordmanager.server.model.ObalkyKnihAnnotation;
import cz.mzk.recordmanager.server.oai.dao.ObalkyKnihAnnotationDAO;
import cz.mzk.recordmanager.server.util.CleaningUtils;
import org.hibernate.SessionFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class AnnotationsWriter extends AnnotationsAbstract implements ItemWriter<ObalkyKnihAnnotation> {

	@Autowired
	protected SessionFactory sessionFactory;

	@Autowired
	private ObalkyKnihAnnotationDAO annotationDAO;

	private static final Pattern RN = Pattern.compile("\\\\[nr]");
	private static final Pattern WHITE_SPACES = Pattern.compile("\\s+");
	private static final Pattern HTML = Pattern.compile("<[^>]+>");

	@Override
	public void write(List<? extends ObalkyKnihAnnotation> items) throws Exception {
		for (ObalkyKnihAnnotation newAnnotation : items) {
			if (newAnnotation.getIsbn() == null && newAnnotation.getCnb() == null && newAnnotation.getOclc() == null)
				continue;
			newAnnotation.setAnnotation(cleanText(newAnnotation.getAnnotation()));
			List<ObalkyKnihAnnotation> annotations = annotationDAO.findByBookId(newAnnotation.getBookId());
			if (annotations.isEmpty()) { // new annotation
				annotationDAO.persist(newAnnotation);
				updateHr(newAnnotation);
			} else { //exists in db
				ObalkyKnihAnnotation annotation = annotations.get(0);
				if (annotation.getUpdated().compareTo(newAnnotation.getUpdated()) != 0
						|| !annotation.getAnnotation().equals(newAnnotation.getAnnotation())) {
					annotation.setUpdated(newAnnotation.getUpdated());
					annotation.setAnnotation(newAnnotation.getAnnotation());
					updateHr(annotation);
				}
				annotation.setLastHarvest(new Date());
				annotationDAO.persist(annotation);
			}
		}
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
	}

	private static String cleanText(String text) {
		text = CleaningUtils.replaceAll(text, RN, " ");
		text = CleaningUtils.replaceAll(text, HTML, " ");
		text = CleaningUtils.replaceAll(text, WHITE_SPACES, " ");
		text = text.trim();
		return text;
	}
}
