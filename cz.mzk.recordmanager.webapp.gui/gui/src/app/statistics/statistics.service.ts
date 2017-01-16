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

  getStatistics(offset: number): Observable<OaiHarvestJobStatistics[]>{
    return this.http.get(SERVER + "/oaiHarvestStats/offset/" + offset).map((res: Response) => {
      return res.json();
    })
  }

  getLibraryWithConfiguration(configId: number): Observable<Id>{
    return this.http.get(SERVER + "/statistics/" + configId).map((res: Response) => {
      return res.json();
    });
  }




}
