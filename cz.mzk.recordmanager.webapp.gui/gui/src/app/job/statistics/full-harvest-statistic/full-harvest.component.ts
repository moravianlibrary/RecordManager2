import {Component, OnInit} from "@angular/core";
import {StatisticsService} from "../statistics.service";
import {SortControl} from "../../../shared/sort-control";
import {StatisticsComponent} from "../statistics.component";
import {Field} from "../../../shared/field";
import {Style} from "../../../shared/style";
@Component({
	selector: 'app-full-harvest',
	templateUrl: './full-harvest.component.html',
	styleUrls: ['./full-harvest.component.css']
})

export class FullHarvestComponent extends StatisticsComponent implements OnInit{



	constructor(protected sortControl: SortControl, protected statisticsService: StatisticsService){
		super(sortControl, statisticsService);
	}

	getStatistics(){
		this.statisticsService.getOaiFullHarvestStatistics(this.offset).subscribe(stats => {
			stats.forEach(st => {
				this.statistics.push(st);
			});
			this.loading = false;

			// Is can load 10 more statistics isMore == true. Not very good solution, Hard-code =(
			if (stats.length >= 10){
				this.isMore = true;
			}else {
				this.isMore = false;
			}
		});
	}

	ngOnInit() {
		super.ngOnInit();

		this.fields.push(new Field({'_name': 'jobExecutionId', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'importConfId', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'libraryName', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'url', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'startTime', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'endTime', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'status', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'fromParam', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'toParam', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'noOfRecords', '_style': new Style()}));

		this.sortByMe("jobExecutionId");

		this.getStatistics();
	}

	nextPartOfStats(offset: number){
		this.offset = this.offset + offset;
		this.getStatistics();
	}

	getStatisticsInPeriods(){
		if (
			this.startDate == null &&
			this.endDate == null &&
			this.fromParam == null &&
			this.toParam == null
		){
			this.offset = 0;
			this.statistics = [];
			this.getStatistics();
		}else{
			this.statisticsService.getOaiFullHarvestStatisticsInPeriods(
				this.startDate,
				this.endDate,
				this.fromParam,
				this.toParam).subscribe(res => {
				this.statistics = res;
				this.offset = 0;
				this.isMore = false;
			});
		}


	}
}