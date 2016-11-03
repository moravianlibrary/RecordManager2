import {ErrorPageComponent} from "./error-page/error-page.component";
import {ModuleWithProviders} from "@angular/core";
import {RouterModule} from "@angular/router";
const errorsRoutes = [
  {
    path: 'error',
    component: ErrorPageComponent
  }
];

export const errorsRouting : ModuleWithProviders = RouterModule.forChild(errorsRoutes);
