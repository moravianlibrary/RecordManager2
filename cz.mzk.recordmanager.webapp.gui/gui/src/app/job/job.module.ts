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

@NgModule({
	imports: [
		CommonModule,
		JobRoutingModule,
		MultiSelectModule
	],
	providers: [JobRunnersService, StatisticsService],
	declarations: [
		JobComponent,
		JobRunnersComponent,
		SingleJobRunnerComponent,
		ImportRecordsJobComponent,
		StatisticsComponent,
		FullHarvestComponent,
		ActualStatisticsComonent
	]
})
export class JobModule{}