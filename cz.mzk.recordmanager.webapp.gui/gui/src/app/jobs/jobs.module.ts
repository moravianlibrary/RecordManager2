import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {jobsRouting} from "./jobs.routing";
import {JobsComponent} from "./jobs.component";
import {JobRunnerComponent} from "./job-runner/job-runner.component";
import {FormsModule} from "@angular/forms";
import {JobsService} from "./jobs.service";
import {MultiSelectModule} from "../shared/multi-select/multi-select.module";


@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    MultiSelectModule,
    jobsRouting
  ],
  providers: [JobsService],
  declarations: [JobsComponent, JobRunnerComponent]
})
export class JobsModule { }
