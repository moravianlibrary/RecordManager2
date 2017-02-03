import {Routes, RouterModule} from "@angular/router";
import {JobRunnersComponent} from "./runners/job-runners.component";
import {NgModule} from "@angular/core";
import {JobComponent} from "./job.component";
import {StatisticsComponent} from "./statistics/statistics.component";
import {FullHarvestComponent} from "./statistics/full-harvest-statistic/full-harvest.component";
import {ActualStatisticsComponent} from "./statistics/actual-statistic/actual-statistic.component";
import {AuthGuard} from "../login/auth.guard";
import {IndexAllRecordsComponent} from "./statistics/index-all-records-statistic/index-all-records.component";
import {DedupRecordsComponent} from "./statistics/dedup-records-statistics/dedup-records-statistics";
import {DetailsComponent} from "./statistics/details/details.component";
import {DownloadImportConfComponent} from "./statistics/download-import-conf-statistics/download-import-conf-statistics";
const jobRoutes: Routes = [
	{
		path: 'job',
		component: JobComponent,
		children: [
			{
				path: 'runners',
				component: JobRunnersComponent,
				canActivate: [AuthGuard]
			},
			{
				path: 'statistics',
				component: StatisticsComponent,
				children:[
					{
						path: '',
						redirectTo: 'actual',
						pathMatch: 'full'
					},
					{
						path: 'full-harvest',
						component: FullHarvestComponent
					},
					{
						path: 'actual',
						component: ActualStatisticsComponent
					},
					{
						path: 'index-all-records',
						component: IndexAllRecordsComponent
					},
					{
						path: 'dedup-records',
						component: DedupRecordsComponent
					},
					{
						path: 'download-import-conf',
						component: DownloadImportConfComponent
					},
					{
						path: 'details/:jobExecId',
						component: DetailsComponent
					}
				]
			}
		]

	}
];


@NgModule({
	imports:[
		RouterModule.forChild(jobRoutes)
	],
	exports: [
		RouterModule
	]
})

export class JobRoutingModule{}