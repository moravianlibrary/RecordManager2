import {Component, OnInit, ElementRef} from '@angular/core';
import {OaiHarvestJobStatistics} from "../model/oai-harvest-job-statistics";
import {StatisticsService} from "./statistics.service";
import {Field} from "../shared/field";
import {Style} from "../shared/style";
import {SortControl} from "../shared/sort-control";
import {Router} from "@angular/router";

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

  statuses: string[] = [];
  startDate: Date;
  endDate: Date;
  fromParam: Date;
  toParam: Date;


  onchange(event: any){
    console.log(event);
  }
  //
  value: any;
  //

  options: any = [];



  selected(event: any){
    this.statuses = [];
    event.forEach(item => {
      this.statuses.push(item.key);
    });
  }

  constructor(private router: Router, private statisticsService: StatisticsService, private sortControl: SortControl) { }

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

    this.options.push({key: "COMPLETED", value: "COMPLETED"});
    this.options.push({key: "FAILED", value: "FAILED"});
    this.options.push({key: "STARTED", value: "STARTED"});

    this.sortByMe("jobExecutionId");

    this.loading = true;

    this.getStatistics();
  }

  sortByMe(name: string){
    this.sortBy = this.sortControl.sortByMe(name, this.fields);
  }

  getArrow(name: string): boolean{
    return this.sortControl.getArrow(name, this.fields)== 'glyphicon-arrow-up' ? true: false;
  }
  getVisibility(name: string): boolean{
    return this.sortControl.getVisibility(name, this.fields) == 'visible' ? true: false;
  }

  routToConfig(configId: number){
    this.statisticsService.getLibraryWithConfiguration(configId).subscribe(id => {
      this.router.navigate(['/library', id.id]);
    });
  }

}
