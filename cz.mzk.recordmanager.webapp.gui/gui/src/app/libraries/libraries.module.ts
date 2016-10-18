import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {librariesRouting} from "./libraries.routing";
import {LibrariesComponent} from "./libraries.component";
import {LibrariesService} from "./libraries.service";
import {LibraryComponent} from "./library/library.component";
import {FormsModule} from "@angular/forms";
import {OrderByPipe} from "../order-by.pipe";


@NgModule({
  imports: [
    CommonModule,
    librariesRouting,
    FormsModule
  ],
  providers: [LibrariesService],

  declarations: [LibrariesComponent, LibraryComponent, OrderByPipe]
})
export class LibrariesModule { }
