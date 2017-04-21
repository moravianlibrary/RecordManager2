import {Component, OnInit} from "@angular/core";
import {Library} from "../../model/library";
import {Field} from "../../shared/field";
import {LibrariesService} from "./libraries.service";
import {SortControl} from "../../shared/sort-control";
import {Style} from "../../shared/style";
import {LoginService} from "../../login/login.service";
import {ADMIN} from "../../roles";

@Component({
	selector: 'app-libraries',
	templateUrl: './libraries.component.html',
	styleUrls: ['./libraries.component.css']
})

export class LibrariesComponent implements OnInit{
	libraries: Library[];
	loading: boolean;
	public newLibrary: Library = new Library();
	sortBy: string;

	fields: Field[] = [];



	constructor(private librariesService: LibrariesService, private sortControl: SortControl, private loginService: LoginService) {}


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

		this.getLibraries();
	}

	sortByMe(name: string) {
		this.sortBy = this.sortControl.sortByMe(name, this.fields);
	}

	getArrow(name: string): boolean{
		return this.sortControl.getArrow(name, this.fields) == 'glyphicon-arrow-up' ? true: false;
	}
	getVisibility(name: string): boolean{
		return this.sortControl.getVisibility(name, this.fields) == 'visible' ? true : false;
	}

	isAllowed(): boolean{
		return this.loginService.getRoles().indexOf(ADMIN) !== -1;
	}
}