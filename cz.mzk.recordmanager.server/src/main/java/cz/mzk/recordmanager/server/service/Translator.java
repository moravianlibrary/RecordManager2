package cz.mzk.recordmanager.server.service;


import cz.mzk.recordmanager.api.model.ContactPersonDto;
import cz.mzk.recordmanager.api.model.ImportConfigurationDto;
import cz.mzk.recordmanager.api.model.LibraryDto;
import cz.mzk.recordmanager.api.model.OaiHarvestConfigurationDto;
import cz.mzk.recordmanager.server.model.ContactPerson;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.model.Library;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;

public class Translator {

	public LibraryDto translate(Library library) {
		LibraryDto libraryDto = new LibraryDto();
		libraryDto.setId(library.getId());
		libraryDto.setCity(library.getCity());
		libraryDto.setName(library.getName());
		libraryDto.setCatalogUrl(library.getCatalogUrl());
		libraryDto.setUrl(library.getUrl());
		return libraryDto;
	}

	public ContactPersonDto translate(ContactPerson contactPerson) {
		ContactPersonDto contactPersonDto = new ContactPersonDto();
		contactPersonDto.setId(contactPerson.getId());
		contactPersonDto.setEmail(contactPerson.getEmail());
		contactPersonDto.setName(contactPerson.getName());
		contactPersonDto.setPhone(contactPerson.getPhone());
		return contactPersonDto;
	}

	public Library translate(LibraryDto libraryDto) {
		Library library = new Library();
		library.setId(libraryDto.getId());
		library.setUrl(libraryDto.getUrl());
		library.setCity(libraryDto.getCity());
		library.setName(libraryDto.getName());
		library.setCatalogUrl(library.getCatalogUrl());
		return library;
	}

	public OaiHarvestConfigurationDto translate(OAIHarvestConfiguration oaiHarvestConfiguration) {
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

	public ContactPerson translate(ContactPersonDto contactPersonDto){
		ContactPerson contactPerson = new ContactPerson();
		contactPerson.setId(contactPersonDto.getId());
		contactPerson.setPhone(contactPersonDto.getPhone());
		contactPerson.setEmail(contactPersonDto.getPhone());
		contactPerson.setName(contactPersonDto.getName());
		return contactPerson;
	}
	public ImportConfigurationDto translate(ImportConfiguration configuration){
		ImportConfigurationDto importConfigurationDto = new ImportConfigurationDto();
		importConfigurationDto.setId(configuration.getId());
		importConfigurationDto.setIdPrefix(configuration.getIdPrefix());
		importConfigurationDto.setLibrary(translate(configuration.getLibrary()));
		return importConfigurationDto;
	}

}
