import {Routes, RouterModule} from "@angular/router";
import {JobRunnersComponent} from "./runners/job-runners.component";
import {NgModule} from "@angular/core";
import {JobComponent} from "./job.component";
import {StatisticsComponent} from "./statistics/statistics.component";
import {FullHarvestComponent} from "./statistics/FullHarvestStatistic/full-harvest.component";
import {ActualStatisticsComonent} from "./statistics/ActualStatistic/actual-statistic.component";
const jobRoutes: Routes = [
	{
		path: 'job',
		component: JobComponent,
		children: [
			{
				path: 'runners',
				component: JobRunnersComponent
			},
			{
				path: 'statistics',
				component: StatisticsComponent,
				children:[
					{
						path: '',
						redirectTo: 'actual-statistics',
						pathMatch: 'full'
					},
					{
						path: 'full-harvest',
						component: FullHarvestComponent
					},
					{
						path: 'actual-statistics',
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