import {Library} from "./library";
import {Observable} from "rxjs";
export class ImportConfig {
  id: number;
  library: Library;
  idPrefix: string;

  constructor(obj?: any) {
    this.id							     = obj && obj.id						 || null;
    this.library						 = obj && obj.librar		     || new Library();
    this.idPrefix            = obj && obj.idPrefix       ||   "";
  }

}
