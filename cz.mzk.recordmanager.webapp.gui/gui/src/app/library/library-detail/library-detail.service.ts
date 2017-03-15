import {Injectable} from "@angular/core";
import {Http, Response, Headers, RequestOptions} from "@angular/http";
import {Observable} from "rxjs";
import {SERVER} from "../../server";
import {LibraryDetail} from "../../model/library-detail";
import {ErrorHolder} from "../../shared/error-holder";
import {ImportConfig} from "../../model/import-config";
import {Library} from "../../model/library";
@Injectable()
export class LibraryDetailService{
	constructor(private http: Http) {
	}

	getLibraryDetails(libraryId: number): Observable<LibraryDetail> {
		return this.http.get(SERVER + "/library/" + libraryId)
			.map(this.processResponse)
			.catch(this.handleError);
	}

	private handleError (error: Response) {
		let status = error.status;
		let message = error.statusText;

		return Observable.throw(new ErrorHolder({status: status, message: message}));
	}

	removeConfiguration(configId: number): Observable<void>{
		return this.http.delete(SERVER + "/configuration/" + configId).map((res: Response) => {return;});
	}

	updateLibrary(library: Library): Observable<void> {
		var headers = new Headers({"Content-Type": 'application/json'});
		let options = new RequestOptions({ headers: headers });
		return this.http.post(SERVER + "/library/" + library.id, JSON.stringify(library), options)
			.map((res: Response) => {return;});
	}

	updateConfiguration(config: ImportConfig, libraryId: number, configurationType: string): Observable<void> {
		var headers = new Headers({"Content-Type": 'application/json'});
		let options = new RequestOptions({ headers: headers });

		console.log(JSON.stringify(config));
		return this.http.post(SERVER + "/library/" + libraryId + "/" + configurationType + "/" + config.id, JSON.stringify(config), options)
			.map((res: Response) => {return;});
	}

	removeLibrary(libraryId: number): Observable<void>{
		return this.http.delete(SERVER + "/library/" + libraryId).map((res: Response) => {return;});
	}

	private processResponse(res: Response){
		return new LibraryDetail(res.json());
	}
}