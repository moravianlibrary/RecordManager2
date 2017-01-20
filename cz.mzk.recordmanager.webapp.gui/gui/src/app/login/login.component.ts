import { Component, OnInit } from '@angular/core';
import {Router} from "@angular/router";
import {LoginService} from "./login.service";

@Component({
  selector: 'app-login',
  templateUrl: 'login.component.html',
  styleUrls: ['login.component.css']
})
export class LoginComponent implements OnInit {

  login: string;
  password: string;

  constructor(private router: Router,
              private loginService: LoginService) { }

  ngOnInit() {
    this.loginService.logout();
  }

  executeLogin(){
    this.loginService.login(this.login, this.password)
		.subscribe(result => {
          if (result === true){
            this.router.navigate(['./']);
          }else {
            this.router.navigate(['/login']);
          }
        });
  }

}
