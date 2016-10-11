import {ContactPerson} from "./contact-person";
export class OaiHarvestConfiguration {
  id: number;
  url: string ;
  set: string;
  metadataPrefix: string;
  contactPerson: ContactPerson;

  constructor(obj?: any)
  {
    this.id                  = obj && obj.id                 || null;
    this.url                 = obj && obj.url                || null;
    this.set                 = obj && obj.set                || null;
    this.metadataPrefix      = obj && obj.metadataPrefix     || null;
    this.contactPerson       = obj && obj.contactPerson      || null;
  }


}
