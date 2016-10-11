import { Component, OnInit } from '@angular/core';
import {LibrariesService} from "./libraries.service";
import {Library} from "./model/library";
import {Http, Response} from "@angular/http";
import {Router} from "@angular/router";
import {$} from "protractor/globals";

@Component({
  selector: 'app-libraries',
  templateUrl: './libraries.component.html',
  styleUrls: ['./libraries.component.css']
})
export class LibrariesComponent implements OnInit {

  libraries: Library[];
  loading: boolean;
  constructor(private librariesService: LibrariesService, private router: Router)
  {}

  getLibraries()
  {
    this.librariesService.getLibraries().subscribe(
      libraries => {
        this.libraries = libraries;
        this.loading = false;
      }
    );
  }


  ngOnInit() {
    this.loading = true;
    this.getLibraries();
  }

}
