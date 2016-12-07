import {Component, OnInit, ViewChild} from '@angular/core';
import {JobsService} from "./jobs.service";


@Component({
  selector: 'app-jobs',
  templateUrl: './jobs.component.html',
  styleUrls: ['./jobs.component.css']
})
export class JobsComponent implements OnInit {

  constructor(private jobsService: JobsService) { }


  runNoParamsJob(name: string){
    if (confirm('Are ypu sure you want to run this job?')){
      this.jobsService.runNoParamsJob(name);
    }
  }

  ngOnInit() {
  }

}
