import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {LoginComponent} from "./login.component";
import {LoginService} from "./login.service";
import {loginRouting} from "./login.routing";
import {AuthGuard} from "./auth.guard";
import {FormsModule} from "@angular/forms";

@NgModule({
  imports: [
    CommonModule,
    loginRouting,
    FormsModule
  ],
  providers: [LoginService, AuthGuard],
  declarations: [LoginComponent]
})
export class LoginModule { }
