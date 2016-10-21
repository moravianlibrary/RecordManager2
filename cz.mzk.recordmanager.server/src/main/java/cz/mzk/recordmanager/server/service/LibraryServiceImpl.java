package cz.mzk.recordmanager.server.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cz.mzk.recordmanager.api.model.ContactPersonDto;
import cz.mzk.recordmanager.server.model.ContactPerson;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.oai.dao.ContactPersonDAO;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;

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

	@Autowired
	private OAIHarvestConfigurationDAO harvestConfigurationDAO;

	@Autowired
	private ContactPersonDAO personDAO;

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
	@Transactional(readOnly=true)
	public LibraryDetailDto getDetail(Long libraryId) {
		Library library = libraryDao.get(libraryId);
		return this.translateWithDetails(library);
	}

	@Override
	@Transactional
	public LibraryDto updateOrCreateLibrary(LibraryDto libraryDto) {
		Library library = (libraryDto.getId() == null)? new Library() : libraryDao.get(libraryDto.getId());
		fillLibrary(library, libraryDto);
		libraryDao.save(library);
		if (libraryDto.getId() == null) {
			libraryDto.setId(library.getId());
		}
		return libraryDto;
	}

	private OAIHarvestConfiguration fillConfiguration(OAIHarvestConfiguration target, OaiHarvestConfigurationDto src) {
		fillOAIHarvestConfiguration(target, src);
		ContactPerson contact = personDAO.get(src.getContact().getId());
		fillPerson(contact, src.getContact());
		target.setContact(contact);
		return target;
	}

	@Override
	@Transactional
	public void updateOrCreateConfig(OaiHarvestConfigurationDto config, Long libraryId) {
		throw new UnsupportedOperationException("updateOrCreateConfig");
	}

	@Override
	@Transactional
	public void removeLibrary(Long libraryId) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Transactional
	public void removeOaiHarvestConfiguration(Long configId) {
		OAIHarvestConfiguration config = harvestConfigurationDAO.get(configId);
		if (config != null) {
			harvestConfigurationDAO.delete(config);
		}
	}

	private void fillOAIHarvestConfiguration(OAIHarvestConfiguration harvestConfiguration, OaiHarvestConfigurationDto config) {
		harvestConfiguration.setIdPrefix(config.getIdPrefix());
		harvestConfiguration.setBaseWeight(config.getBaseWeight());
		harvestConfiguration.setClusterIdEnabled(config.isClusterIdEnabled());
		harvestConfiguration.setFilteringEnabled(config.isFilteringEnabled());
		harvestConfiguration.setInterceptionEnabled(config.isInterceptionEnabled());
		harvestConfiguration.setLibrary(config.isLibrary());
		harvestConfiguration.setUrl(config.getUrl());
		harvestConfiguration.setRegex(config.getExtractIdRegex());
		harvestConfiguration.setHarvestJobName(config.getHarvestJobName());
		harvestConfiguration.setSet(config.getSet());
		harvestConfiguration.setMetadataPrefix(config.getMetadataPrefix());
	}


	private void fillPerson(ContactPerson target, ContactPersonDto src) {
		target.setName(src.getName());
		target.setEmail(src.getEmail());
		target.setPhone(src.getPhone());

	}

	private Library fillLibrary(Library target, LibraryDto src) {
		target.setCatalogUrl(src.getCatalogUrl());
		target.setCity(src.getCity());
		target.setUrl(src.getUrl());
		target.setName(src.getName());
		return target;
	}

	private Library translate(LibraryDto libraryDto) {
		Library library = new Library();
		library.setId(libraryDto.getId());
		library.setUrl(libraryDto.getUrl());
		library.setCity(libraryDto.getCity());
		library.setName(libraryDto.getName());
		library.setCatalogUrl(library.getCatalogUrl());
		return library;
	}

	private LibraryDetailDto translateWithDetails(Library library) {
		LibraryDetailDto libraryDetailDto = new LibraryDetailDto();
		List<OaiHarvestConfigurationDto> configs = library.getOaiHarvestConfigurations().stream()
				.map(it -> translate((OAIHarvestConfiguration) it)).collect(Collectors.toList());
		libraryDetailDto.setOaiHarvestConfigurations(configs);
		libraryDetailDto.setId(library.getId());
		libraryDetailDto.setName(library.getName());
		libraryDetailDto.setUrl(library.getUrl());
		libraryDetailDto.setCatalogUrl(library.getCatalogUrl());
		libraryDetailDto.setCity(library.getCity());
		return libraryDetailDto;
	}

	private OaiHarvestConfigurationDto translate(OAIHarvestConfiguration oaiHarvestConfiguration) {
		OaiHarvestConfigurationDto oaiHarvestConfigurationDto = new OaiHarvestConfigurationDto();
		oaiHarvestConfigurationDto.setId(oaiHarvestConfiguration.getId());
		oaiHarvestConfigurationDto.setContact(translate(oaiHarvestConfiguration.getContact()));
		oaiHarvestConfigurationDto.setIdPrefix(oaiHarvestConfiguration.getIdPrefix());
		oaiHarvestConfigurationDto.setBaseWeight(oaiHarvestConfiguration.getBaseWeight());
		oaiHarvestConfigurationDto.setClusterIdEnabled(oaiHarvestConfiguration.isClusterIdEnabled());
		oaiHarvestConfigurationDto.setFilteringEnabled(oaiHarvestConfiguration.isFilteringEnabled());
		oaiHarvestConfigurationDto.setInterceptionEnabled(oaiHarvestConfiguration.isInterceptionEnabled());
		oaiHarvestConfigurationDto.setLibrary(oaiHarvestConfiguration.isLibrary());
		oaiHarvestConfigurationDto.setUrl(oaiHarvestConfiguration.getUrl());
		oaiHarvestConfigurationDto.setSet(oaiHarvestConfiguration.getSet());
		oaiHarvestConfigurationDto.setMetadataPrefix(oaiHarvestConfiguration.getMetadataPrefix());
		oaiHarvestConfigurationDto.setExtractIdRegex(oaiHarvestConfiguration.getRegex());
		oaiHarvestConfigurationDto.setHarvestJobName(oaiHarvestConfiguration.getHarvestJobName());
		return oaiHarvestConfigurationDto;
	}

	private ContactPersonDto translate(ContactPerson contactPerson) {
		ContactPersonDto contactPersonDto = new ContactPersonDto();
		contactPersonDto.setId(contactPerson.getId());
		contactPersonDto.setEmail(contactPerson.getEmail());
		contactPersonDto.setName(contactPerson.getName());
		contactPersonDto.setPhone(contactPerson.getPhone());
		return contactPersonDto;
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
