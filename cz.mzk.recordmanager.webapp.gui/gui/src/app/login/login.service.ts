import { Injectable } from '@angular/core';
import {Http, Headers, RequestOptions, Response} from "@angular/http";
import {Observable} from "rxjs";
import {SERVER} from "../server";


@Injectable()
export class LoginService {

  public token: string;

  constructor(private http: Http) {
    var currentuser = JSON.parse(localStorage.getItem('currentUser'));
    this.token = currentuser && currentuser.token;
  }

  login(username: string, password: string): Observable<boolean>{
    var headers = new Headers({"Content-Type": 'application/json', 'Accept': 'application/json'});
    let options = new RequestOptions({ headers: headers });
    return this.http.post(SERVER + "/login", JSON.stringify({login: username, password: password}), options).map((response: Response) => {
      let token = response.json() && response.json().token;

      if (token){
        this.token = token;
        let login = response.json() && response.json().login;
        localStorage.setItem('currentUser', JSON.stringify({user: login, token: token}));
        return true;
      }else {
        return false;
      }
    });
  }

  logout(): void{
    this.token = null;
    localStorage.removeItem('currentUser');
  }


}
