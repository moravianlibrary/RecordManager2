import {Component, OnInit} from "@angular/core";
import {Field} from "../../shared/field";
import {SortControl} from "../../shared/sort-control";
import {StatisticsService} from "./statistics.service";

@Component({
	selector: 'app-statistics',
	templateUrl: '/statistics.component.html',
	styleUrls: ['./statistics.component.css']
})

export class StatisticsComponent implements OnInit{

	statistics: any[] = [];

	offset: number;


	fields: Field[] = [];
	loading: boolean;

	statuses: string[] = [];

	sortBy: string;

	startDate: Date = null;
	endDate: Date = null;
	fromParam: Date = null;
	toParam: Date = null;


	options: any = [];

	isMore: boolean = true;

	constructor(protected sortControl: SortControl, protected statisticsService: StatisticsService){
	}


	getArrow(name: string): boolean{
		return this.sortControl.getArrow(name, this.fields)== 'glyphicon-arrow-up' ? true: false;
	}
	getVisibility(name: string): boolean{
		return this.sortControl.getVisibility(name, this.fields) == 'visible' ? true: false;
	}

	sortByMe(name: string){
		this.sortBy = this.sortControl.sortByMe(name, this.fields);
	}

	selected(event: any){
		this.statuses = [];
		event.forEach(item => {
			this.statuses.push(item.key);
		});
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


	ngOnInit(): void {
		this.options.push({key: "COMPLETED", value: "COMPLETED"});

		this.options.push({key: "FAILED", value: "FAILED"});

		this.options.push({key: "STARTED", value: "STARTED"});

		this.offset = 0;

		this.loading = true;

	}

}