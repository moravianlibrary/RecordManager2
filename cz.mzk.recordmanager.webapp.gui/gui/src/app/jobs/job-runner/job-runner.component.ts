import {Component, OnInit, Input, style} from '@angular/core';
import {JobsService} from "../jobs.service";
import {ImportConfig} from "../../model/import-config";

class ItemStyle{
  id: number;
  selected: boolean;

  constructor(obj?: any) {
    this.id           = obj && obj.id         ||  -1;
    this.selected     = obj && obj.selected   ||  false;
  }
}


@Component({
  selector: 'app-job-runner',
  templateUrl: './job-runner.component.html',
  styleUrls: ['./job-runner.component.css'],
})
export class JobRunnerComponent implements OnInit{

  private itemStyles: ItemStyle[] = [];

  private importConfigs: ImportConfig[] = [];

  @Input()
  whoAmI: string;

  @Input()
  needId: boolean;

  selectedConfiguration: ImportConfig = new ImportConfig;


  constructor(private jobsService: JobsService) { }

  getConfigurations(){
    this.jobsService.getImportConfigurations().subscribe(configs => {
      this.importConfigs = configs;
      for (var index = 0; index < this.importConfigs.length; ++index){
        this.itemStyles.push(new ItemStyle({id: this.importConfigs[index].id}));
      }
    });
  }

  selectMe(id: number){
    this.itemStyles.forEach(item => {
      if (item.id == id){
        item.selected = true;
      }else {
        item.selected = false;
      }
    });
  }

  chooseConfiguration(id: number){
    this.importConfigs.filter((conf) => {
      if (conf.id == id){
        this.selectedConfiguration = new ImportConfig(conf);
      }
    });
    this.selectMe(id);
  }

  runJob(){
    if (this.needId){
      if (this.selectedConfiguration.idPrefix.length > 0 && this.selectedConfiguration.id != null)
        this.jobsService.runJob(this.whoAmI, this.selectedConfiguration.id);
    }else {
      this.jobsService.runJob(this.whoAmI);
    }
  }

  amISelected(id: number): boolean{
    var isSelected = false;
    this.itemStyles.forEach(item => {
      if (item.id == id){
        isSelected = item.selected;
      }
    });
    return isSelected;
  }

  ngOnInit() {
    this.getConfigurations();
  }

}
