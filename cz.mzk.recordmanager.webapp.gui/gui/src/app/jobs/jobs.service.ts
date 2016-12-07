import { Injectable } from '@angular/core';
import {Observable} from "rxjs";
import {ImportConfig} from "../model/import-config";
import {Http, Response, Headers, RequestOptions} from "@angular/http";
import {SERVER} from "../server";

@Injectable()
export class JobsService {

  constructor(private http: Http) { }


  getImportConfigurations(): Observable<ImportConfig[]>{
    return this.http.get(SERVER + "/importConfiguration").map((res: Response) => res.json());
  }

  runJob(name: string, idList: number[]){
    var headers = new Headers({"Content-Type": 'application/json'});
    let options = new RequestOptions({ headers: headers });
    let ids = idList.map(item => { return {id: item} })
    this.http.post(SERVER + "/batches/run/" + name, JSON.stringify(ids), options).subscribe();
  }


  runIndividualRecordsToSolrJob(recordIds: any[]){
    var headers = new Headers({"Content-Type": 'application/json'});
    let options = new RequestOptions({ headers: headers });
    this.http.post(SERVER + "/batches/run/indexIndividualIndexesToSolr", JSON.stringify(recordIds), options).subscribe();
  }

  runImportRecordsJob(importRecords: any){
    let options = new RequestOptions();
    let formData = new FormData();
    formData.append("file", importRecords.file, importRecords.file.name);
    formData.append("id", importRecords.id);
    formData.append("format", importRecords.format);

    this.http.post(SERVER + "/batches/run/importRecordsJob", formData, options).subscribe();
  }

  runNoParamsJob(name: string){
    this.http.post(SERVER + "/batches/run/" + name, JSON, new RequestOptions()).subscribe();
  }

  getFormats(): Observable<string[]>{
    return this.http.get(SERVER + "/format").map((res: Response) => res.json());
  }
}
