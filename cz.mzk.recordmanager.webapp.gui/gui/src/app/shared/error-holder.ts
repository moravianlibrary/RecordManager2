export class ErrorHolder {
  status: number;
  message: string;

  constructor(obj?: any){
    this.status           = obj && obj.status       ||  -100;
    this.message          = obj && obj.message      ||  "";
  }
}
