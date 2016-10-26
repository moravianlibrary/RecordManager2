import {Component, OnInit, ElementRef} from '@angular/core';
import {LibrariesService} from "./libraries.service";
import {Library} from "../model/library";
import {Router} from "@angular/router";
import {Field} from "../shared/Field";
import {Style} from "../shared/Style";
import {SortControl} from "../shared/SortControl";


@Component({
	selector: 'app-libraries',
	templateUrl: './libraries.component.html',
	styleUrls: ['./libraries.component.css']
})
export class LibrariesComponent implements OnInit {

	libraries: Library[];
	loading: boolean;
	public newLibrary: Library = new Library();
  sortBy: string;

  fields: Field[] = [];



	constructor(private librariesService: LibrariesService, private router: Router) {}


	getLibraries() {
		this.loading = true;
		this.librariesService.getLibraries().subscribe(
			libraries => {
				this.libraries = libraries;
				this.loading = false;
			}
		);
	}
	createLibrary() {
		this.librariesService.createLibrary(this.newLibrary).subscribe(library => this.libraries.push(library));
	}

	ngOnInit() {

    this.fields.push(new Field({'_name': 'id', '_style': new Style()}));

    this.fields.push(new Field({'_name': 'name', '_style': new Style()}));

    this.fields.push(new Field({'_name': 'city', '_style': new Style()}));

    this.fields.push(new Field({'_name': 'url', '_style': new Style()}));

    this.fields.push(new Field({'_name': 'catalogUrl', '_style': new Style()}));

    this.sortByMe("id");

    this.loading = true;

    this.getLibraries();
	}

	sortByMe(name: string) {
    this.sortBy = SortControl.sortByMe(name, this.fields);
  }

  getArrow(name: string): string{
    return SortControl.getArrow(name, this.fields);
  }
  getVisibility(name: string): string{
    return SortControl.getVisibility(name, this.fields);
  }
}
