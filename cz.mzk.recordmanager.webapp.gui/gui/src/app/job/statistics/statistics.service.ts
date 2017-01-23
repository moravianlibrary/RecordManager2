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
		startTime.setDate(startTime.getDate() - 7);

		let formData = new FormData();
		formData.append("startDate", startTime);
		return this.http.post(SERVER + "/statistics/actuals", formData, options).map((resp: Response) => {
			return resp.json();
		});
	}

	getOaiFullHarvestStatistics(offset: number): Observable<any>{
		return this.http.get(SERVER + "/statistics/oaiHarvestStatistics/" + offset).map((res: Response) => {
			return res.json();
		})
	}

	getOaiFullHarvestStatisticsInPeriods(startDate: Date, endDate: Date, fromDate: Date, toDate: Date): Observable<any>{
		if (startDate == null) startDate = new Date(0);
		if (endDate == null) endDate = new Date(Date.now());
		if (fromDate == null) fromDate = new Date(0);
		if (toDate == null) toDate = new Date(Date.now());

		var headers = new Headers({"Content-Type": 'application/json', 'Accept': 'application/json'});
		let options = new RequestOptions({ headers: headers });

		return this.http.post(SERVER + "/statistics/oaiHarvestStatistics/inPeriods",
			JSON.stringify([{start: startDate, end: endDate}, {start: fromDate, end: toDate}]),
		options).map((res:Response) => {

			return res.json();
		});
	}

	getIndexAllRecordsStatistics(offset: number): Observable<any>{
		return this.http.get(SERVER + "/statistics/indexAllRecordsStatistics/" + offset).map((res: Response) => {
			return res.json();
		})
	}

	getDedupRecordsStatistics(offset: number): Observable<any>{
		return this.http.get(SERVER + "/statistics/dedupRecordsStatistics/" + offset).map((res: Response) => {
			return res.json();
		})
	}

	getIndexAllRecordsStatisticsInPeriods(startDate: Date, endDate: Date, fromDate: Date, toDate: Date): Observable<any>{
		if (startDate == null) startDate = new Date(0);
		if (endDate == null) endDate = new Date(Date.now());
		if (fromDate == null) fromDate = new Date(0);
		if (toDate == null) toDate = new Date(Date.now());

		var headers = new Headers({"Content-Type": 'application/json', 'Accept': 'application/json'});
		let options = new RequestOptions({ headers: headers });

		console.log(startDate);
		console.log(endDate);
		console.log(fromDate);
		console.log(toDate);


		return this.http.post(SERVER + "/statistics/indexAllRecordsStatistics/inPeriods",
			JSON.stringify([{start: startDate, end: endDate}, {start: fromDate, end: toDate}]),
			options).map((res:Response) => {
			console.log(res);
			return res.json();
		});
	}

}