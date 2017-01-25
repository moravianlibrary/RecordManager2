package cz.mzk.recordmanager.webapp.controller;


import cz.mzk.recordmanager.api.model.PeriodDto;
import cz.mzk.recordmanager.api.model.statistics.*;
import cz.mzk.recordmanager.api.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;


import java.util.Date;
import java.util.List;


@RestController
@RequestMapping(value = "/statistics")
public class JobsStatisticsController {

    @Autowired
    private StatisticsService statisticsService;


    @RequestMapping(method = RequestMethod.GET, value = "oaiHarvestStatistics/{offset}")
    @ResponseBody
    public List<OaiHarvestJobStatisticsDto> getOaiHarvestStatistics(@PathVariable Integer offset) {
        return statisticsService.getOaiHarvestJobStats(offset);
    }

	@RequestMapping(method = RequestMethod.POST, value = "oaiHarvestStatistics/inPeriods")
	@ResponseBody
	public List<OaiHarvestJobStatisticsDto> getOaiHarvestStatisticsInPeriods(@RequestBody List<PeriodDto> periods){

    	return statisticsService.getOaiHarvestStatisticsInPeriods(periods.get(0), periods.get(1));

	}


    @RequestMapping(method = RequestMethod.POST, value = "actuals")
	@ResponseBody
	public List<ActualStatisticsDto> getActualStatistics(@RequestParam("startDate") Date startDate){
	return statisticsService.getActualStatisticsForThePeriod(startDate);
    }


    @RequestMapping(method = RequestMethod.GET, value = "indexAllRecordsStatistics/{offset}")
	@ResponseBody
	public List<IndexAllRecordsJobStatisticsDto> getIndexAllRecordsStatistics(@PathVariable Integer offset){
		return statisticsService.getIndexAllRecordsStatistics(offset);
    }

    @RequestMapping(method = RequestMethod.POST, value = "indexAllRecordsStatistics/inPeriods")
	@ResponseBody
	public List<IndexAllRecordsJobStatisticsDto> getIndexAllRecordsStatisticsInPeriods(@RequestBody List<PeriodDto> periods){
		return statisticsService.getIndexAllRecordsStatisticsInPeriods(periods.get(0), periods.get(1));
    }

    @RequestMapping(method = RequestMethod.GET, value = "dedupRecordsStatistics/{offset}")
	@ResponseBody
	public List<DedupRecordsDto> getDedupRecordsStatistics(@PathVariable Integer offset){
		return statisticsService.getDedupRecordsStatistics(offset);
    }

	@RequestMapping(method = RequestMethod.POST, value = "dedupRecordsStatistics/inPeriods")
	@ResponseBody
	public List<DedupRecordsDto> getDedupRecordsStatisticsInPeriods(@RequestBody PeriodDto period){
		return statisticsService.getDedupRecordsStatisticsInPeriods(period);
	}

	@RequestMapping(method = RequestMethod.GET, value = "details/{jobExecutionId}")
	@ResponseBody
	public StatisticDetailsDto getDetails(@PathVariable Long jobExecutionId){
		return statisticsService.getDetails(jobExecutionId);
	}

}
