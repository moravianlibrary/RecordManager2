import {Component, OnInit} from '@angular/core';
import './rxjs-operators';
import {Router} from "@angular/router";
import {LoginService} from "./login/login.service";
import {SERVER} from "./server";
import {ADMIN} from "./roles";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {


  constructor(private router: Router, private loginService: LoginService){}

	ngOnInit(): void {
		this.storeIdentity();
	}

	storeIdentity(): void{
		this.loginService.storeIdentity().subscribe();
	}

	logout(): void{
  	    this.loginService.logout();
	}


}
