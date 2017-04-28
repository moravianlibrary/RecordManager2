package cz.mzk.recordmanager.server.service;

import java.util.ArrayList;
import java.util.List;
import cz.mzk.recordmanager.api.model.*;
import cz.mzk.recordmanager.api.model.configurations.DownloadImportConfigurationDto;
import cz.mzk.recordmanager.api.model.configurations.ImportConfigurationDto;
import cz.mzk.recordmanager.api.model.configurations.KrameriusConfigurationDto;
import cz.mzk.recordmanager.api.model.configurations.OaiHarvestConfigurationDto;
import cz.mzk.recordmanager.server.model.*;
import cz.mzk.recordmanager.server.oai.dao.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import cz.mzk.recordmanager.api.service.LibraryService;

public class LibraryServiceImpl implements LibraryService {

	@Autowired
	private LibraryDAO libraryDao;

	@Autowired
	private OAIHarvestConfigurationDAO harvestConfigurationDAO;

	@Autowired
	private KrameriusConfigurationDAO krameriusConfigurationDAO;

	@Autowired
	private DownloadImportConfigurationDAO downloadImportConfigurationDAO;

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
		if (library == null) {
			return null;
		}
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


	@Override
	@Transactional
	public void updateOrCreateConfig(ImportConfigurationDto config, Long libraryId) {
		Library lib = libraryDao.get(libraryId);

		if (lib != null){
			if (config.getId() != null){
				if (config instanceof OaiHarvestConfigurationDto){
					OAIHarvestConfiguration  configuration = harvestConfigurationDAO.get(config.getId());
					if (configuration != null) {
						fillHarvestConfiguration(configuration, (OaiHarvestConfigurationDto) config);
						harvestConfigurationDAO.persist(configuration);
					}

				}

				if (config instanceof KrameriusConfigurationDto){
					KrameriusConfiguration configuration = krameriusConfigurationDAO.get(config.getId());
					if (configuration != null){
						fillKramConfiguration(configuration, (KrameriusConfigurationDto) config);
						krameriusConfigurationDAO.persist(configuration);
					}

				}

				if (config instanceof DownloadImportConfigurationDto){
					DownloadImportConfiguration configuration = downloadImportConfigurationDAO.get(config.getId());
					if (configuration != null){
						configuration.setLibrary(lib);
						downloadImportConfigurationDAO.persist(configuration);
					}

				}
			}
		}
	}

	@Override
	@Transactional
	public void removeLibrary(Long libraryId) {
		Library lib = libraryDao.get(libraryId);
		if (lib != null) {
			libraryDao.delete(lib);
		}

	}

	@Override
	@Transactional
	public void removeConfiguration(Long configId) {
		OAIHarvestConfiguration config = harvestConfigurationDAO.get(configId);
		if (config != null) {
			harvestConfigurationDAO.delete(config);
			return;
		}

		KrameriusConfiguration kramConf = krameriusConfigurationDAO.get(configId);
		if (kramConf != null) {
			krameriusConfigurationDAO.delete(kramConf);
			return;
		}

		DownloadImportConfiguration dowConf = downloadImportConfigurationDAO.get(configId);
		if (dowConf != null) {
			downloadImportConfigurationDAO.delete(dowConf);
			return;
		}
	}

	private void fillImportConfig(ImportConfiguration importConfiguration, ImportConfigurationDto config){
		importConfiguration.setIdPrefix(config.getIdPrefix());
		importConfiguration.setBaseWeight(config.getBaseWeight());
		importConfiguration.setClusterIdEnabled(config.isClusterIdEnabled());
		importConfiguration.setFilteringEnabled(config.isFilteringEnabled());
		importConfiguration.setInterceptionEnabled(config.isInterceptionEnabled());
		importConfiguration.setLibrary(config.isThisLibrary());
	}

	private KrameriusConfiguration fillKramConfiguration(KrameriusConfiguration target, KrameriusConfigurationDto src){
		fillKrameriusConfiguration(target, src);
		ContactPerson contact = personDAO.get(src.getContact().getId());
		fillPerson(contact, src.getContact());
		return target;
	}

	private void fillKrameriusConfiguration(KrameriusConfiguration krameriusConfiguration, KrameriusConfigurationDto config){
		fillImportConfig(krameriusConfiguration, config);
		krameriusConfiguration.setUrl(config.getUrl());
		krameriusConfiguration.setUrlSolr(config.getUrlSolr());
		krameriusConfiguration.setQueryRows(config.getQueryRows());
		krameriusConfiguration.setMetadataStream(config.getMetadataStream());
		krameriusConfiguration.setAuthToken(config.getAuthToken());
		krameriusConfiguration.setDownloadPrivateFulltexts(config.isDownloadPrivateFulltexts());
		krameriusConfiguration.setFulltextHarvestType(config.getFulltextHarvestType());
		krameriusConfiguration.setHarvestJobName(config.getHarvestJobName());
	}

	private OAIHarvestConfiguration fillHarvestConfiguration(OAIHarvestConfiguration target, OaiHarvestConfigurationDto src) {
		fillOAIHarvestConfiguration(target, src);
		ContactPerson contact = personDAO.get(src.getContact().getId());
		fillPerson(contact, src.getContact());
		target.setContact(contact);
		return target;
	}

	private void fillOAIHarvestConfiguration(OAIHarvestConfiguration harvestConfiguration, OaiHarvestConfigurationDto config) {
		fillImportConfig(harvestConfiguration, config);

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
		target.setUrl(src.getUrl());
		target.setCatalogUrl(src.getCatalogUrl());
		return target;
	}

	private LibraryDetailDto translateWithDetails(Library library) {
		LibraryDetailDto libraryDetailDto = new LibraryDetailDto();
		List<ImportConfigurationDto> configs = new ArrayList<>();
		library.getOaiHarvestConfigurations().forEach(conf -> {
			if (conf instanceof OAIHarvestConfiguration) {
				configs.add(translator.translate((OAIHarvestConfiguration) conf));
			}
			if (conf instanceof KrameriusConfiguration) {
				configs.add(translator.translate((KrameriusConfiguration) conf));
			}
			if (conf instanceof DownloadImportConfiguration) {
				configs.add(translator.translate((DownloadImportConfiguration) conf));
			}

		});
		libraryDetailDto.setOaiHarvestConfigurations(configs);
		libraryDetailDto.setId(library.getId());
		libraryDetailDto.setName(library.getName());
		libraryDetailDto.setUrl(library.getUrl());
		libraryDetailDto.setCatalogUrl(library.getCatalogUrl());
		libraryDetailDto.setCity(library.getCity());

		return libraryDetailDto;
	}

}
