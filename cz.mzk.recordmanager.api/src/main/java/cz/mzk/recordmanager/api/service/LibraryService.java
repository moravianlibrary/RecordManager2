package cz.mzk.recordmanager.api.service;

import java.util.List;

import cz.mzk.recordmanager.api.model.LibraryDetailDto;
import cz.mzk.recordmanager.api.model.LibraryDto;
import cz.mzk.recordmanager.api.model.OaiHarvestConfigurationDto;

public interface LibraryService {
	
	public List<LibraryDto> getLibraries();
	
	public LibraryDetailDto getDetail(Long libraryId);
	
	public void updateOrCreateLibrary(LibraryDto library);
	
	public void removeLibrary(Long libraryId);
	
	public void updateOrCreateConfig(OaiHarvestConfigurationDto config);
	
	public void removeOaiHarvestConfiguration(Long configId);
	
}
