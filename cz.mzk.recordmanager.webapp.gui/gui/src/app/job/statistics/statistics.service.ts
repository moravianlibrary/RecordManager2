import {Injectable} from "@angular/core";
import {Http, Headers, RequestOptions, Response} from "@angular/http";
import {Observable} from "rxjs";
import {SERVER} from "../../server";
@Injectable()
export class StatisticsService{
	constructor(private http: Http){}

	getActualStatistics(): Observable<any>{
		let options = new RequestOptions();
		let startTime = new Date(Date.now());
		startTime.setDate(startTime.getDate() - 200);

		let formData = new FormData();
		formData.append("startDate", startTime);
		return this.http.post(SERVER + "/statistics/actuals", formData, options).map((resp: Response) => {
			return resp.json();
		});
	}

}