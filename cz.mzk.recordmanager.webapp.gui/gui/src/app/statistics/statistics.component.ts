import { Component, OnInit } from '@angular/core';
import {OaiHarvestJobStatistics} from "../model/oai-harvest-job-statistics";
import {StatisticsService} from "./statistics.service";
import {Field} from "../shared/field";
import {Style} from "../shared/style";
import {SortControl} from "../shared/sort-control";

@Component({
  selector: 'app-statistics',
  templateUrl: './statistics.component.html',
  styleUrls: ['./statistics.component.css']
})
export class StatisticsComponent implements OnInit {

  statistics: OaiHarvestJobStatistics[];

  sortBy: string;
  fields: Field[] = [];
  loading: boolean;

  status: string;
  startDate: Date;
  endDate: Date;
  fromParam: Date;
  toParam: Date;


  constructor(private statisticsService: StatisticsService, private sortControl: SortControl) { }

  getStatistics(){
    this.statisticsService.getStatistics().subscribe(statistics => {
      this.statistics = statistics;
      this.loading = false;
    });
  }

  ngOnInit() {
    this.fields.push(new Field({'_name': 'jobExecutionId', '_style': new Style()}));

    this.fields.push(new Field({'_name': 'importConfId', '_style': new Style()}));

    this.fields.push(new Field({'_name': 'libraryName', '_style': new Style()}));

    this.fields.push(new Field({'_name': 'url', '_style': new Style()}));

    this.fields.push(new Field({'_name': 'startTime', '_style': new Style()}));

    this.fields.push(new Field({'_name': 'endTime', '_style': new Style()}));

    this.fields.push(new Field({'_name': 'status', '_style': new Style()}));

    this.fields.push(new Field({'_name': 'fromParam', '_style': new Style()}));

    this.fields.push(new Field({'_name': 'toParam', '_style': new Style()}));

    this.fields.push(new Field({'_name': 'noOfRecords', '_style': new Style()}));

    this.sortByMe("jobExecutionId");

    this.loading = true;

    this.getStatistics();
  }

  sortByMe(name: string){
    this.sortBy = this.sortControl.sortByMe(name, this.fields);
  }

  getArrow(name: string): string{
    return this.sortControl.getArrow(name, this.fields);
  }
  getVisibility(name: string): string{
    return this.sortControl.getVisibility(name, this.fields);
  }


}
