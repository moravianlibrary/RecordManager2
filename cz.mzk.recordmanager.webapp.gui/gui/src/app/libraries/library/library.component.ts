/**
 * Component represents view of library. How does library looks like.
 */
import { Component, OnInit } from '@angular/core';

import {LibraryDetail} from "../../model/library-detail";
import {LibrariesService} from "../libraries.service";
import {ActivatedRoute, Params, Router} from "@angular/router";
import {Library} from "../../model/library";
import {OaiHarvestConfiguration} from "../../model/oai-harvest-configuration";
import {ContactPerson} from "../../model/contact-person";


@Component({
	selector: 'app-library',
	templateUrl: './library.component.html',
	styleUrls: ['./library.component.css']
})
export class LibraryComponent implements OnInit{

	libraryDetail: LibraryDetail;
	library: Library = new Library;
	selected: OaiHarvestConfiguration = new OaiHarvestConfiguration;

	constructor(private librariesService: LibrariesService, private route: ActivatedRoute, private router: Router){
	}

	getLibraryDetails(libraryId: number) {
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

          if (libraryDetail.oaiHarvestConfigurations.length > 0) {
            this.selected = libraryDetail.oaiHarvestConfigurations[0];
          }
        },

			error => {
          this.router.navigate(['/error', {status: error.status, message: error.message}]);
      });

	}

  selectMe(id: number){
    this.selected = this.libraryDetail.oaiHarvestConfigurations.filter(conf => conf.id == id)[0];
  }

	updateLibrary(): void {
		this.librariesService.updateLibrary(this.library)
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

        if (libraryDetail.oaiHarvestConfigurations.length > 0) {
          this.selected = libraryDetail.oaiHarvestConfigurations[0];
        }
      });
	}


	ngOnInit(): void {
		this.route.params.forEach((params: Params) => {
			let id = +params['id'];
			this.getLibraryDetails(id);
		});
	}

	selectConfiguration(page: number) {
		this.selected = this.libraryDetail.oaiHarvestConfigurations[page];

	}

	updateConfiguration(id: number) {
		this.librariesService.updateConfiguration(this.selected, id)
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

        if (libraryDetail.oaiHarvestConfigurations.length > 0) {
          this.selected = libraryDetail.oaiHarvestConfigurations[0];
        }
      });
	}



}
