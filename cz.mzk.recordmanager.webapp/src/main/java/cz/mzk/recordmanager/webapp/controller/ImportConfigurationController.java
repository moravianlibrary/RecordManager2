package cz.mzk.recordmanager.webapp.controller;

import cz.mzk.recordmanager.api.model.configurations.ImportConfigurationDto;
import cz.mzk.recordmanager.api.service.ImportConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping(value = "/importConfiguration")
public class ImportConfigurationController {

	@Autowired
	private ImportConfigurationService importConfigurationService;

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public List<ImportConfigurationDto> getAllConfigs() {
		return importConfigurationService.getConfigurations();
	}

}

