import {Component, OnInit, ElementRef} from '@angular/core';
import {LibrariesService} from "./libraries.service";
import {Library} from "../model/library";
import {Router} from "@angular/router";


class Style{
  get visibility(): string {
    return this._visibility;
  }

  set visibility(value: string) {
    this._visibility = value;
  }
  get arrow(): string {
    return this._arrow;
  }

  set arrow(value: string) {
    this._arrow = value;
  }

  switchArrow(){
    this._arrow = this._arrow == "sortArrowUp" ? "sortArrowDown" : "sortArrowUp";
  }

  switchVisibility(){
    this._visibility = this._visibility == "invisible" ? "visible" : "invisible";
  }

  constructor(obj?: any) {
    this._arrow       = obj && obj._arrow    ||  "sortArrowUp";
    this._visibility  = obj && obj._arrow    ||  "invisible";
  }

  private _arrow: string;
  private _visibility: string;
}

class Field{
  get style(): Style {
    return this._style;
  }

  set style(value: Style) {
    this._style = value;
  }
  get name(): string {
    return this._name;
  }

  set name(value: string) {
    this._name = value;
  }
  constructor(obj?: any) {
    this._name        = obj && obj._name    ||  "id";
    this._style       = obj && obj._style   ||  null;
  }
  private _name: string;
  private _style: Style;
}

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

	sortByMe(name: string)
  {
    //Make all arrows invisible
    this.fields.forEach(field => {
      if (field.style.visibility == 'visible'){
        field.style.switchVisibility();
      }
    });

    // //Make target arrow visible
    var me = this.fields.filter(field => field.name == name)[0];

    me.style.switchArrow();
    me.style.switchVisibility();

    this.sortBy = me.style.arrow == "sortArrowUp" ? "+" + name : "-" + name;
  }

  getArrow(name: string): string{
    return this.fields.filter(field => field.name == name)[0].style.arrow;
  }
  getVisibility(name: string): string{
    return this.fields.filter(field => field.name == name)[0].style.visibility;
  }
}
