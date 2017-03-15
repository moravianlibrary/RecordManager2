import {Component, OnInit} from "@angular/core";
import {StatisticsService} from "../statistics.service";
import {SortControl} from "../../../shared/sort-control";
import {StatisticsComponent} from "../statistics.component";
import {Field} from "../../../shared/field";
import {Style} from "../../../shared/style";
@Component({
	selector: 'app-actual-statistics',
	templateUrl: './actual-statistic.component.html',
	styleUrls: ['./actual-statistic.component.css']
})

export class ActualStatisticsComponent extends StatisticsComponent implements OnInit{


	constructor(protected sortControl: SortControl, protected  statisticsService: StatisticsService){
		super(sortControl, statisticsService);
	}


	getActuals(){
		this.loading = true;
		this.statisticsService.getActualStatistics().subscribe(res => {
			this.statistics = res;
			this.loading = false;
		});
	}


	ngOnInit(): void {
		super.ngOnInit();

		this.fields.push(new Field({'_name': 'jobExecutionId', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'jobName', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'status', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'startTime', '_style': new Style()}));

		this.sortByMe('jobExecutionId');

		this.getActuals();
	}


}