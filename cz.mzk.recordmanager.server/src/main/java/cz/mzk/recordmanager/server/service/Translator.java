package cz.mzk.recordmanager.server.service;

import cz.mzk.recordmanager.api.model.*;
import cz.mzk.recordmanager.api.model.configurations.DownloadImportConfigurationDto;
import cz.mzk.recordmanager.api.model.configurations.ImportConfigurationDto;
import cz.mzk.recordmanager.api.model.configurations.KrameriusConfigurationDto;
import cz.mzk.recordmanager.api.model.configurations.OaiHarvestConfigurationDto;
import cz.mzk.recordmanager.server.model.*;

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
		oaiHarvestConfigurationDto.setThisLibrary(oaiHarvestConfiguration.isLibrary());


		oaiHarvestConfigurationDto.setUrl(oaiHarvestConfiguration.getUrl());
		oaiHarvestConfigurationDto.setSet(oaiHarvestConfiguration.getSet());
		oaiHarvestConfigurationDto.setMetadataPrefix(oaiHarvestConfiguration.getMetadataPrefix());
		oaiHarvestConfigurationDto.setExtractIdRegex(oaiHarvestConfiguration.getRegex());
		oaiHarvestConfigurationDto.setHarvestJobName(oaiHarvestConfiguration.getHarvestJobName());

		oaiHarvestConfigurationDto.setConfigurationType(oaiHarvestConfiguration.getClass().getSimpleName());
		return oaiHarvestConfigurationDto;
	}

	public KrameriusConfigurationDto translate(KrameriusConfiguration krameriusConfiguration) {
		KrameriusConfigurationDto krameriusConfigurationDto = new KrameriusConfigurationDto();

		krameriusConfigurationDto.setId(krameriusConfiguration.getId());
		krameriusConfigurationDto.setIdPrefix(krameriusConfiguration.getIdPrefix());
		krameriusConfigurationDto.setUrl(krameriusConfiguration.getUrl());
		krameriusConfigurationDto.setUrlSolr(krameriusConfiguration.getUrlSolr());
		krameriusConfigurationDto.setQueryRows(krameriusConfiguration.getQueryRows());
		krameriusConfigurationDto.setMetadataStream(krameriusConfiguration.getMetadataStream());
		krameriusConfigurationDto.setAuthToken(krameriusConfiguration.getAuthToken());
		krameriusConfigurationDto.setDownloadPrivateFulltexts(krameriusConfiguration.isDownloadPrivateFulltexts());
		krameriusConfigurationDto.setFulltextHarvestType(krameriusConfiguration.getFulltextHarvestType());
		krameriusConfigurationDto.setHarvestJobName(krameriusConfiguration.getHarvestJobName());
		krameriusConfigurationDto.setContact(translate(krameriusConfiguration.getContact()));
		krameriusConfigurationDto.setThisLibrary(krameriusConfiguration.isLibrary());

		krameriusConfigurationDto.setConfigurationType(krameriusConfiguration.getClass().getSimpleName());
		return krameriusConfigurationDto;
	}

	public DownloadImportConfigurationDto translate(DownloadImportConfiguration downloadImportConfiguration) {
		DownloadImportConfigurationDto downloadImportConfigurationDto = new DownloadImportConfigurationDto();

		downloadImportConfigurationDto.setId(downloadImportConfiguration.getId());
		downloadImportConfigurationDto.setIdPrefix(downloadImportConfiguration.getIdPrefix());
		downloadImportConfigurationDto.setUrl(downloadImportConfiguration.getUrl());
		downloadImportConfigurationDto.setFormat(downloadImportConfiguration.getFormat());
		downloadImportConfigurationDto.setJobName(downloadImportConfiguration.getJobName());
		downloadImportConfigurationDto.setRegex(downloadImportConfiguration.getRegex());
		downloadImportConfigurationDto.setContact(translate(downloadImportConfiguration.getContact()));
		downloadImportConfigurationDto.setThisLibrary(downloadImportConfiguration.isLibrary());

		downloadImportConfigurationDto.setConfigurationType(downloadImportConfiguration.getClass().getSimpleName());
		return downloadImportConfigurationDto;
	}

	public ContactPerson translate(ContactPersonDto contactPersonDto) {
		ContactPerson contactPerson = new ContactPerson();
		contactPerson.setId(contactPersonDto.getId());
		contactPerson.setPhone(contactPersonDto.getPhone());
		contactPerson.setEmail(contactPersonDto.getEmail());
		contactPerson.setName(contactPersonDto.getName());
		return contactPerson;
	}
	public ImportConfigurationDto translate(ImportConfiguration configuration) {
		ImportConfigurationDto importConfigurationDto = new ImportConfigurationDto();
		importConfigurationDto.setId(configuration.getId());
		importConfigurationDto.setIdPrefix(configuration.getIdPrefix());
		importConfigurationDto.setLibrary(translate(configuration.getLibrary()));
		return importConfigurationDto;
	}

	public OAIHarvestConfiguration translate(OaiHarvestConfigurationDto conf) {
		OAIHarvestConfiguration configuration = new OAIHarvestConfiguration();

		configuration.setId(conf.getId());
		configuration.setContact(translate(conf.getContact()));
		configuration.setIdPrefix(conf.getIdPrefix());
		configuration.setBaseWeight(conf.getBaseWeight());
		configuration.setClusterIdEnabled(conf.isClusterIdEnabled());
		configuration.setFilteringEnabled(conf.isFilteringEnabled());
		configuration.setInterceptionEnabled(conf.isInterceptionEnabled());
		configuration.setLibrary(conf.isThisLibrary());
		configuration.setContact(translate(conf.getContact()));

		configuration.setUrl(conf.getUrl());
		configuration.setSet(conf.getSet());
		configuration.setMetadataPrefix(conf.getMetadataPrefix());
		configuration.setRegex(conf.getExtractIdRegex());
		configuration.setHarvestJobName(conf.getHarvestJobName());

		return configuration;
	}

	public KrameriusConfiguration translate(KrameriusConfigurationDto conf) {
		KrameriusConfiguration configuration = new KrameriusConfiguration();

		configuration.setId(conf.getId());
		configuration.setContact(translate(conf.getContact()));
		configuration.setIdPrefix(conf.getIdPrefix());
		configuration.setBaseWeight(conf.getBaseWeight());
		configuration.setClusterIdEnabled(conf.isClusterIdEnabled());
		configuration.setFilteringEnabled(conf.isFilteringEnabled());
		configuration.setInterceptionEnabled(conf.isInterceptionEnabled());
		configuration.setLibrary(conf.isThisLibrary());
		configuration.setContact(translate(conf.getContact()));

		configuration.setUrl(conf.getUrl());
		configuration.setUrlSolr(conf.getUrlSolr());
		configuration.setQueryRows(conf.getQueryRows());
		configuration.setMetadataStream(conf.getMetadataStream());
		configuration.setAuthToken(conf.getAuthToken());
		configuration.setDownloadPrivateFulltexts(conf.isDownloadPrivateFulltexts());
		configuration.setFulltextHarvestType(conf.getFulltextHarvestType());
		configuration.setHarvestJobName(conf.getHarvestJobName());


		return configuration;
	}

	public DownloadImportConfiguration translate(DownloadImportConfigurationDto conf) {
		DownloadImportConfiguration configuration = new DownloadImportConfiguration();

		configuration.setId(conf.getId());
		configuration.setContact(translate(conf.getContact()));
		configuration.setIdPrefix(conf.getIdPrefix());
		configuration.setBaseWeight(conf.getBaseWeight());
		configuration.setClusterIdEnabled(conf.isClusterIdEnabled());
		configuration.setFilteringEnabled(conf.isFilteringEnabled());
		configuration.setInterceptionEnabled(conf.isInterceptionEnabled());
		configuration.setLibrary(conf.isThisLibrary());
		configuration.setContact(translate(conf.getContact()));

		configuration.setUrl(conf.getUrl());
		configuration.setFormat(conf.getFormat());
		configuration.setJobName(conf.getJobName());
		configuration.setRegex(conf.getRegex());

		return configuration;
	}

}
