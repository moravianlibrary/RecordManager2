package cz.mzk.recordmanager.server.miscellaneous.fit.fulltextAnalyser;

import cz.mzk.recordmanager.server.model.FitProject.FitProjectEnum;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFitProject;
import cz.mzk.recordmanager.server.oai.dao.FitProjectDAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

public class FulltextAnalyserWriter implements ItemWriter<List<FulltextAnalyser>> {

	private static Logger logger = LoggerFactory.getLogger(FulltextAnalyserWriter.class);

	@Autowired
	private HarvestedRecordDAO hrDao;

	@Autowired
	private FitProjectDAO fitProjectDAO;

	@Override
	public void write(List<? extends List<FulltextAnalyser>> items) throws Exception {
		for (List<FulltextAnalyser> item : items) {
			for (FulltextAnalyser fulltextAnalyser : item) {
				HarvestedRecord hr;
				if (fulltextAnalyser.getNames() == null) continue;
				if ((fulltextAnalyser.getNkpId() != null
						&& (hr = hrDao.findByHarvestConfAndRaw001Id(Constants.IMPORT_CONF_NKP, fulltextAnalyser.getNkpId())) != null)
						|| (fulltextAnalyser.getMzkId() != null
						&& (hr = hrDao.findByHarvestConfAndRaw001Id(Constants.IMPORT_CONF_MZK, fulltextAnalyser.getMzkId())) != null)) {
					Set<HarvestedRecordFitProject> results = hr.getFitProjects();
					results.add(HarvestedRecordFitProject.create(fitProjectDAO.getProjectsFromEnums(FitProjectEnum.FULLTEXT_ANALYSER), String.join("|", fulltextAnalyser.getNames())));
					hr.setFitProjects(results);
					hrDao.saveOrUpdate(hr);
				}
			}
		}
	}
}
