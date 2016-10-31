package cz.mzk.recordmanager.server.service;


import cz.mzk.recordmanager.api.model.ImportConfigurationDto;
import cz.mzk.recordmanager.api.model.LibraryDto;
import cz.mzk.recordmanager.api.service.ImportConfigurationService;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.model.Library;
import cz.mzk.recordmanager.server.oai.dao.ImportConfigurationDAO;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class ImportConfigurationServiceImpl implements ImportConfigurationService {

	@Autowired
	private ImportConfigurationDAO importConfigurationDAO;

	@Autowired
	private Translator translator;

	@Override
	public List<ImportConfigurationDto> getConfigurations() {
		List<ImportConfiguration> configurations = importConfigurationDAO.findAll();
		List<ImportConfigurationDto> result = new ArrayList<ImportConfigurationDto>(configurations.size());
		for (ImportConfiguration configuration: configurations) {
			result.add(this.translate(configuration));
		}
		return result;
	}

	private ImportConfigurationDto translate(ImportConfiguration configuration){
		ImportConfigurationDto importConfigurationDto = new ImportConfigurationDto();
		importConfigurationDto.setId(configuration.getId());
		importConfigurationDto.setIdPrefix(configuration.getIdPrefix());
		importConfigurationDto.setLibrary(translator.translate(configuration.getLibrary()));
		return importConfigurationDto;
	}



}
