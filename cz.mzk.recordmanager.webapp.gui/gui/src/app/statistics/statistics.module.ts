import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {StatisticsService} from "./statistics.service";
import {statisticsRouting} from "./statistics.routing";
import {StatisticsComponent} from "./statistics.component";
import {IndexModule} from "../shared/index";
import {StatisticDetailComponent} from "./statistic-detail/statistic-detail.component";
import {SortControl} from "../shared/sort-control";
import {FormsModule} from "@angular/forms";
import {StatusFilterPipe} from "../pipes/status-filter.pipe";
import {AfterDateFilterPipe} from "../pipes/after-date-filter.pipe";
import {BeforeDateFilterPipe} from "../pipes/before-date-filter.pipe";
import {SelectModule} from "angular2-select";


@NgModule({
  imports: [
    CommonModule,
    IndexModule,
    FormsModule,
    SelectModule,
    statisticsRouting
  ],
  providers: [StatisticsService, SortControl],
  declarations: [StatisticsComponent, StatisticDetailComponent, StatusFilterPipe, AfterDateFilterPipe, BeforeDateFilterPipe]
})
export class StatisticsModule { }
