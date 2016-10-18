/**
 * Component represents view of library. How does library looks like.
 */
import { Component, OnInit } from '@angular/core';

import {LibraryDetail} from "../../model/library-detail";
import {LibrariesService} from "../libraries.service";
import {ActivatedRoute, Params} from "@angular/router";
import {Library} from "../../model/library";
import {OaiHarvestConfiguration} from "../../model/oai-harvest-configuration";


@Component({
	selector: 'app-library',
	templateUrl: './library.component.html',
	styleUrls: ['./library.component.css']
})
export class LibraryComponent implements OnInit{

	libraryDetail: LibraryDetail;
	library: Library = new Library;
	selected: OaiHarvestConfiguration = new OaiHarvestConfiguration;

	constructor(private librariesService: LibrariesService, private route: ActivatedRoute){
	}

	getLibraryDetails(libraryId: number)
	{
		this.librariesService.getLibraryDetails(libraryId)
			.subscribe(libraryDetail => {
			  this.libraryDetail = new LibraryDetail;
				this.libraryDetail = libraryDetail;

				this.library = new Library({
					id: this.libraryDetail.id,
					name: this.libraryDetail.name,
					url: this.libraryDetail.url,
					catalogUrl: this.libraryDetail.catalogUrl,
					city: this.libraryDetail.city
				});
			});

	}

	updateLibrary(): void
	{
		this.librariesService.updateLibrary(this.library)
      .subscribe(library => this.library = library);
	}


	ngOnInit(): void
	{
		this.route.params.forEach((params: Params) => {
			let id = +params['id'];
			this.getLibraryDetails(id);
		});
	}

	selectConfiguration(id: number)
	{
		this.selected = this.libraryDetail.oaiHarvestConfigurations.filter(config => config.id == id)[0];
    console.log(this.selected.id);
    console.log(this.selected.harvestJobName);
	}

	updateConfiguration(id: number)
	{
		this.librariesService.updateConfiguration(this.selected, id)
      .subscribe(configuration => {
        this.libraryDetail.oaiHarvestConfigurations.filter(conf => conf.id == configuration.id)[0] = configuration;
        this.selected = configuration;
      });
	}

}
