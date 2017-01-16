import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { AppComponent } from './app.component';

import {StatisticsModule} from "./statistics/statistics.module";
import {LoginModule} from "./login/login.module";
import {LibraryModule} from "./library/library.module";
import {JobModule} from "./job/job.module";
import {AppRoutingModule} from "./app-routing.module";





@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
      BrowserModule,
      FormsModule,
      HttpModule,
      LibraryModule,
      StatisticsModule,
      LoginModule,
      JobModule,
      AppRoutingModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
