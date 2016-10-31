import { Component, OnInit } from '@angular/core';
import {BatchJobExecution} from "../../model/batch-job-execution";
import {StatisticsService} from "../statistics.service";
import {ActivatedRoute, Params} from "@angular/router";

@Component({
  selector: 'app-statistic-detail',
  templateUrl: './statistic-detail.component.html',
  styleUrls: ['./statistic-detail.component.css']
})
export class StatisticDetailComponent implements OnInit {

  batchJobExecution: BatchJobExecution;

  constructor(private statisticsService: StatisticsService, private route: ActivatedRoute) { }

  getDetail(id: number){
    this.statisticsService.getDetails(id).subscribe(detail => {
      this.batchJobExecution = detail;
    });
  }
  ngOnInit() {
    this.route.params.forEach((params: Params) => {
      let id = +params['id'];
      this.getDetail(id);
    });
  }

}
