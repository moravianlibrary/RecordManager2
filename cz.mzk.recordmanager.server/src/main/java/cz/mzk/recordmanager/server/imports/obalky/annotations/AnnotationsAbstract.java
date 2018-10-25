package cz.mzk.recordmanager.server.imports.obalky.annotations;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.ObalkyKnihAnnotation;
import cz.mzk.recordmanager.server.oai.dao.CnbDAO;
import cz.mzk.recordmanager.server.oai.dao.DedupRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.IsbnDAO;
import cz.mzk.recordmanager.server.oai.dao.OclcDAO;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class AnnotationsAbstract {

	@Autowired
	private IsbnDAO isbnDAO;

	@Autowired
	private CnbDAO cnbDAO;

	@Autowired
	private OclcDAO oclcDAO;

	@Autowired
	private DedupRecordDAO dedupRecordDAO;

	void updateHr(ObalkyKnihAnnotation annotation) {
		Set<HarvestedRecord> hrToUpdate = new HashSet<>();
		try {
			if (annotation.getIsbn() != null) hrToUpdate.addAll(isbnDAO.findHrByIsbn(annotation.getIsbn()));
		} catch (NumberFormatException ignore) {
		}
		if (annotation.getOclc() != null) hrToUpdate.addAll(oclcDAO.findHrByOclc(annotation.getOclc()));
		if (annotation.getCnb() != null) hrToUpdate.addAll(cnbDAO.findHrByCnb(annotation.getCnb()));
		Set<DedupRecord> drToUpdate = hrToUpdate.stream().filter(hr -> hr.getDedupRecord() != null)
				.map(HarvestedRecord::getDedupRecord).collect(Collectors.toSet());
		for (DedupRecord dr : drToUpdate) {
			dr.setUpdated(new Date());
			dedupRecordDAO.persist(dr);
		}
	}
}
