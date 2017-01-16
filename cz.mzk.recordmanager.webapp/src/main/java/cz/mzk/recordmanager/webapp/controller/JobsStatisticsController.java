package cz.mzk.recordmanager.webapp.controller;


import cz.mzk.recordmanager.api.model.batch.OaiHarvesrJobStatisticsDto;
import cz.mzk.recordmanager.api.model.statistics.ActualStatisticsDto;
import cz.mzk.recordmanager.api.service.OaiHarvestStatisticsService;
import cz.mzk.recordmanager.api.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;


import java.time.Period;
import java.util.Date;
import java.util.List;


@RestController
@RequestMapping(value = "/statistics")
public class JobsStatisticsController {
//    @Autowired
//    private OaiHarvestStatisticsService oaiHarvestStatisticsService;

    @Autowired
    private StatisticsService statisticsService;


//    @RequestMapping(method = RequestMethod.GET, value = "offset/{offset}")
//    @ResponseBody
//    public List<OaiHarvesrJobStatisticsDto> getStatistics(@PathVariable Integer offset) {
//        return oaiHarvestStatisticsService.getHarvestJobStatistics(offset);
//    }

//    @RequestMapping(method = RequestMethod.GET, value = "{configId}")
//    @ResponseBody
//    public IdDto getLibraryId(@PathVariable Long configId){
//        return oaiHarvestStatisticsService.getLibraryId(configId);
//    }


    @RequestMapping(method = RequestMethod.POST, value = "actuals")
	@ResponseBody
	public List<ActualStatisticsDto> getActualStatistics(@RequestParam("startDate") Date startDate){
	return statisticsService.getActualStatisticsForThePeriod(startDate);
    }
}
