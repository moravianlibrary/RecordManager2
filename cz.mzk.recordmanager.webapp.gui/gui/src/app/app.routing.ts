import {Routes, RouterModule} from "@angular/router";
import {ModuleWithProviders} from "@angular/core";


const appRoutes: Routes = [
  {
    path: '',
    redirectTo: 'library',
    pathMatch: 'full'
  },
];
export const routing: ModuleWithProviders = RouterModule.forRoot(appRoutes);
