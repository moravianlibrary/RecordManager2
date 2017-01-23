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
import {FullHarvestComponent} from "./statistics/full-harvest-statistic/full-harvest.component";
import {ActualStatisticsComponent} from "./statistics/actual-statistic/actual-statistic.component";
import {StatisticsService} from "./statistics/statistics.service";
import {DatetimePickerModule} from "../shared/datetime-picker/datetime-picker.module";
import {IndexModule} from "../shared/index";
import {StatusFilterPipe} from "../pipes/status-filter.pipe";
import {SortControl} from "../shared/sort-control";
import {IndexAllRecordsComponent} from "./statistics/index-all-records-statistic/index-all-records.component";
import {DedupRecordsComponent} from "./statistics/dedup-records-statistics/dedup-records-statistics";

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
		ActualStatisticsComponent,
		StatusFilterPipe,
		IndexAllRecordsComponent,
		DedupRecordsComponent
	]
})
export class JobModule{}