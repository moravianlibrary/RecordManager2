package cz.mzk.recordmanager.webapp.controller;

import java.util.List;

import cz.mzk.recordmanager.api.model.*;
import cz.mzk.recordmanager.api.model.configurations.DownloadImportConfigurationDto;
import cz.mzk.recordmanager.api.model.configurations.KrameriusConfigurationDto;
import cz.mzk.recordmanager.api.model.configurations.OaiHarvestConfigurationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import cz.mzk.recordmanager.api.service.LibraryService;

@RestController
@RequestMapping(value = "/library")
public class LibraryController {

	@Autowired
	private LibraryService libraryService;

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public List<LibraryDto> library() {
		return libraryService.getLibraries();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{libraryId}")
	@ResponseBody
	public LibraryDetailDto libraryDetails(@PathVariable Long libraryId) {
		LibraryDetailDto detail = libraryService.getDetail(libraryId);
		if (detail == null) {
			return null;
		}
		return detail;
	}


	@RequestMapping(method = RequestMethod.DELETE, value = "{libraryId}")
	@ResponseBody
	public void removeLibrary(@PathVariable Long libraryId){
		libraryService.removeLibrary(libraryId);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/configuration/{configId}")
	@ResponseBody
	public void removeConfiguration(@PathVariable Long configId){
		libraryService.removeConfiguration(configId);
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public LibraryDto createLibrary(@RequestBody LibraryDto libraryDto) {
		return libraryService.updateOrCreateLibrary(libraryDto);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{libraryId}")
	@ResponseBody
	public void updateLibrary(@RequestBody LibraryDto library,
			@PathVariable Long libraryId) {
		library.setId(libraryId);
		libraryService.updateOrCreateLibrary(library);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/{libraryId}/configuration")
	@ResponseBody
	public OaiHarvestConfigurationDto createHarvestConfiguration(
			@RequestBody OaiHarvestConfigurationDto configurationDto,
			@PathVariable Long libraryId) {
		libraryService.updateOrCreateConfig(configurationDto, libraryId);
		return configurationDto;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{libraryId}/OAIHarvestConfiguration/{configId}")
	@ResponseBody
	public void updateOaiHarvestConfig(
			@RequestBody OaiHarvestConfigurationDto configurationDto,
			@PathVariable Long libraryId, @PathVariable Long configId) {
		configurationDto.setId(configId);
		libraryService.updateOrCreateConfig(configurationDto, libraryId);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{libraryId}/KrameriusConfiguration/{configId}")
	@ResponseBody
	public void updateKrameriusConfig(
			@RequestBody KrameriusConfigurationDto configurationDto,
			@PathVariable Long libraryId, @PathVariable Long configId) {
		configurationDto.setId(configId);
		libraryService.updateOrCreateConfig(configurationDto, libraryId);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{libraryId}/DownloadImportConfiguration/{configId}")
	@ResponseBody
	public void updateDownloadImportConfig(
			@RequestBody DownloadImportConfigurationDto configurationDto,
			@PathVariable Long libraryId, @PathVariable Long configId) {
		configurationDto.setId(configId);
		libraryService.updateOrCreateConfig(configurationDto, libraryId);
	}

}
