import {Component, OnInit} from "@angular/core";
import {JobRunnersService} from "./job-runners.service";
import {LoginService} from "../../login/login.service";
import {ADMIN} from "../../roles";
@Component({
	selector: 'app-job-runners',
	templateUrl: './job-runners.component.html',
	styleUrls: ['./job-runners.component.css']
})
export class JobRunnersComponent implements OnInit{

	constructor(private jobsService: JobRunnersService, private loginService: LoginService) { }
	runNoParamsJob(name: string){
		if (confirm('Are ypu sure you want to run this job?')){
			this.jobsService.runNoParamsJob(name);
		}
	}

	isAllowed(): boolean{
		return this.loginService.getRoles().indexOf(ADMIN) !== -1;
	}

	ngOnInit(): void {
	}

}