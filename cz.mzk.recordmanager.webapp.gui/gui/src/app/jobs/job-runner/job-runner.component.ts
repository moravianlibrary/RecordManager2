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

  selectedConfigs: number[] = [];

  @Input()
  whoAmI: string;

  @Input()
  needId: boolean;

  options: any[] = [];


  constructor(private jobsService: JobsService) {
  }

  getConfigurations(){
    this.jobsService.getImportConfigurations().subscribe(configs => {
      this.importConfigs = configs;

      this.importConfigs.forEach(conf => {
        this.options.push({key: conf.id, value: conf.id + " " + conf.idPrefix + " " + conf.library.name});
      })
    });
  }

  onSelectHandler(event: any){
    this.selectedConfigs = [];
    event.forEach(conf => {
      this.selectedConfigs.push(conf.key);
    });
  }

  runJob(){

    if (confirm("Are you sure you want to run jod: " + this.whoAmI)){
      this.jobsService.runJob(this.whoAmI, this.selectedConfigs);
    }
  }

  ngOnInit() {
    this.getConfigurations();
  }

}
