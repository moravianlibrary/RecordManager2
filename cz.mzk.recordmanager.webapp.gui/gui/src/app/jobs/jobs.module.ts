import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {jobsRouting} from "./jobs.routing";
import {JobsComponent} from "./jobs.component";

@NgModule({
  imports: [
    CommonModule,
    jobsRouting
  ],
  declarations: [JobsComponent]
})
export class JobsModule { }
