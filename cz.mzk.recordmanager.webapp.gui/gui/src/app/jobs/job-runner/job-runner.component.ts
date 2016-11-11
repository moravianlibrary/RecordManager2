import {Component, OnInit, Input, style} from '@angular/core';
import {JobsService} from "../jobs.service";
import {ImportConfig} from "../../model/import-config";


@Component({
  selector: 'app-job-runner',
  templateUrl: './job-runner.component.html',
  styleUrls: ['./job-runner.component.css'],
})
export class JobRunnerComponent implements OnInit{


  private importConfigs: ImportConfig[] = [];

  selectedConfigs: number[] = [];

  @Input()
  whoAmI: string;

  @Input()
  needId: boolean;


  constructor(private jobsService: JobsService) { }

  getConfigurations(){
    this.jobsService.getImportConfigurations().subscribe(configs => {
      this.importConfigs = configs;
    });
  }


  runJob(){
    this.jobsService.runJob(this.whoAmI, this.selectedConfigs);

  }


  ngOnInit() {
    this.getConfigurations();
  }

}
