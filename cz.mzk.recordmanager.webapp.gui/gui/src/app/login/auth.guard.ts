import {Injectable} from "@angular/core";
import {ActivatedRouteSnapshot, CanActivate, RouterStateSnapshot} from "@angular/router";
import {Observable} from "rxjs/Observable";
import {ADMIN} from "../roles";


@Injectable()
export class AuthGuard implements CanActivate{
	canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot):
		Observable<boolean>
		| Promise<boolean>
		| boolean {
		return JSON.parse(localStorage.getItem("currentUser")).roles.indexOf(ADMIN) !== -1;
	}

}