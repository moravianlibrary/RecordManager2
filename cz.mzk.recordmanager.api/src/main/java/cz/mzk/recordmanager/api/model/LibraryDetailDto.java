package cz.mzk.recordmanager.api.model;

import java.util.List;

public class LibraryDetailDto extends LibraryDto {

	private static final long serialVersionUID = 1L;
	
	private List<OaiHarvestConfigurationDto> oaiHarvestConfigurations;
	
	public List<OaiHarvestConfigurationDto> getOaiHarvestConfigurations() {
		return oaiHarvestConfigurations;
	}

	public void setOaiHarvestConfigurations(
			List<OaiHarvestConfigurationDto> oaiHarvestConfigurations) {
		this.oaiHarvestConfigurations = oaiHarvestConfigurations;
	}

}
