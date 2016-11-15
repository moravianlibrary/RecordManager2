import { Injectable } from '@angular/core';
import {Http, Response} from "@angular/http";
import {Observable} from "rxjs";
import {OaiHarvestJobStatistics} from "../model/oai-harvest-job-statistics";
import {SERVER} from "../server";
import {BatchJobExecution} from "../model/batch-job-execution";
import {Id} from "../model/id";

@Injectable()
export class StatisticsService {

  constructor(private http: Http) { }

  getStatistics(): Observable<OaiHarvestJobStatistics[]>{
    return this.http.get(SERVER + "/oaiHarvestStats").map((res: Response) => {
      return res.json();
    })
  }

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
  getLibraryWithConfiguration(configId: number): Observable<Id>{
    return this.http.get(SERVER + "/oaiHarvestStats/" + configId).map((res: Response) => {
      return res.json();
    });
  }


}
