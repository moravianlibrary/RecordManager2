package cz.mzk.recordmanager.api.service;

import cz.mzk.recordmanager.api.model.configurations.ImportConfigurationDto;

import java.util.List;

public interface ImportConfigurationService {

	public List<ImportConfigurationDto> getConfigurations();

}
