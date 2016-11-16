import { Injectable } from '@angular/core';
import {Library} from "../model/library";
import {Http, Response, RequestOptions, RequestMethod, Headers} from "@angular/http";
import {Observable} from "rxjs";
import {LibraryDetail} from "../model/library-detail";
import {OaiHarvestConfiguration} from "../model/oai-harvest-configuration";
import {ContactPerson} from "../model/contact-person";
import {SERVER} from "../server";
import {Router} from "@angular/router";
import {stat} from "fs";
import {ErrorHolder} from "../shared/error-holder";
import {ImportConfig} from "../model/import-config";
import {KrameriusConfiguration} from "../model/kramerius-configuration";
import {DownloadImportConfiguration} from "../model/download-import-configuration";


@Injectable()
export class LibrariesService {

	constructor(private http: Http) {
	}

	getLibraries(): Observable<Library[]> {
		return this.http.get(SERVER + "/library")
			.map((res: Response) => res.json())
      .catch(this.handleError);
	}

	getLibraryDetails(libraryId: number): Observable<LibraryDetail> {
		return this.http.get(SERVER + "/library/" + libraryId)
			.map(this.processResponse)
      .catch(this.handleError);
	}


	updateLibrary(library: Library): Observable<void> {
		var headers = new Headers({"Content-Type": 'application/json'});
		let options = new RequestOptions({ headers: headers });
    console.log(JSON.stringify(library));
		return this.http.post(SERVER + "/library/" + library.id, JSON.stringify(library), options)
			.map((res: Response) => {return;});
	}
	createLibrary(library: Library): Observable<Library> {
		var headers = new Headers({"Content-Type": 'application/json'});
		let options = new RequestOptions({ headers: headers });


		return this.http.put(SERVER + "/library", JSON.stringify(library), options)
			.map((res: Response) =>  res.json());
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

  removeConfiguration(configId: number): Observable<void>{
	  return this.http.delete(SERVER + "/configuration/" + configId).map((res: Response) => {return;});
  }



  private handleError (error: Response) {
    let status = error.status;
    let message = error.statusText;

    return Observable.throw(new ErrorHolder({status: status, message: message}));
  }

  private processResponse(res: Response){
	  return new LibraryDetail(res.json());
  }

}
