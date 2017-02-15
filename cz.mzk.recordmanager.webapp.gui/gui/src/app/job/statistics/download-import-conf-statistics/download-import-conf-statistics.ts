import {Component, OnInit} from "@angular/core";
import {StatisticsComponent} from "../statistics.component";
import {Field} from "../../../shared/field";
import {Style} from "../../../shared/style";
import {SortControl} from "../../../shared/sort-control";
import {StatisticsService} from "../statistics.service";
@Component({
	selector: 'app-download-import-conf',
	templateUrl: './download-import-conf-statistics.html',
	styleUrls: ['./download-import-conf-statistics.css']
})
export class DownloadImportConfComponent extends StatisticsComponent implements OnInit{

	constructor(protected sortControl: SortControl, protected statisticsService: StatisticsService){
		super(sortControl, statisticsService);
	}

	getDownloadImportConfStatistics(){
		this.statisticsService.getDownloadImportConfStatistics(this.offset).subscribe(stats => {
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
		})
	}


	ngOnInit(){
		super.ngOnInit();

		this.fields.push(new Field({'_name': 'jobExecutionId', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'importConfId', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'libraryName', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'url', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'startTime', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'endTime', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'status', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'importJobName', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'noOfRecords', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'duration', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'format', '_style': new Style()}));

		this.sortByMe("jobExecutionId");

		this.getDownloadImportConfStatistics();
	}

	nextPartOfStats(offset: number){
		this.offset = this.offset + offset;
		this.getDownloadImportConfStatistics();
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
			this.getDownloadImportConfStatistics();
		}else{
			this.loading = false;
			this.statisticsService.getDownloadImportConfInPeriods(
				this.startDate,
				this.endDate).subscribe(res => {
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
}