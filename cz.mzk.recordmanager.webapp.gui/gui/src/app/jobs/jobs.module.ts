import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {jobsRouting} from "./jobs.routing";
import {JobsComponent} from "./jobs.component";
import {JobRunnerComponent} from "./job-runner/job-runner.component";
import {FormsModule} from "@angular/forms";
import {JobsService} from "./jobs.service";
import {AutocompletePipe} from "../pipes/autocomplete.pipe";

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    jobsRouting
  ],
  providers: [JobsService],
  declarations: [JobsComponent, JobRunnerComponent, AutocompletePipe]
})
export class JobsModule { }
