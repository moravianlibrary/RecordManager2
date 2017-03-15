import {Library} from "./library";
import {Id} from "./id";
import {ContactPerson} from "./contact-person";
export class ImportConfig extends Id{
  library: Library;
  idPrefix: string;
  contact: ContactPerson;
  thisLibrary: boolean;

  constructor(obj?: any) {
    super(obj);
    this.contact               = obj && obj.contact        || new ContactPerson();
    this.library					  	 = obj && obj.library		     || new Library();
    this.idPrefix              = obj && obj.idPrefix       ||   "";
    this.thisLibrary           = obj && obj.thisLibrary    ||   false;
  }

}
