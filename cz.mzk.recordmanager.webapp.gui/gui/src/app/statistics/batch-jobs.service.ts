import { Injectable } from '@angular/core';
import {Http, Response, Headers, RequestOptions} from "@angular/http";
import {Observable} from "rxjs";
import {BatchJobExecution} from "../model/batch-job-execution";
import {SERVER} from "../server";

@Injectable()
export class BatchJobsService {

  constructor(private http: Http) { }

  getDetails(id: number): Observable<BatchJobExecution>{
    return this.http.get(SERVER + "/batches/" + id)
      .map((res: Response) => {
        return new BatchJobExecution({
          'id': res.json().id,
          'jobInstanceID': res.json().jobInstanceID,
          'createTime': res.json().createTime,
          'startTime': res.json().startTime,
          'endTime': res.json().endTime,
          'status': res.json().status,
          'exitCode': res.json().exitCode,
          'exitMessage': res.json().exitMessage
        });
      });
  }

  restart(jobExecution: BatchJobExecution): Observable<void>{
    var headers = new Headers({"Content-Type": 'application/json'});
    let options = new RequestOptions({ headers: headers });
    return this.http.patch(SERVER + "/batches", JSON.stringify(jobExecution), options).map(res => {
      return ;
    });
  }

}
