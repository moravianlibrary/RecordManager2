package cz.mzk.recordmanager.api.model;

import cz.mzk.recordmanager.api.model.configurations.ImportConfigurationDto;

import java.util.List;

public class LibraryDetailDto extends LibraryDto {

	private static final long serialVersionUID = 1L;

	private List<ImportConfigurationDto> oaiHarvestConfigurations;

	public List<ImportConfigurationDto> getOaiHarvestConfigurations() {
		return oaiHarvestConfigurations;
	}

	public void setOaiHarvestConfigurations(
			List<ImportConfigurationDto> oaiHarvestConfigurations) {
		this.oaiHarvestConfigurations = oaiHarvestConfigurations;
	}

}
