import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import {LoginService} from "./login.service";

import {FormsModule} from "@angular/forms";

@NgModule({
  imports: [
    CommonModule,
    FormsModule
  ],
  providers: [LoginService],
  declarations: []
})
export class LoginModule { }
