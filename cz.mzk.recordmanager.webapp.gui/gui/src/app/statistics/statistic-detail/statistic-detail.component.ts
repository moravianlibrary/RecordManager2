import { Component, OnInit } from '@angular/core';
import {BatchJobExecution} from "../../model/batch-job-execution";
import {StatisticsService} from "../statistics.service";
import {ActivatedRoute, Params} from "@angular/router";
import {BatchJobsService} from "../batch-jobs.service";

@Component({
  selector: 'app-statistic-detail',
  templateUrl: './statistic-detail.component.html',
  styleUrls: ['./statistic-detail.component.css']
})
export class StatisticDetailComponent implements OnInit {

  batchJobExecution: BatchJobExecution;

  loading: boolean;

  constructor(private statisticsService: StatisticsService, private batchJobService: BatchJobsService, private route: ActivatedRoute) { }

  getDetail(id: number){
    this.loading = true;
    this.batchJobService.getDetails(id).subscribe(detail => {
      this.batchJobExecution = detail;
      this.loading = false;
    });
  }
  ngOnInit() {
    this.route.params.forEach((params: Params) => {
      let id = +params['id'];
      this.getDetail(id);
    });
  }

  restartJob(){
    this.batchJobService.restart(this.batchJobExecution).subscribe(res => {
      this.getDetail(this.batchJobExecution.id);
    });
  }

}
