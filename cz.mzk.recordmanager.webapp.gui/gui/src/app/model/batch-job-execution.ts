export class BatchJobExecution {

  id: number;

  jobInstanceID: number;

  createTime: Date;

  startTime: Date;

  endTime: Date;

  status: string;

  exitCode: string;

  exitMessage: string;

  constructor(obj?: any) {
    this.id							      = obj && obj.id						   || null;
    this.jobInstanceID				= obj && obj.jobInstanceID	 || -100;
    this.createTime						= obj && obj.createTime			 || new Date();
    this.startTime			      = obj && obj.startTime		   || new Date;
    this.endTime						  = obj && obj.endTime				 || new Date;
    this.status						    = obj && obj.status					 || "???";
    this.exitCode						  = obj && obj.status					 || "???";
    this.exitMessage				  = obj && obj.exitMessage		 || "???";
  }

}
