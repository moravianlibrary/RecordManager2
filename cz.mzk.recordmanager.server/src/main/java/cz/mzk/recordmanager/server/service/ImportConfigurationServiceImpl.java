package cz.mzk.recordmanager.server.service;

import cz.mzk.recordmanager.api.model.configurations.ImportConfigurationDto;
import cz.mzk.recordmanager.api.service.ImportConfigurationService;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.oai.dao.ImportConfigurationDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

public class ImportConfigurationServiceImpl implements ImportConfigurationService {

	@Autowired
	private ImportConfigurationDAO importConfigurationDAO;

	@Autowired
	private Translator translator;

	@Override
	@Transactional(readOnly = true)
	public List<ImportConfigurationDto> getConfigurations() {
		List<ImportConfiguration> configurations = importConfigurationDAO.findAll();
		List<ImportConfigurationDto> result = new ArrayList<ImportConfigurationDto>(configurations.size());
		for (ImportConfiguration configuration: configurations) {
			result.add(translator.translate(configuration));
		}
		return result;
	}

}