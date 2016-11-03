package cz.mzk.recordmanager.server.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

	@Autowired
	private Translator translator;

	@Override
	@Transactional(readOnly=true)
	public List<LibraryDto> getLibraries() {
		List<Library> libraries = libraryDao.findAll();
		List<LibraryDto> result = new ArrayList<LibraryDto>(libraries.size());
		for (Library library: libraries) {
			result.add(translator.translate(library));
		}
		return result;
	}

	@Override
	@Transactional(readOnly=true)
	public LibraryDetailDto getDetail(Long libraryId) {
		Library library = libraryDao.get(libraryId);
		if (library == null)
			return null;
		return this.translateWithDetails(library);
	}


	@Override
	@Transactional
	public LibraryDto updateOrCreateLibrary(LibraryDto libraryDto) {
		Library library = (libraryDto.getId() == null)? new Library() : libraryDao.get(libraryDto.getId());
		library = fillLibrary(library, libraryDto);
		libraryDao.persist(library);
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
	public OaiHarvestConfigurationDto updateOrCreateConfig(OaiHarvestConfigurationDto config, Long libraryId) {
		Library lib = libraryDao.get(libraryId);

		if (
				lib == null ||
						config.getContact() == null ||
						config.getContact().getId() == null ||
						personDAO.get(config.getContact().getId()) == null
				)
		{
			return null;
		}
		OAIHarvestConfiguration oaiHarvestConfiguration;
		long id = -1000;
		if (config.getId() == null)	//Detect creating new configuration
		{
			//TODO: How to add contact to configuration??
			oaiHarvestConfiguration = new OAIHarvestConfiguration();

			oaiHarvestConfiguration = fillConfiguration(oaiHarvestConfiguration, config);

			harvestConfigurationDAO.persist(oaiHarvestConfiguration);

			id = oaiHarvestConfiguration.getId();

		}else
		{
			oaiHarvestConfiguration = harvestConfigurationDAO.get(config.getId());

			oaiHarvestConfiguration = fillConfiguration(oaiHarvestConfiguration, config);

			id = config.getId();

			harvestConfigurationDAO.update(oaiHarvestConfiguration);
		}
		return translator.translate(harvestConfigurationDAO.get(id));
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
		if (config != null)
			harvestConfigurationDAO.delete(config);
	}

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


	private void fillPerson(ContactPerson target, ContactPersonDto src) {
		target.setName(src.getName());
		target.setEmail(src.getEmail());
		target.setPhone(src.getPhone());
	}

	private Library fillLibrary(Library target, LibraryDto src){
		target.setId(src.getId());
		target.setCity(src.getCity());
		target.setName(src.getName());
		target.setUrl(src.getName());
		target.setCatalogUrl(src.getCatalogUrl());
		return target;
	}

	private LibraryDetailDto translateWithDetails(Library library) {
		LibraryDetailDto libraryDetailDto = new LibraryDetailDto();
		List<OaiHarvestConfigurationDto> configs = library.getOaiHarvestConfigurations().stream()
				.map(it -> translator.translate((OAIHarvestConfiguration) it)).collect(Collectors.toList());
		libraryDetailDto.setOaiHarvestConfigurations(configs);
		libraryDetailDto.setId(library.getId());
		libraryDetailDto.setName(library.getName());
		libraryDetailDto.setUrl(library.getUrl());
		libraryDetailDto.setCatalogUrl(library.getCatalogUrl());
		libraryDetailDto.setCity(library.getCity());

		return libraryDetailDto;
	}




}
