import {Component, OnInit} from "@angular/core";
import {StatisticsComponent} from "../statistics.component";
import {SortControl} from "../../../shared/sort-control";
import {StatisticsService} from "../statistics.service";
import {Field} from "../../../shared/field";
import {Style} from "../../../shared/style";
@Component({
	selector: 'app-regenerate-dedup-keys',
	templateUrl: './regenerate-dedup-keys-statistics.component.html',
	styleUrls: ['./regenerate-dedup-keys-statistics.component.css']
})
export class RegenerateDedupKeysComponent extends StatisticsComponent implements OnInit{

	constructor(protected sortControl: SortControl, protected statisticsService: StatisticsService){
		super(sortControl, statisticsService);
	}

	getRegenerateDedupKeysStatistics(){
		this.statisticsService.getRegenerateDedupKeysStatistics(this.offset).subscribe(stats => {
			stats.forEach(st => {
				if (st.startTime != null && st.endTime != null){
					st.duration = st.endTime - st.startTime;
				}else{
					st.duration = null;
				}

				this.statistics.push(st);
			});

			if (stats.length >= 10){
				this.isMore = true;
			}else {
				this.isMore = false;
			}

			this.loading = false;
		})
	}

	nextPartOfStats(offset: number){
		this.offset += offset;
		this.getRegenerateDedupKeysStatistics();
	}

	ngOnInit(): void {
		super.ngOnInit();

		this.fields.push(new Field({'_name': 'jobExecutionId', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'startTime', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'endTime', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'status', '_style': new Style()}));

		this.fields.push(new Field({'_name': 'duration', '_style': new Style()}));

		this.sortByMe("jobExecutionId");

		this.getRegenerateDedupKeysStatistics();
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
			this.getRegenerateDedupKeysStatistics();
		}else{
				this.loading = true;
			this.statisticsService.getRegenerateDedupKeysInPeriod(
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