import {ImportConfig} from "./import-config";
export class DownloadImportConfiguration extends ImportConfig{
  url: string;
  format: string;
  jobName: string;
  regex: string;

  constructor(obj?: any){
    super(obj);
    this.url         = obj && obj.url     || "";
    this.format      = obj && obj.format  || "";
    this.jobName     = obj && obj.jobName || "";
    this.regex       = obj && obj.regex   || "";
  }
}
