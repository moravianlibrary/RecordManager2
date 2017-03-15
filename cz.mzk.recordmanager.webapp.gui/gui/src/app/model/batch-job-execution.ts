import {Id} from "./id";
export class BatchJobExecution extends Id{

  jobInstanceID: number;

  createTime: Date;

  startTime: Date;

  endTime: Date;

  status: string;

  exitCode: string;

  exitMessage: string;

  constructor(obj?: any) {
    super(obj);
    this.jobInstanceID				= obj && obj.jobInstanceID	 || -100;
    this.createTime						= obj && obj.createTime			 || new Date();
    this.startTime			      = obj && obj.startTime		   || new Date;
    this.endTime						  = obj && obj.endTime				 || new Date;
    this.status						    = obj && obj.status					 || "???";
    this.exitCode						  = obj && obj.status					 || "";
    this.exitMessage				  = obj && obj.exitMessage		 || "";
  }

}
