package cz.mzk.recordmanager.api.service;

import java.util.List;

import cz.mzk.recordmanager.api.model.ImportConfigurationDto;
import cz.mzk.recordmanager.api.model.LibraryDetailDto;
import cz.mzk.recordmanager.api.model.LibraryDto;
import cz.mzk.recordmanager.api.model.OaiHarvestConfigurationDto;

public interface LibraryService {

	public List<LibraryDto> getLibraries();

	public LibraryDetailDto getDetail(Long libraryId);

	public LibraryDto updateOrCreateLibrary(LibraryDto libraryDto);

	public void removeLibrary(Long libraryId);

	public void updateOrCreateConfig(ImportConfigurationDto config,
			Long libraryId);

	public void removeOaiHarvestConfiguration(Long configId);

}
