import { Component, OnInit } from '@angular/core';
import {JobsService} from "../jobs.service";

@Component({
  selector: 'app-index-individual-records-to-solr-job-runner',
  templateUrl: './index-individual-records-to-solr-job-runner.component.html',
  styleUrls: ['./index-individual-records-to-solr-job-runner.component.css']
})
export class IndexIndividualRecordsToSolrJobRunnerComponent implements OnInit {

  recordIds: any[] = [];

  constructor(private jobsService: JobsService) { }

  addId(){

    if (this.recordIds.length > 0){
      if (this.recordIds[this.recordIds.length - 1].completeInstitutionId.length > 0)
        this.recordIds.push({completeInstitutionId: ""});
    }else {
      this.recordIds.push({completeInstitutionId: ""});
    }
  }
  runIndividualRecordsToSolrJob(){
    if(confirm("Are you sure you want to run this job?"))
      this.jobsService.runIndividualRecordsToSolrJob(this.recordIds);
  }
  removeRecordId(index: number){
    this.recordIds.splice(index, 1);
  }
  ngOnInit() {
  }

}
