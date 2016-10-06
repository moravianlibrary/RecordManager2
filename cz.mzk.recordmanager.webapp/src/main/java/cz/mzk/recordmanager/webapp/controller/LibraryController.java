package cz.mzk.recordmanager.webapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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

}
