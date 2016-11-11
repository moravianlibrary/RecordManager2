import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { AppComponent } from './app.component';
import {LibrariesModule} from "./libraries/libraries.module";
import {routing} from "./app.routing";
import {CommonModule} from "@angular/common";
import {StatisticsModule} from "./statistics/statistics.module";
import {JobsModule} from "./jobs/jobs.module";

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    LibrariesModule,
    StatisticsModule,
    // JobsModule,
    routing
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
