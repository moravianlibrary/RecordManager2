import {Component, OnInit} from "@angular/core";
import {JobRunnersService} from "./runners/job-runners.service";
@Component({
	selector: 'app-job',
	templateUrl: './job.component.html',
	styleUrls: ['./job.component.css']
})

export class JobComponent implements OnInit{

	constructor(private jobsService: JobRunnersService) { }
	runNoParamsJob(name: string){
		if (confirm('Are ypu sure you want to run this job?')){
			this.jobsService.runNoParamsJob(name);
		}
	}

	ngOnInit(): void {
	}

}
