import { Injectable } from '@angular/core';
import {Http, Headers, RequestOptions, Response} from "@angular/http";
import {Observable} from "rxjs";
import {SERVER} from "../server";



@Injectable()
export class LoginService {

  constructor(private http: Http) {
  }

	storeIdentity(): Observable<boolean>{
		return this.http.get(SERVER + "/login").map((response: Response) => {
			localStorage.setItem("currentUser", JSON.stringify(response.json()));
			return true;
		});
	}


  logout(): void{
    localStorage.removeItem('currentUser');
    window.location.href = SERVER.match('http(s)?:\/\/[^\/]*')[0] + '/Shibboleth.sso/Logout';
  }

  getRoles(): any{
  	return localStorage.getItem("currentUser") !== null ? JSON.parse(localStorage.getItem("currentUser")).roles : [];
  }

}
