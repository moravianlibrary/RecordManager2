import { Component, OnInit } from '@angular/core';
import {JobsService} from "../jobs.service";
import {ImportConfig} from "../../model/import-config";

@Component({
  selector: 'app-import-records-job',
  templateUrl: './import-records-job.component.html',
  styleUrls: ['./import-records-job.component.css']
})
export class ImportRecordsJobComponent implements OnInit {

  private importConfigs: ImportConfig[] = [];
  options: any[] = [];

  formats: any[] = [];

  id: number;
  file: File;
  format: string;

  constructor(private jobsService: JobsService) { }

  getConfigurations(){
    this.jobsService.getImportConfigurations().subscribe(configs => {
      this.importConfigs = configs;

      this.importConfigs.forEach(conf => {
        this.options.push({key: conf.id, value: conf.id + " " + conf.idPrefix + " " + conf.library.name});
      })
    });
  }

  getFormats(){
    this.jobsService.getFormats().subscribe(formats => {
      formats.forEach(fr => {
        this.formats.push({key: fr, value: fr});
      });
    });
  }

  selectConfig(event: any){
    if (event.length > 0){
      this.id = event[0].key;
    }else {
      this.id = null;
    }
  }

  chooseFile(event: any){
    this.file = event.srcElement.files[0];

    console.log(this.file);
  }

  selectFormat(event: any){
    if (event.length > 0)
      this.format = event[0].key;
    else
      this.format = null;

  }

  runImportRecordsJob(){
    if (confirm("Are you sure you want to run this job?")){
      this.jobsService.runImportRecordsJob({id: this.id, file: this.file, format: this.format});
    }
  }

  ngOnInit() {
    this.getConfigurations();
    this.getFormats();
  }

}
