import {Component, OnInit} from "@angular/core";
import {StatisticsService} from "../statistics.service";
import {SortControl} from "../../../shared/sort-control";
import {StatisticsComponent} from "../statistics.component";
import {Field} from "../../../shared/field";
import {Style} from "../../../shared/style";
import {LibrariesService} from "../../../library/libraries/libraries.service";
import {Library} from "../../../model/library";
@Component({
	selector: 'app-full-harvest',
	templateUrl: './full-harvest.component.html',
	styleUrls: ['./full-harvest.component.css']
})

export class FullHarvestComponent extends StatisticsComponent implements OnInit{

	libraries: any = [];

	selectedLibraries: Library[]  = [];

	constructor(protected sortControl: SortControl, protected statisticsService: StatisticsService, protected librariesService: LibrariesService){
		super(sortControl, statisticsService);
	}

	getStatistics(){
		this.statisticsService.getOaiFullHarvestStatistics(this.offset).subscribe(stats => {
			stats.forEach(st => {
				if (st.startTime != null && st.endTime != null){
					st.duration = st.endTime - st.startTime;
				}else{
					st.duration = null;
				}
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

	getLibraries(){
		this.librariesService.getLibraries().subscribe(libs => {
			libs.forEach(l => {
				this.libraries.push({key: l.id, value: l.name || ""});
			});
		});
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
				this.loading = true;

				this.statisticsService.getOaiFullHarvestStatisticsInPeriods(
					this.startDate,
					this.endDate,
					this.fromParam,
					this.toParam,
					this.selectedLibraries
				).subscribe(res => {
					this.statistics = [];
					res.forEach(r => {
						if (r.startTime != null && r.endTime != null){
							r.duration = r.endTime - r.startTime;
						}else{
							r.duration = null;
						}
						this.statistics.push(r);
					});

					this.loading = false;
					this.offset = 0;
					this.isMore = false;
			});
		}
	}

	selectLibrary(event: any){
		this.selectedLibraries = [];
		event.forEach(l => {
			this.selectedLibraries.push(new Library({id: l.key, name: l.value}))
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

		this.fields.push(new Field({'_name': 'duration', '_style': new Style()}));

		this.sortByMe("jobExecutionId");

		this.getStatistics();

		this.getLibraries();
	}


}