package cz.mzk.recordmanager.api.service;

import java.util.List;

import cz.mzk.recordmanager.api.model.LibraryDetailDto;
import cz.mzk.recordmanager.api.model.LibraryDto;
import cz.mzk.recordmanager.api.model.OaiHarvestConfigurationDto;

public interface LibraryService {
	
	List<LibraryDto> getLibraries();

	LibraryDetailDto getDetail(Long libraryId);

	LibraryDto updateOrCreateLibrary(LibraryDto libraryDto);

	OaiHarvestConfigurationDto updateOrCreateConfig(OaiHarvestConfigurationDto config, Long libraryId);

//	public void removeLibrary(Long libraryId);
//
//	public void updateOrCreateConfig(OaiHarvestConfigurationDto configm, Long libraryId);
//
//	public void removeOaiHarvestConfiguration(Long configId);
	
}
