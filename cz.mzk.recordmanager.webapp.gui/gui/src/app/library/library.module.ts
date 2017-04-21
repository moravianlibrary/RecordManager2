import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {IndexModule} from "../shared/index";
import {FormsModule} from "@angular/forms";
import {LibrariesComponent} from "./libraries/libraries.component";
import {LibraryComponent} from "./library.component";
import {LibraryRoutingModule} from "./library-routing.module";
import {LibrariesService} from "./libraries/libraries.service";
import {LibraryDetailComponent} from "./library-detail/library-detail.component";
import {LibraryDetailService} from "./library-detail/library-detail.service";



@NgModule({
  imports: [
      CommonModule,
      LibraryRoutingModule,
      IndexModule,
      FormsModule
  ],
  providers: [LibrariesService, LibraryDetailService],
  declarations: [LibrariesComponent, LibraryComponent, LibraryDetailComponent]
})
export class LibraryModule { }
