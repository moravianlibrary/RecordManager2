
import {ModuleWithProviders} from "@angular/core";
import {RouterModule} from "@angular/router";
import {LoginComponent} from "./login.component";
const loginRotes = [
	{
		path:'login',
		component: LoginComponent
	}
];
export const  loginRouting : ModuleWithProviders = RouterModule.forChild(loginRotes);