import {ImportConfig} from "./import-config";
export class KrameriusConfiguration extends ImportConfig{
  url: string;
  urlSolr: string;
  queryRows: number;
  metadataStream: string;
  authToken: string;
  downloadFulltexts: boolean;
  fulltextHarvestType: string ;
  harvestJobName: string;

  constructor(obj?: any){
    super(obj);
    this.url                              = obj && obj.url                  || "";
    this.urlSolr                          = obj && obj.urlSolr              || "";
    this.queryRows                        = obj && obj.queryRows            || -1;
    this.metadataStream                   = obj && obj.metadataStream       || "";
    this.authToken                        = obj && obj.authToken            || "";
    this.downloadFulltexts                = obj && obj.downloadFulltexts    || "";
    this.fulltextHarvestType              = obj && obj.fulltextHarvestType  || "";
    this.harvestJobName                   = obj && obj.harvestJobName       || "";
  }

}
