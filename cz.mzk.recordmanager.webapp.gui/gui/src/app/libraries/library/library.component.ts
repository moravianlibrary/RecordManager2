/**
 * Component represents view of library. How does library looks like.
 */
import { Component, OnInit } from '@angular/core';

import {LibraryDetail} from "../model/library-detail";
import {LibrariesService} from "../libraries.service";
import {Router, ActivatedRoute, Params} from "@angular/router";


@Component({
  selector: 'app-library',
  templateUrl: './library.component.html',
  styleUrls: ['./library.component.css']
})
export class LibraryComponent implements OnInit{

  libraryDetail: LibraryDetail;

  constructor(private librariesService: LibrariesService, private route: ActivatedRoute,){}

  getLibraryDetails(libraryId: number)
  {
    this.librariesService.getLibraryDetails(libraryId)
      .subscribe(libraryDetail => {
        this.libraryDetail = libraryDetail;
      });
  }

  ngOnInit(): void {
    this.route.params.forEach((params: Params) => {
      let id = +params['id'];
      this.getLibraryDetails(id);
    });

  }

}
