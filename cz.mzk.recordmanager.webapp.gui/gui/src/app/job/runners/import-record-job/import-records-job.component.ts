import {Component, OnInit} from "@angular/core";
import {ImportConfig} from "../../../model/import-config";
import {JobRunnersService} from "../job-runners.service";
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
	format: string = null;

	constructor(private jobRunnersService: JobRunnersService) { }

	getConfigurations(){
		this.jobRunnersService.getImportConfigurations().subscribe(configs => {
			this.importConfigs = configs;

			this.importConfigs.forEach(conf => {
				this.options.push({key: conf.id, value: conf.id + " " + conf.idPrefix + " " + conf.library.name});
			})
		});
	}

	getFormats(){
		this.jobRunnersService.getFormats().subscribe(formats => {
			formats.forEach(fr => {
				this.formats.push({key: fr, value: fr});
			});
			this.formats.push({key: null, value: "null"});
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

	}

	selectFormat(event: any){
		if (event.length > 0)
			this.format = event[0].key;
		else
			this.format = null;

	}

	runImportRecordsJob(){
		if (confirm("Are you sure you want to run this job?")){
			this.jobRunnersService.runImportRecordsJob({id: this.id, file: this.file, format: this.format});
		}
	}

	ngOnInit() {
		this.getConfigurations();
		this.getFormats();
	}

}
