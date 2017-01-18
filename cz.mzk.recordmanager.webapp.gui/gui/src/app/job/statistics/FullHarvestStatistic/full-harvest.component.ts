import {Component, OnInit} from "@angular/core";
import {Field} from "../../../shared/field";
import {Router} from "@angular/router";
import {StatisticsService} from "../statistics.service";
import {SortControl} from "../../../shared/sort-control";
import {Style} from "../../../shared/style";
@Component({
	selector: 'app-full-harvest',
	templateUrl: './full-harvest.component.html',
	styleUrls: ['./full-harvest.component.css']
})

export class FullHarvestComponent implements OnInit{

	statistics: any[] = [];

	offset: number;

	sortBy: string;
	fields: Field[] = [];
	loading: boolean;

	statuses: string[] = [];

	startDate: Date = null;
	endDate: Date = null;
	fromParam: Date = null;
	toParam: Date = null;


	options: any = [];

	isMore: boolean = true;



	selected(event: any){
		this.statuses = [];
		event.forEach(item => {
			this.statuses.push(item.key);
		});
	}

	constructor(private router: Router, private statisticsService: StatisticsService, private sortControl: SortControl) { }

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

		this.options.push({key: "COMPLETED", value: "COMPLETED"});
		this.options.push({key: "FAILED", value: "FAILED"});
		this.options.push({key: "STARTED", value: "STARTED"});

		this.sortByMe("jobExecutionId");

		this.loading = true;
		this.offset = 0;
		this.getStatistics();
	}

	sortByMe(name: string){
		this.sortBy = this.sortControl.sortByMe(name, this.fields);
	}

	getArrow(name: string): boolean{
		return this.sortControl.getArrow(name, this.fields)== 'glyphicon-arrow-up' ? true: false;
	}
	getVisibility(name: string): boolean{
		return this.sortControl.getVisibility(name, this.fields) == 'visible' ? true: false;
	}


	setStart(event: any){
		this.startDate = event;
	}
	setEnd(event: any){
		this.endDate = event;
	}
	setFrom(event: any){
		this.fromParam = event;
	}
	setTo(event: any){
		this.toParam = event;
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