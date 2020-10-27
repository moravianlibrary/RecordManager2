package cz.mzk.recordmanager.server.miscellaneous.fit.classifier;

import cz.mzk.recordmanager.server.bibliolinker.keys.DelegatingBiblioLinkerKeysParser;
import cz.mzk.recordmanager.server.model.FitProject.FitProjectEnum;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFitProject;
import cz.mzk.recordmanager.server.oai.dao.FitProjectDAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.ProgressLogger;
import org.hibernate.SessionFactory;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ImportClassifierWriter implements ItemWriter<List<Record>> {

	private static final Logger logger = LoggerFactory.getLogger(ImportClassifierWriter.class);
	private final ProgressLogger progressLogger = new ProgressLogger(logger);

	@Autowired
	protected DelegatingBiblioLinkerKeysParser biblioLinkerKeysParser;

	@Autowired
	private HarvestedRecordDAO hrDao;

	@Autowired
	protected SessionFactory sessionFactory;

	@Autowired
	private FitProjectDAO fitProjectDAO;

	private final Long configurationId;


	private static final List<String> ENRICHING_FIELD = Collections.singletonList("072");

	public ImportClassifierWriter(Long configurationId) {
		this.configurationId = configurationId;
	}

	@Override
	public void write(List<? extends List<Record>> items) throws Exception {
		try {
			writeInner(items);
		} finally {
			sessionFactory.getCurrentSession().flush();
			sessionFactory.getCurrentSession().clear();
		}
	}

	protected void writeInner(List<? extends List<Record>> items) {
		for (List<Record> records : items) {
			for (Record currentRecord : records) {
				progressLogger.incrementAndLogProgress();
				List<DataField> dataFields = new ArrayList<>();
				String oaiId = "";
				for (DataField df : currentRecord.getDataFields()) {
					if (df.getTag().equals("OAI")) oaiId = df.getSubfield('a').getData();
					if (ENRICHING_FIELD.contains(df.getTag())) dataFields.add(df);
				}
				HarvestedRecord hr = hrDao.findByIdAndHarvestConfiguration(oaiId, configurationId);
				if (hr == null) continue;
				for (DataField df : dataFields) {
					Set<HarvestedRecordFitProject> results = hr.getFitProjects();
					results.add(HarvestedRecordFitProject.create(
							fitProjectDAO.getProjectsFromEnums(FitProjectEnum.CLASSIFIER), df.toString()));
					hr.setFitProjects(results);
				}
				hrDao.saveOrUpdate(hr);
			}
		}
	}

}
