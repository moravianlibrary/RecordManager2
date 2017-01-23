import {Component, OnInit} from "@angular/core";
import {StatisticsComponent} from "../statistics.component";
import {Field} from "../../../shared/field";
import {Style} from "../../../shared/style";
import {SortControl} from "../../../shared/sort-control";
import {StatisticsService} from "../statistics.service";
@Component({
	selector: 'app-dedup-records',
	templateUrl: './dedup-records-statistics.html',
	styleUrls: ['./dedup-records-statistics.css']
})
export class DedupRecordsComponent extends StatisticsComponent implements OnInit{

	constructor(protected sortControl: SortControl, protected  statisticsService: StatisticsService){
		super(sortControl, statisticsService);
	}

	getDedupRecords(){
		this.loading = true;
		this.statisticsService.getDedupRecordsStatistics(this.offset).subscribe(res => {
			res.forEach(st => {
				this.statistics.push(st);
			});

			// Is can load 10 more statistics isMore == true. Not very good solution, Hard-code =(
			if (res.length >= 10){
				this.isMore = true;
			}else {
				this.isMore = false;
			}
			this.loading = false;
		})
	}

	ngOnInit(): void {
		super.ngOnInit();

		this.fields.push(new Field({'_name': 'jobExecutionId', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'startTime', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'endTime', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'status', '_style': new Style()}));

		this.sortByMe('jobExecutionId');

		this.getDedupRecords();

	}

	nextPartOfStats(offset: number){
		this.offset = this.offset + offset;
		this.getDedupRecords();
	}
}