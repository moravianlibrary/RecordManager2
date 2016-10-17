import {Component, OnInit, ElementRef} from '@angular/core';
import {LibrariesService} from "./libraries.service";
import {Library} from "../model/library";
import {Router} from "@angular/router";

@Component({
	selector: 'app-libraries',
	templateUrl: './libraries.component.html',
	styleUrls: ['./libraries.component.css']
})
export class LibrariesComponent implements OnInit {

	libraries: Library[];
	loading: boolean;

	public newLibrary: Library = new Library();

	constructor(private librariesService: LibrariesService, private router: Router)
	{}


	getLibraries()
	{
		this.loading = true;
		this.librariesService.getLibraries().subscribe(
			libraries => {
				this.libraries = libraries;
				this.loading = false;
			}
		);
	}
	createLibrary()
	{
		console.log(this.newLibrary.name);
		this.librariesService.createLibrary(this.newLibrary).subscribe(library => this.libraries.push(library));
	}

	ngOnInit() {
		this.loading = true;
		this.getLibraries();
	}

}
