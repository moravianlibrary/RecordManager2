import {Routes, RouterModule} from "@angular/router";
import {ModuleWithProviders} from "@angular/core";
import {AppComponent} from "./app.component";

const appRoutes: Routes = [
  {
    path: '',
    component: AppComponent
  }
];
export const routing: ModuleWithProviders = RouterModule.forRoot(appRoutes);
