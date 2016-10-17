package cz.mzk.recordmanager.webapp.controller;

import java.util.List;

import cz.mzk.recordmanager.api.model.LibraryDetailDto;
import cz.mzk.recordmanager.api.model.OaiHarvestConfigurationDto;
import cz.mzk.recordmanager.server.model.Library;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import cz.mzk.recordmanager.api.model.LibraryDto;
import cz.mzk.recordmanager.api.service.LibraryService;

@RestController
@RequestMapping( value = "/library" )
public class LibraryController {

	@Autowired
	private LibraryService libraryService;

	@RequestMapping(method=RequestMethod.GET)
	@ResponseBody
	public List<LibraryDto> library() {
		return libraryService.getLibraries();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{libraryId}")
	@ResponseBody
	public LibraryDetailDto libraryDetails(@PathVariable Long libraryId)
	{
		return libraryService.getDetail(libraryId);
	}

	@RequestMapping(method = RequestMethod.PUT)
	@ResponseBody
	public LibraryDto createLibrary(@RequestBody LibraryDto libraryDto)
	{
		return libraryService.updateOrCreateLibrary(libraryDto);
	}


	@RequestMapping(method = RequestMethod.POST, value = "/{libraryId}")
	@ResponseBody
	public LibraryDto updateLibrary(@RequestBody LibraryDto library, @PathVariable Long libraryId) {
		library.setId(libraryId);
		return libraryService.updateOrCreateLibrary(library);
	}


	@RequestMapping(method = RequestMethod.PUT, value = "/{libraryId}/configuration")
	@ResponseBody
	public void createHarvestConfiguration(@RequestBody OaiHarvestConfigurationDto configurationDto, @PathVariable Long libraryId) {
		libraryService.updateOrCreateConfig(configurationDto, libraryId);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{libraryId}/configuration/{configId}")
	@ResponseBody
	public void deleteConfiguration(@RequestBody OaiHarvestConfigurationDto configurationDto, @PathVariable Long libraryId, @PathVariable Long configId) {
		configurationDto.setId(configId);
		libraryService.updateOrCreateConfig(configurationDto, libraryId);
	}
}
