import {Component, OnInit} from "@angular/core";
import {StatisticsService} from "../statistics.service";
@Component({
	selector: 'app-actual-statistics',
	templateUrl: './actual-statistic.component.html',
	styleUrls: ['./actual-statistic.component.css']
})

export class ActualStatisticsComonent implements OnInit{

	actualStats: any[] = [];

	constructor(private statisticsService: StatisticsService){}

	getRunning(): any{
		return this.actualStats.filter(item => item.status == 'STARTED');
	}
	getFinished():any{
		return this.actualStats.filter(item => item.status != 'STARTED');
	}

	getActuals(){

		this.statisticsService.getActualStatistics().subscribe(res => {
			this.actualStats = res;
		});
	}

	ngOnInit(): void {
		this.getActuals();
	}

}