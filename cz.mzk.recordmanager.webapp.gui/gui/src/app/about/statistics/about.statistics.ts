import {Component, OnInit} from "@angular/core";
@Component({
	selector: 'app-about-statistics',
	templateUrl: './about.statistics.html',
	styleUrls: ['./about.statistics.css']
})
export class AboutStatisticsComponent implements OnInit{
	imagesRoot : string = "../../../images";
	statisticsLocationPath : string = this.imagesRoot + "/StatisticsLocation.png";
	statisticsTabsPath : string = this.imagesRoot + "/StatisticsTabs.png";
	statusesChooser : string =  this.imagesRoot + "/StatusesChooser.png";
	advancedSearch : string = this.imagesRoot + "/AdvancedSearch.png";
	datePicker : string = this.imagesRoot + "/ChooseDate.png";
	hourMinute : string = this.imagesRoot + "/HourMinute.png";
	ngOnInit(): void {
	}

}
