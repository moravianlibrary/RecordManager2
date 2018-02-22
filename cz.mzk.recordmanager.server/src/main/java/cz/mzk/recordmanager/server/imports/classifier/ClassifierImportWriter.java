package cz.mzk.recordmanager.server.imports.classifier;

import cz.mzk.recordmanager.server.model.Classifier;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@StepScope
public class ClassifierImportWriter implements ItemWriter<PredictedRecord> {

	@Autowired
	private HarvestedRecordDAO hrDao;

	public ClassifierImportWriter() {
	}

	@Override
	public void write(List<? extends PredictedRecord> items)
			throws Exception {
		for (PredictedRecord item : items) {
			HarvestedRecord hr = hrDao.findByIdAndHarvestConfiguration(item.getRecordId(), 300L);
			if (hr == null) return;
			List<Classifier> classifiers = new ArrayList<>();
			for (Pair<String, Float> value : item.getValues()) {
				Classifier newClass = new Classifier();
				newClass.setValue(value.getLeft());
				newClass.setRelevance(value.getRight());
				classifiers.add(newClass);
			}
			hrDao.dropClassifiers(hr);
			hr.setClassifiers(classifiers);
			hr.setUpdated(new Date());
			hrDao.persist(hr);
		}
	}
}
