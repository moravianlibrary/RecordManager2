import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {StatisticsService} from "./statistics.service";
import {statisticsRouting} from "./statistics.routing";
import {StatisticsComponent} from "./statistics.component";
import {IndexModule} from "../shared/index";
import {StatisticDetailComponent} from "./statistic-detail/statistic-detail.component";

@NgModule({
  imports: [
    CommonModule,
    IndexModule,
    statisticsRouting
  ],
  providers: [StatisticsService],
  declarations: [StatisticsComponent, StatisticDetailComponent]
})
export class StatisticsModule { }
