import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {librariesRouting} from "./libraries.routing";
import {LibrariesComponent} from "./libraries.component";
import {LibrariesService} from "./libraries.service";
import {LibraryComponent} from "./library/library.component";

@NgModule({
  imports: [
    CommonModule,
    librariesRouting
  ],
  providers: [LibrariesService],

  declarations: [LibrariesComponent, LibraryComponent]
})
export class LibrariesModule { }
