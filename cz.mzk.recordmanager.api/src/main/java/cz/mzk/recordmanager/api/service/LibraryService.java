package cz.mzk.recordmanager.api.service;

import java.util.List;

import cz.mzk.recordmanager.api.model.configurations.ImportConfigurationDto;
import cz.mzk.recordmanager.api.model.LibraryDetailDto;
import cz.mzk.recordmanager.api.model.LibraryDto;

public interface LibraryService {

	public List<LibraryDto> getLibraries();

	public LibraryDetailDto getDetail(Long libraryId);

	public LibraryDto updateOrCreateLibrary(LibraryDto libraryDto);

	public void removeLibrary(Long libraryId);

	public void updateOrCreateConfig(ImportConfigurationDto config,
			Long libraryId);

	public void removeConfiguration(Long configId);


}
