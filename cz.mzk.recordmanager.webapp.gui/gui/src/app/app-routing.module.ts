import {Routes, RouterModule} from "@angular/router";
import {NgModule} from "@angular/core";
const appRoutes: Routes = [
	{
		path: '',
		redirectTo: 'library',
		pathMatch: 'full'
	},
];

@NgModule({
	imports: [
		RouterModule.forRoot(appRoutes)
	],
	exports: [
		RouterModule
	]
})
export class AppRoutingModule{}