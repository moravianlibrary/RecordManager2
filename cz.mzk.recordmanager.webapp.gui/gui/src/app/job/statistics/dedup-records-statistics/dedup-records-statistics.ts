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

	getDedupRecordsStatistics(){
		this.loading = true;
		this.statisticsService.getDedupRecordsStatistics(this.offset).subscribe(res => {
			res.forEach(st => {

				if (st.startTime != null && st.endTime != null){
					st.duration = st.endTime - st.startTime;
				}else{
					st.duration = null;
				}
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

		this.fields.push(new Field({'_name': 'duration', '_style': new Style()}));

		this.sortByMe('jobExecutionId');

		this.getDedupRecordsStatistics();

	}

	nextPartOfStats(offset: number){
		this.offset = this.offset + offset;
		this.getDedupRecordsStatistics();
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
			this.getDedupRecordsStatistics();
		}else{
				this.loading = true;
				this.statisticsService.getDedupRecordsStatisticsInPeriods(
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