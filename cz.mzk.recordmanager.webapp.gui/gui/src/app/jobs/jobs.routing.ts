import {JobsComponent} from "./jobs.component";
import {ModuleWithProviders} from "@angular/core";
import {RouterModule} from "@angular/router";
const  jobsRoutes = [
  {
    path: 'jobs',
    component: JobsComponent
  }
];

export const jobsRouting : ModuleWithProviders = RouterModule.forChild(jobsRoutes);
