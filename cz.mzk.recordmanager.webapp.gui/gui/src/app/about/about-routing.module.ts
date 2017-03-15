import {Routes, RouterModule} from "@angular/router";
import {AboutComponent} from "./about.component";
import {NgModule} from "@angular/core";
const aboutRoutes: Routes =[
	{
		path: 'about',
		component: AboutComponent
	}
];

@NgModule({
	imports:[
		RouterModule.forChild(aboutRoutes)
	],
	exports: [
		RouterModule
	]
})

export class AboutRoutingModule{}