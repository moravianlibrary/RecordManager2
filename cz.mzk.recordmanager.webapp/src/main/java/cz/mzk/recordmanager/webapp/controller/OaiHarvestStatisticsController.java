package cz.mzk.recordmanager.webapp.controller;

import cz.mzk.recordmanager.api.model.IdDto;
import cz.mzk.recordmanager.api.model.batch.OaiHarvestJobStatisticsDto;
import cz.mzk.recordmanager.api.service.OaiHarvestStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/oaiHarvestStats")
public class OaiHarvestStatisticsController {
	@Autowired
	private OaiHarvestStatisticsService oaiHarvestStatisticsService;

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public List<OaiHarvestJobStatisticsDto> getStatistics() {
		return oaiHarvestStatisticsService.getHarvestJobStatistics();
	}

	@RequestMapping(method = RequestMethod.GET, value = "{configId}")
	@ResponseBody
	public IdDto getLibraryId(@PathVariable Long configId) {
		return oaiHarvestStatisticsService.getLibraryId(configId);
	}

}
