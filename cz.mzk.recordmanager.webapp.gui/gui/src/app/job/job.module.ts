import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {JobComponent} from "./job.component";
import { JobRoutingModule} from "./job-routing.module";
import {JobRunnersComponent} from "./runners/job-runners.component";
import {SingleJobRunnerComponent} from "./runners/single-job-runner/single-job-runner.component";
import {MultiSelectModule} from "../shared/multi-select/multi-select.module";
import {JobRunnersService} from "./runners/job-runners.service";
import {ImportRecordsJobComponent} from "./runners/import-record-job/import-records-job.component";
import {StatisticsComponent} from "./statistics/statistics.component";
import {FullHarvestComponent} from "./statistics/FullHarvestStatistic/full-harvest.component";
import {ActualStatisticsComonent} from "./statistics/ActualStatistic/actual-statistic.component";
import {StatisticsService} from "./statistics/statistics.service";
import {DatetimePickerModule} from "../shared/datetime-picker/datetime-picker.module";
import {IndexModule} from "../shared/index";
import {StatusFilterPipe} from "../pipes/status-filter.pipe";
import {AfterDateFilterPipe} from "../pipes/after-date-filter.pipe";
import {BeforeDateFilterPipe} from "../pipes/before-date-filter.pipe";
import {SortControl} from "../shared/sort-control";

@NgModule({
	imports: [
		CommonModule,
		JobRoutingModule,
		MultiSelectModule,
		DatetimePickerModule,
		IndexModule
	],
	providers: [JobRunnersService, StatisticsService, SortControl],
	declarations: [
		JobComponent,
		JobRunnersComponent,
		SingleJobRunnerComponent,
		ImportRecordsJobComponent,
		StatisticsComponent,
		FullHarvestComponent,
		ActualStatisticsComonent,
		StatusFilterPipe,
		AfterDateFilterPipe,
		BeforeDateFilterPipe
	]
})
export class JobModule{}