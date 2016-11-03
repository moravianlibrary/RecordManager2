import {Component, OnInit, Input} from '@angular/core';
import {JobsService} from "../jobs.service";
import {ImportConfig} from "../../model/import-config";


@Component({
  selector: 'app-job-runner',
  templateUrl: './job-runner.component.html',
  styleUrls: ['./job-runner.component.css'],
})
export class JobRunnerComponent implements OnInit{


  private importConfigs: ImportConfig[] = [];

  @Input()
  whoAmI: string;

  selectedConfiguration: ImportConfig = new ImportConfig;


  constructor(private jobsService: JobsService) { }

  getConfigurations(){
    this.jobsService.getImportConfigurations().subscribe(configs => {
      this.importConfigs = configs;
    });
  }
  chooseConfiguration(id: number){
    this.importConfigs.filter((conf) => {
      if (conf.id == id){
        this.selectedConfiguration = new ImportConfig(conf);
      }
    });
  }

  runJob(){
    this.jobsService.runJob(this.whoAmI, this.selectedConfiguration.id);
  }
  ngOnInit() {
    this.getConfigurations();
  }

}
