import { Injectable } from '@angular/core';
import {Library} from "./model/library";
import {Http, Response, RequestOptions, RequestMethod, Headers} from "@angular/http";
import {Observable} from "rxjs";
import {LibraryDetail} from "./model/library-detail";
import {OaiHarvestConfiguration} from "./model/oai-harvest-configuration";
import {ContactPerson} from "./model/contact-person";
import {SERVER} from "../server";
import {_throw} from "rxjs/observable/throw";

@Injectable()
export class LibrariesService {

  constructor(private http: Http)
  {
  }

  getLibraries(): Observable<Library[]>
  {
    return this.http.get(SERVER + "/library")
      .map((res: Response) => res.json());
  }

  getLibraryDetails(libraryId: number): Observable<LibraryDetail>
  {
    return this.http.get(SERVER + "/library/" + libraryId)
      .map((res: Response) => {

        let configs: OaiHarvestConfiguration[] = [];

        for (var i = 0; i < res.json().oaiHarvestConfigurations.length; ++i)
        {
          //Create contact person
          let person: ContactPerson = new ContactPerson({
            id: res.json().oaiHarvestConfigurations[i].contactPerson.id,
            name: res.json().oaiHarvestConfigurations[i].contactPerson.name,
            email: res.json().oaiHarvestConfigurations[i].contactPerson.email,
            phone: res.json().oaiHarvestConfigurations[i].contactPerson.phone
          });

          let config: OaiHarvestConfiguration = new OaiHarvestConfiguration({
            id: res.json().oaiHarvestConfigurations[i].id,
            url: res.json().oaiHarvestConfigurations[i].url,
            set: res.json().oaiHarvestConfigurations[i].set,
            metadataPrefix: res.json().oaiHarvestConfigurations[i].metadataPrefix,
            contactPerson: person
          });

          configs.push(config);
        }
        let detail: LibraryDetail = new LibraryDetail({
          id: res.json().id,
          name: res.json().name,
          url: res.json().url,
          catalogUrl: res.json().catalogUrl,
          city: res.json().city,
          oaiHarvestConfigurations: configs
        });

        return detail;
      });
  }

  createLibrary(library: Library)
  {
    var headers = new Headers({"Content-Type": 'application/json'});
    let options = new RequestOptions({ headers: headers });
    console.log(JSON.stringify(library) + " " + SERVER + "/library");

    this.http.put(SERVER + "/library", JSON.stringify(library), options)
      .subscribe();
  }

}
