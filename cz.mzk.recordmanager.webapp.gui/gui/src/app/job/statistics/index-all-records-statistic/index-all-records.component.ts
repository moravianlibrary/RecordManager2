import {Component, OnInit} from "@angular/core";
import {StatisticsService} from "../statistics.service";
import {SortControl} from "../../../shared/sort-control";
import {StatisticsComponent} from "../statistics.component";
import {Field} from "../../../shared/field";
import {Style} from "../../../shared/style";
@Component({
	selector: 'app-index-all-records',
	templateUrl: './index-all-records.component.html',
	styleUrls: ['./index-all-records.component.css']
})
export class IndexAllRecordsComponent extends StatisticsComponent implements OnInit{


	constructor(protected sortControl: SortControl, protected statisticsService: StatisticsService){
		super(sortControl, statisticsService);
	}
	getIndexAllRecordsStats(){
		this.loading = true;
		this.statisticsService.getIndexAllRecordsStatistics(this.offset).subscribe(res => {
			res.forEach(item => {
				this.statistics.push(item);
			});

			if (res.length >= 10){
				this.isMore = true;
			}else {
				this.isMore = false;
			}
			this.loading = false;
		});
	}

	nextPartOfStats(offset: number){
		this.offset += offset;
		this.getIndexAllRecordsStats();
	}

	ngOnInit(): void {
		super.ngOnInit();

		this.fields.push(new Field({'_name': 'jobExecutionId', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'startTime', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'endTime', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'status', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'fromParam', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'toParam', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'stringVal', '_style': new Style()}));

		this.sortByMe("jobExecutionId");

		this.getIndexAllRecordsStats()
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
			this.getIndexAllRecordsStats();
		}else{
			this.statisticsService.getIndexAllRecordsStatisticsInPeriods(
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