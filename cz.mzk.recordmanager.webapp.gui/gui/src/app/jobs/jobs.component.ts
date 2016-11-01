import { Component, OnInit } from '@angular/core';
import {ImportConfig} from "../model/import-config";
import {JobsService} from "./jobs.service";

@Component({
  selector: 'app-jobs',
  templateUrl: './jobs.component.html',
  styleUrls: ['./jobs.component.css']
})
export class JobsComponent implements OnInit {

  constructor() { }


  ngOnInit() {
  }

}
