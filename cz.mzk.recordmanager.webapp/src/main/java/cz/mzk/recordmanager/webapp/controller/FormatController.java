package cz.mzk.recordmanager.webapp.controller;

import cz.mzk.recordmanager.server.export.IOFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/format")
public class FormatController {

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	private List<String> getFormats(){
		return IOFormat.getStringifyFormats();
	}

}
