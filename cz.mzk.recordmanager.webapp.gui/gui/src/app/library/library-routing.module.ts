import {Routes, RouterModule} from "@angular/router";
import {NgModule} from "@angular/core";
import {LibraryComponent} from "./library.component";
import {LibrariesComponent} from "./libraries/libraries.component";
import {LibraryDetailComponent} from "./library-detail/library-detail.component";
import {AuthGuard} from "../login/auth.guard";


const libraryRoutes: Routes = [
	{
		path: 'library',
		component: LibraryComponent,
		children:[
			{
				path: '',
				children:[
					{
						path: '',
						component: LibrariesComponent,
					},
					{
						path: ':id',
						component: LibraryDetailComponent
					}
				]
			}
		]
	}
];

@NgModule({
	imports: [
		RouterModule.forChild(libraryRoutes)
	],
	exports: [
		RouterModule
	]
})
export class LibraryRoutingModule{}