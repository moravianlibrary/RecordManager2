import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {AboutRoutingModule} from "./about-routing.module";
import {AboutComponent} from "./about.component";
import {AboutStatisticsComponent} from "./statistics/about.statistics";
import {AboutLibrariesComponent} from "./libraries/about.libraries";

@NgModule({
  imports: [
    CommonModule,
    AboutRoutingModule
  ],
  declarations: [
      AboutComponent,
	  AboutStatisticsComponent,
	  AboutLibrariesComponent
  ]
})
export class AboutModule { }
