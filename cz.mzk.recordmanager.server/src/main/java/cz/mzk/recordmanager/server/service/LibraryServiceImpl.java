package cz.mzk.recordmanager.server.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import cz.mzk.recordmanager.api.model.LibraryDetailDto;
import cz.mzk.recordmanager.api.model.LibraryDto;
import cz.mzk.recordmanager.api.model.OaiHarvestConfigurationDto;
import cz.mzk.recordmanager.api.service.LibraryService;
import cz.mzk.recordmanager.server.model.Library;
import cz.mzk.recordmanager.server.oai.dao.LibraryDAO;

public class LibraryServiceImpl implements LibraryService {

	@Autowired
	private LibraryDAO libraryDao;

	@Override
	@Transactional(readOnly=true)
	public List<LibraryDto> getLibraries() {
		List<Library> libraries = libraryDao.findAll();
		List<LibraryDto> result = new ArrayList<LibraryDto>(libraries.size());
		for (Library library: libraries) {
			result.add(this.translate(library));
		}
		return result;
	}

	@Override
	public LibraryDetailDto getDetail(Long libraryId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateOrCreateLibrary(LibraryDto library) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeLibrary(Long libraryId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateOrCreateConfig(OaiHarvestConfigurationDto config) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeOaiHarvestConfiguration(Long configId) {
		throw new UnsupportedOperationException();
	}

	private LibraryDto translate(Library library) {
		LibraryDto libraryDto = new LibraryDto();
		libraryDto.setId(library.getId());
		libraryDto.setCity(library.getCity());
		libraryDto.setName(library.getName());
		libraryDto.setCatalogUrl(library.getCatalogUrl());
		libraryDto.setUrl(library.getUrl());
		return libraryDto;
	}

}
