package cz.mzk.recordmanager.webapp.controller;

import cz.mzk.recordmanager.api.model.batch.OaiHarvestJobStatisticsDto;
import cz.mzk.recordmanager.api.service.OaiHarvestStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping(value = "/oaiHarvestStats")
public class OaiHarvestStatisticsController {
    @Autowired
    private OaiHarvestStatisticsService oaiHarvestStatisticsService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<OaiHarvestJobStatisticsDto> getStatistics()
    {
        return oaiHarvestStatisticsService.getHarvestJobStatistics();
    }
}
