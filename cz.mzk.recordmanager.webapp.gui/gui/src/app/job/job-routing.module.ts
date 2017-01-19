import {Routes, RouterModule} from "@angular/router";
import {JobRunnersComponent} from "./runners/job-runners.component";
import {NgModule} from "@angular/core";
import {JobComponent} from "./job.component";
import {StatisticsComponent} from "./statistics/statistics.component";
import {FullHarvestComponent} from "./statistics/full-harvest-statistic/full-harvest.component";
import {ActualStatisticsComonent} from "./statistics/actual-statistic/actual-statistic.component";
import {AuthGuard} from "../login/auth.guard";
import {IndexAllRecordsComponent} from "./statistics/index-all-records-statistic/index-all-records.component";
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
						component: ActualStatisticsComonent
					},
					{
						path: 'index-all-records',
						component: IndexAllRecordsComponent
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