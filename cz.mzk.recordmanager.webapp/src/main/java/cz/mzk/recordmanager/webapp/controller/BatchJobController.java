package cz.mzk.recordmanager.webapp.controller;

import cz.mzk.recordmanager.api.model.batch.BatchJobExecutionDTO;
import cz.mzk.recordmanager.api.service.BatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by sergey on 10/11/16.
 */
@RestController
@RequestMapping(value = "/batches")
public class BatchJobController {
    @Autowired
    private BatchService batchService;


    @RequestMapping(method = RequestMethod.GET, value = "/running")
    @ResponseBody
    private List<BatchJobExecutionDTO> getRunningJobExecutions()
    {
        return batchService.getRunningJobExecutions();
    }
}
