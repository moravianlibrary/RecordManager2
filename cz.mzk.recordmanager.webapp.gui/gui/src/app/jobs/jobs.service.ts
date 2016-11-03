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

  runJob(name: string, id: number){
    var headers = new Headers({"Content-Type": 'application/json'});
    let options = new RequestOptions({ headers: headers });
    console.log(SERVER + "/batches/run/" + name);
    console.log(JSON.stringify({id: id}));
    this.http.post(SERVER + "/batches/run/" + name, JSON.stringify({id: id}), options).subscribe();
  }
}
