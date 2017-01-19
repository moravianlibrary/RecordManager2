import {Component, OnInit} from "@angular/core";
import {StatisticsService} from "../statistics.service";
@Component({
	selector: 'app-index-all-records',
	templateUrl: './index-all-records.component.html',
	styleUrls: ['./index-all-records.component.css']
})
export class IndexAllRecordsComponent implements OnInit{

	statistics: any[] = [];
	offset: number = 0;

	loading: boolean = true;

	constructor(private statisticsService: StatisticsService){}

	getIndexAllRecordsStats(){
		this.loading = true;
		this.statisticsService.getIndexAllRecordsStatistics(this.offset).subscribe(res => {
			res.forEach(item => {
				this.statistics.push(item);
			});

			this.loading = false;
		});
	}

	ngOnInit(): void {
		this.getIndexAllRecordsStats()
	}

}