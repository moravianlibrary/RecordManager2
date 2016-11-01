import { Injectable } from '@angular/core';
import {Observable} from "rxjs";
import {ImportConfig} from "../model/import-config";
import {Http, Response} from "@angular/http";
import {SERVER} from "../server";

@Injectable()
export class JobsService {

  constructor(private http: Http) { }


  getImportConfigurations(): Observable<ImportConfig[]>{
    return this.http.get(SERVER + "/importConfiguration").map((res: Response) => res.json());
  }
}
