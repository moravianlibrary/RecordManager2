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
import {MultiSelectModule} from "../shared/multi-select/multi-select.module";
import {BatchJobsService} from "./batch-jobs.service";
import {DatetimePickerModule} from "../shared/datetime-picker/datetime-picker.module";

@NgModule({
  imports: [
    CommonModule,
    IndexModule,
    FormsModule,
    MultiSelectModule,
    DatetimePickerModule,
    statisticsRouting
  ],
  providers: [StatisticsService, SortControl, BatchJobsService],
  declarations: [StatisticsComponent,  StatisticDetailComponent, StatusFilterPipe, AfterDateFilterPipe, BeforeDateFilterPipe]
})
export class StatisticsModule { }
