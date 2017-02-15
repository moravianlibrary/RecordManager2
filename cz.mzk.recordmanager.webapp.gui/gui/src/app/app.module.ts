import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { AppComponent } from './app.component';


import {LoginModule} from "./login/login.module";
import {LibraryModule} from "./library/library.module";
import {JobModule} from "./job/job.module";
import {AppRoutingModule} from "./app-routing.module";
import {AboutModule} from "./about/about.module";





@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
      BrowserModule,
      FormsModule,
      HttpModule,
      LibraryModule,
      LoginModule,
      JobModule,
      AppRoutingModule,
      AboutModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
