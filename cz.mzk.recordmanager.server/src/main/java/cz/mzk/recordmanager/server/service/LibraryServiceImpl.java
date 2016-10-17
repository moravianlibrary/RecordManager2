package cz.mzk.recordmanager.server.service;

import java.util.ArrayList;
import java.util.List;

import cz.mzk.recordmanager.api.model.ContactPersonDto;
import cz.mzk.recordmanager.server.model.ContactPerson;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
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

		List<OAIHarvestConfiguration> configs = libraryDao.getOAIHarvestConfigurations(libraryId);

		if (configs.size() <= 0)
		{
			Library lib = libraryDao.get(libraryId);
			if (lib == null)
				return null;

			LibraryDetailDto details = translate(lib, new ArrayList<>());


			return details;
		}

		List<OaiHarvestConfigurationDto> harvestConfigurationDtos = new ArrayList<>();

		for (OAIHarvestConfiguration config:
				configs) {

			OaiHarvestConfigurationDto oaiHarvestConfDto = translate(config);

			harvestConfigurationDtos.add(oaiHarvestConfDto);
		}

		LibraryDetailDto detail = translate(configs.get(0).getLibrary(), harvestConfigurationDtos);

		return detail;
	}


	@Override
	@Transactional
	public LibraryDto updateOrCreateLibrary(LibraryDto libraryDto) {

		Library lib;

		if (libraryDto.getId() == null)
			lib = new Library();
		else
			lib = libraryDao.get(libraryDto.getId());

		long id = -10000;

		if (lib == null || lib.getId() == null)
		{
			lib = new Library();
			fillLibrary(lib, libraryDto);
			id =  libraryDao.save(lib);

		}else
		{
			lib.setId(libraryDto.getId());
			fillLibrary(lib, libraryDto);
			libraryDao.updateLibrary(lib);
			id = libraryDto.getId();
		}

		return translate(libraryDao.get(id));

	}

	@Override
	@Transactional
	public void updateOrCreateConfig(OaiHarvestConfigurationDto config, Long libraryId) {
		Library lib = libraryDao.get(libraryId);

		if (
				lib == null ||
						config.getContact() == null ||
						config.getContact().getId() == null ||
						personDAO.get(config.getContact().getId()) == null
				)
		{
			return;
		}
		OAIHarvestConfiguration oaiHarvestConfiguration;
		if (config.getId() == null)	//Detect creating new configuration
		{
			//TODO: How to add contact to configuration??
			oaiHarvestConfiguration = new OAIHarvestConfiguration();
		}else
		{
			oaiHarvestConfiguration = harvestConfigurationDAO.get(config.getId());
		}

		fillOAIHarvestConfiguration(oaiHarvestConfiguration, config);

		ContactPerson contact = personDAO.get(config.getContact().getId());

		fillPerson(contact, config.getContact());

		oaiHarvestConfiguration.setContact(contact);

		harvestConfigurationDAO.saveOrUpdate(oaiHarvestConfiguration);
	}

//
//	@Override
//	@Transactional
//	public void removeLibrary(Long libraryId) { throw new UnsupportedOperationException();
//	}
//

//
//	@Override
//	@Transactional
//	public void removeOaiHarvestConfiguration(Long configId) {
//		OAIHarvestConfiguration config = harvestConfigurationDAO.get(configId);
//		if (config != null)
//			harvestConfigurationDAO.delete(config);
//	}

	private void fillOAIHarvestConfiguration(OAIHarvestConfiguration harvestConfiguration, OaiHarvestConfigurationDto config)
	{

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


	private void fillPerson(ContactPerson target, ContactPersonDto src)
	{
		target.setName(src.getName());
		target.setEmail(src.getEmail());
		target.setPhone(src.getPhone());

	}
	private Library fillLibrary(Library target, LibraryDto src)
	{
		target.setCatalogUrl(src.getCatalogUrl());
		target.setCity(src.getCity());
		target.setUrl(src.getUrl());
		target.setName(src.getName());
		return target;
	}

	private Library translate(LibraryDto libraryDto)
	{
		Library library = new Library();
		library.setId(libraryDto.getId());
		library.setUrl(libraryDto.getUrl());
		library.setCity(libraryDto.getCity());
		library.setName(libraryDto.getName());
		library.setCatalogUrl(library.getCatalogUrl());

		return library;
	}
	private LibraryDetailDto translate(Library library, List<OaiHarvestConfigurationDto> configs)
	{
		LibraryDetailDto libraryDetailDto = new LibraryDetailDto();

		libraryDetailDto.setOaiHarvestConfigurations(configs);
		libraryDetailDto.setId(library.getId());
		libraryDetailDto.setName(library.getName());
		libraryDetailDto.setUrl(library.getUrl());
		libraryDetailDto.setCatalogUrl(library.getCatalogUrl());
		libraryDetailDto.setCity(library.getCity());

		return libraryDetailDto;
	}

	private OaiHarvestConfigurationDto translate(OAIHarvestConfiguration oaiHarvestConfiguration)
	{
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

	private ContactPersonDto translate(ContactPerson contactPerson)
	{
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
