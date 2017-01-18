import {Routes, RouterModule} from "@angular/router";
import {JobRunnersComponent} from "./runners/job-runners.component";
import {NgModule} from "@angular/core";
import {JobComponent} from "./job.component";
import {StatisticsComponent} from "./statistics/statistics.component";
import {FullHarvestComponent} from "./statistics/FullHarvestStatistic/full-harvest.component";
import {ActualStatisticsComonent} from "./statistics/ActualStatistic/actual-statistic.component";
import {AuthGuard} from "../login/auth.guard";
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