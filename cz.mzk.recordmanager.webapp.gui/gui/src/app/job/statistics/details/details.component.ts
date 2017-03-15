import {Component, OnInit} from "@angular/core";
import {StatisticsService} from "../statistics.service";
import {ActivatedRoute, Params} from "@angular/router";
@Component({
	selector: 'app-details',
	templateUrl: './details.component.html',
	styleUrls: ['./details.component.css']
})

export class DetailsComponent implements OnInit{

	details: any;
	loading: boolean = true;

	constructor(private statisticsService: StatisticsService, private route: ActivatedRoute){}

	getDetails(jobExecutionId){
		this.loading = true;
		this.statisticsService.getDetals(jobExecutionId).subscribe(res => {
			this.details = res;
			this.loading = false;
		});
	}

	ngOnInit(): void {
		this.route.params.subscribe((params:Params) => {
			let jobExecutionId = params['jobExecId'];
			this.getDetails(jobExecutionId);
		})
	}

}
