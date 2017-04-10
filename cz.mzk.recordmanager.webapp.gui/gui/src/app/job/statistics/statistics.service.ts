import {Injectable} from "@angular/core";
import {Http, Headers, RequestOptions, Response} from "@angular/http";
import {Observable} from "rxjs";
import {SERVER} from "../../server";
import {Library} from "../../model/library";
@Injectable()
export class StatisticsService{
	constructor(private http: Http){}



	getActualStatistics(): Observable<any>{
		let options = new RequestOptions();
		let startTime = new Date(Date.now());
		startTime.setDate(startTime.getDate() - 7);

		let formData = new FormData();
		formData.append("startDate", startTime);
		return this.http.get(SERVER + "/statistics/actuals" + "?startDate=" + startTime.getTime()).map((resp: Response) => {
			return resp.json();
		})
	}

	getOaiFullHarvestStatistics(offset: number): Observable<any>{
		return this.http.get(SERVER + "/statistics/oaiHarvestStatistics/" + offset).map((res: Response) => {
			return res.json();
		})
	}

	getOaiFullHarvestStatisticsInPeriods(startDate: Date, endDate: Date, fromDate: Date, toDate: Date, libraries: Library[]): Observable<any>{
		if (startDate == null) startDate = new Date(0);
		if (endDate == null) endDate = new Date(Date.now());
		if (fromDate == null) fromDate = new Date(0);
		if (toDate == null) toDate = new Date(Date.now());

		var headers = new Headers({"Content-Type": 'application/json', 'Accept': 'application/json'});
		let options = new RequestOptions({ headers: headers });


		return this.http.post(SERVER + "/statistics/oaiHarvestStatistics/inPeriods" +
			"?startDate=" + startDate.getTime() +
			"&endDate=" + endDate.getTime() +
			"&fromDate=" + fromDate.getTime() +
			"&toDate=" + toDate.getTime(),
			libraries,
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


		return this.http.get(SERVER + "/statistics/indexAllRecordsStatistics/inPeriods" +
			"?startDate=" + startDate.getTime() +
			"&endDate=" + endDate.getTime() +
			"&fromDate=" + fromDate.getTime() +
			"&toDate=" + toDate.getTime()
		).map((res: Response) => {
			return res.json();
		});
	}


	getDedupRecordsStatisticsInPeriods(startDate: Date, endDate: Date): Observable<any>{
		if (startDate == null) startDate = new Date(0);
		if (endDate == null) endDate = new Date(Date.now());

		return this.http.get(SERVER + "/statistics/dedupRecordsStatistics/inPeriods" +
			"?startDate=" + startDate.getTime() +
			"&endDate=" + endDate.getTime()
		).map((res: Response) => {
			return res.json();
		})
	}

	getDownloadImportConfStatistics(offset: number): Observable<any>{
		return this.http.get(SERVER + "/statistics/downloadImportConfStatistics/" + offset).map((res: Response) => {
			return res.json();
		})
	}

	getDownloadImportConfInPeriods(startDate: Date, endDate: Date): Observable<any>{
		if (startDate == null) startDate = new Date(0);
		if (endDate == null) endDate = new Date(Date.now());

		var headers = new Headers({"Content-Type": 'application/json', 'Accept': 'application/json'});
		let options = new RequestOptions({ headers: headers });

		return this.http.get(SERVER + "/statistics/downloadImportConfStatistics/inPeriods" +
			"?startDate=" + startDate.getTime() +
			"&endDate=" + endDate.getTime()
		).map((res: Response) => {
			return res.json();
		})

	}


	getDetals(jobExecutionId: number): Observable<any>{
		return this.http.get(SERVER + "/statistics/details/" + jobExecutionId).map((response: Response)=>{
			return response.json();
		});
	}

	getRegenerateDedupKeysStatistics(offset: number): Observable<any>{
		return this.http.get(SERVER + "/statistics/regenerateDedupKeysStatistics/" + offset).map((res:Response) => {
			return res.json();
		});
	}

	getRegenerateDedupKeysInPeriod(startDate: Date, endDate: Date): Observable<any>{
		if (startDate == null) startDate = new Date(0);
		if (endDate == null) endDate = new Date(Date.now());

		return this.http.get(SERVER + "/statistics/regenerateDedupKeysStatistics/inPeriods" +
			"?startDate=" + startDate.getTime() +
			"&endDate=" + endDate.getTime()
		).map((res: Response) => {
			return res.json();
		})
	}

}