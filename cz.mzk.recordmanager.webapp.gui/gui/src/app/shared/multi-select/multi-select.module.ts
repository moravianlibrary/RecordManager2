import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {MultiSelectComponent} from "./multi-select.component";
import {TypeaheadPipe} from "./typeahead.pipe";
import {FormsModule} from "@angular/forms";

@NgModule({
  imports: [
    CommonModule,
    FormsModule
  ],
  declarations: [MultiSelectComponent, TypeaheadPipe],
  exports: [MultiSelectComponent, TypeaheadPipe]
})
export class MultiSelectModule { }
