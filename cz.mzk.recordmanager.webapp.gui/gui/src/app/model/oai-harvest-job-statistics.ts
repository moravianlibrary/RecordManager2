export class OaiHarvestJobStatistics {

  constructor(obj?: any) {
    this.jobExecutionId       = obj && obj.jobExecutionId  || -100;
    this.importConfId         = obj && obj.importConfId    || -100;
    this.libraryName          = obj && obj.libraryName     || "";
    this.url                  = obj && obj.url             || "";
    this.setSpec              = obj && obj.setSpec         || "";
    this.startTime            = obj && obj.startTime       || new Date();
    this.endTime              = obj && obj.endTime         || new Date();
    this.status               = obj && obj.status          || "???";
    this.fromParam            = obj && obj.fromParam       || new Date();
    this.toParam              = obj && obj.toParam         || new Date();
    this.noOfRecords          = obj && obj.noOfRecords     || -100;
  }

  jobExecutionId: number;
  importConfId: number;
  libraryName: string;
  url: string;
  setSpec:string;
  startTime: Date;
  endTime: Date;
  status: string;
  fromParam: Date;
  toParam: Date;
  noOfRecords: number;
}
