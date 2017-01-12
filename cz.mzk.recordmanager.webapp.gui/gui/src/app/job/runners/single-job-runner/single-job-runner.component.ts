import {Component, OnInit, Input} from "@angular/core";
import {ImportConfig} from "../../../model/import-config";
import {JobRunnersService} from "../job-runners.service";
@Component({
	selector: 'app-single-job-runner',
	templateUrl: './single-job-runner.component.html',
	styleUrls: ['single-job-runner.component.css']
})
export class SingleJobRunnerComponent implements OnInit{

	private importConfigs: ImportConfig[] = [];

	selectedConfigs: number[] = [];

	@Input()
	whoAmI: string;

	@Input()
	needId: boolean;

	options: any[] = [];


	constructor(private jobRunnersService: JobRunnersService) {
	}

	getConfigurations(){
		this.jobRunnersService.getImportConfigurations().subscribe(configs => {
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
			this.jobRunnersService.runJob(this.whoAmI, this.selectedConfigs);
		}
	}

	ngOnInit() {
		this.getConfigurations();
	}

	
}