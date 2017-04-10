import {Injectable} from "@angular/core";
import {Http, Response, Headers, RequestOptions} from "@angular/http";
import {Observable} from "rxjs";
import {Library} from "../../model/library";
import {SERVER} from "../../server";
import {ErrorHolder} from "../../shared/error-holder";

@Injectable()
export class LibrariesService{
	constructor(private http: Http) {
	}

	getLibraries(): Observable<Library[]> {
		return this.http.get(SERVER + "/library")
			.map((res: Response) => res.json())
			.catch(this.handleError);
	}

	createLibrary(library: Library): Observable<Library> {
		var headers = new Headers({"Content-Type": 'application/json'});
		let options = new RequestOptions({ headers: headers });

		return this.http.post(SERVER + "/library", JSON.stringify(library), options)
			.map((res: Response) =>  res.json());
	}

	private handleError (error: Response) {
		console.log(error);
		let status = error.status;
		let message = error.statusText;

		return Observable.throw(new ErrorHolder({status: status, message: message}));
	}
}