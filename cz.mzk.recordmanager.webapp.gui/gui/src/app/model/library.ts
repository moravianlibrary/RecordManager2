import any = jasmine.any;
import {Id} from "./id";

export class Library extends Id{
	name: string;
	url: string;
	catalogUrl: string;
	city: string;

	constructor(obj?: any) {
    super(obj);
		this.name						 = obj && obj.name					 || null;
		this.url							= obj && obj.url						|| null;
		this.catalogUrl			 = obj && obj.catalogUrl		 || null;
		this.city						 = obj && obj.city					 || null;
	}

}
