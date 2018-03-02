package cz.mzk.recordmanager.api.service;

import java.util.List;

import cz.mzk.recordmanager.api.model.configurations.ImportConfigurationDto;
import cz.mzk.recordmanager.api.model.LibraryDetailDto;
import cz.mzk.recordmanager.api.model.LibraryDto;

public interface LibraryService {

	List<LibraryDto> getLibraries();

	LibraryDetailDto getDetail(Long libraryId);

	LibraryDto updateOrCreateLibrary(LibraryDto libraryDto);

	void removeLibrary(Long libraryId);

	void updateOrCreateConfig(ImportConfigurationDto config,
							  Long libraryId);

	void removeConfiguration(Long configId);


}
