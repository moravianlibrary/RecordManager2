import { Injectable } from '@angular/core';
import {Library} from "../model/library";
import {Http, Response, RequestOptions, RequestMethod, Headers} from "@angular/http";
import {Observable} from "rxjs";
import {LibraryDetail} from "../model/library-detail";
import {OaiHarvestConfiguration} from "../model/oai-harvest-configuration";
import {ContactPerson} from "../model/contact-person";
import {SERVER} from "../server";


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
						id: res.json().oaiHarvestConfigurations[i].contact.id,
						name: res.json().oaiHarvestConfigurations[i].contact.name,
						email: res.json().oaiHarvestConfigurations[i].contact.email,
						phone: res.json().oaiHarvestConfigurations[i].contact.phone
					});

					let config: OaiHarvestConfiguration = new OaiHarvestConfiguration({
						id: res.json().oaiHarvestConfigurations[i].id,
						url: res.json().oaiHarvestConfigurations[i].url,
						set: res.json().oaiHarvestConfigurations[i].set,
						metadataPrefix: res.json().oaiHarvestConfigurations[i].metadataPrefix,
						contact: person,
						idPrefix: res.json().oaiHarvestConfigurations[i].idPrefix,
						baseWeight: res.json().oaiHarvestConfigurations[i].baseWeight,
						clusterIdEnabled: res.json().oaiHarvestConfigurations[i].clusterIdEnabled,
						filteringEnabled: res.json().oaiHarvestConfigurations[i].filteringEnabled,
						interceptionEnabled: res.json().oaiHarvestConfigurations[i].interceptionEnabled,
						extractIdRegex: res.json().oaiHarvestConfigurations[i].extractIdRegex,
						harvestJobName: res.json().oaiHarvestConfigurations[i].harvestJobName,
						library: res.json().oaiHarvestConfigurations[i].library
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


	updateLibrary(library: Library)
	{
		var headers = new Headers({"Content-Type": 'application/json'});
		let options = new RequestOptions({ headers: headers });
		console.log(JSON.stringify(library) + " " + SERVER + "/library/" + library.id + "Updated");

		this.http.post(SERVER + "/library/" + library.id, JSON.stringify(library), options)
			.subscribe();
	}
	createLibrary(library: Library): Observable<Library>
	{
		var headers = new Headers({"Content-Type": 'application/json'});
		let options = new RequestOptions({ headers: headers });
		console.log(JSON.stringify(library) + " " + SERVER + "/library");

		return this.http.put(SERVER + "/library", JSON.stringify(library), options)
			.map((res: Response) =>  res.json());
	}

	updateConfiguration(config: OaiHarvestConfiguration, libraryId: number)
	{
		var headers = new Headers({"Content-Type": 'application/json'});
		let options = new RequestOptions({ headers: headers });
		console.log(JSON.stringify(config) + " " + SERVER + "/library/" + libraryId + "Updated");

		this.http.post(SERVER + "/library/" + libraryId + "/configuration/" + config.id, JSON.stringify(config), options)
      .subscribe();
	}


}
