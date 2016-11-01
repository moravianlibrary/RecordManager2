import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {librariesRouting} from "./libraries.routing";
import {LibrariesComponent} from "./libraries.component";
import {LibrariesService} from "./libraries.service";
import {LibraryComponent} from "./library/library.component";
import {FormsModule} from "@angular/forms";
import {IndexModule} from "../shared/index";
import {SortControl} from "../shared/SortControl";


@NgModule({
  imports: [
    CommonModule,
    IndexModule,
    librariesRouting,
    FormsModule
  ],
  providers: [LibrariesService, SortControl],

  declarations: [LibrariesComponent, LibraryComponent]
})
export class LibrariesModule { }
