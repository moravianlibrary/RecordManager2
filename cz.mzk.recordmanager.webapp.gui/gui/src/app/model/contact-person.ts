import {Id} from "./id";
export class ContactPerson extends Id{
  name: string;
  email: string;
  phone: string;

  constructor(obj?: any) {
    super(obj);
    this.name        = obj && obj.name           || null;
    this.email       = obj && obj.email          || null;
    this.phone       = obj && obj.phone          || null;
  }
}
