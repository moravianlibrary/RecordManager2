import { Component, OnInit } from '@angular/core';

import {LibraryDetail} from "../../model/library-detail";
import {LibrariesService} from "../libraries.service";
import {ActivatedRoute, Params, Router} from "@angular/router";
import {Library} from "../../model/library";



@Component({
	selector: 'app-library',
	templateUrl: './library.component.html',
	styleUrls: ['./library.component.css']
})
export class LibraryComponent implements OnInit{

	libraryDetail: LibraryDetail;
	library: Library = new Library;
	selected: any;
  loading: boolean;

	constructor(private librariesService: LibrariesService, private route: ActivatedRoute, private router: Router){
	}

	getLibraryDetails(libraryId: number) {
    this.loading = true;
		this.librariesService.getLibraryDetails(libraryId)
			.subscribe(libraryDetail => {
          this.libraryDetail = new LibraryDetail;
          this.libraryDetail = libraryDetail;

          this.library = new Library(libraryDetail);

          if (libraryDetail.oaiHarvestConfigurations.length > 0) {
            this.selected = libraryDetail.oaiHarvestConfigurations[0];
          }

          this.loading = false;
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
      .subscribe(res => {
        this.getLibraryDetails(this.library.id);
      });
	}


	ngOnInit(): void {
		this.route.params.forEach((params: Params) => {
			let id = +params['id'];
			this.getLibraryDetails(id);
		});
	}


	updateConfiguration(id: number, configurationType: string) {
		this.librariesService.updateConfiguration(this.selected, id, configurationType)
      .subscribe(res => {
        this.getLibraryDetails(this.library.id);
      });
	}

	removeConfiguration(confId: number){
		if (confirm("Are You sure You want to remove this configuration?")){
			this.librariesService.removeConfiguration(confId).subscribe(res => {
				this.getLibraryDetails(this.library.id);
			});
		}
  }

	removeLibrary(){
		if (confirm("Are You sure You want to remove this library?")) {
			this.librariesService.removeLibrary(this.library.id).subscribe(res => {
				this.router.navigate(['..']);
			});
		}

  }

	changeConfig(field: any, key: string){
	  this.selected[key] = field.value;
  }

  changeContact(field: any, key: string){
    this.selected.contact[key] = field.value;
  }

}
