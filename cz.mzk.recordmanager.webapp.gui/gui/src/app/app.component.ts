import { Component } from '@angular/core';
import './rxjs-operators';
import {Router} from "@angular/router";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {

  constructor(private router: Router){}

  logedInformation(): string{
  	if (localStorage.getItem('currentUser'))
  		return 'Logout';
  	return 'Login';
  }


}
