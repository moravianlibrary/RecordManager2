import {Routes, RouterModule} from "@angular/router";
import {JobRunnersComponent} from "./runners/job-runners.component";
import {NgModule} from "@angular/core";
import {JobComponent} from "./job.component";
const jobRoutes: Routes = [
	{
		path: 'job',
		component: JobComponent,
		children: [
			{
				path: 'runners',
				component: JobRunnersComponent
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