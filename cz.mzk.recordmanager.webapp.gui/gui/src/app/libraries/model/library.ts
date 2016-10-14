import any = jasmine.any;
/**
 * Class represents library.
 */
export class Library
{
	id: number;
	name: string;
	url: string;
	catalogUrl: string;
	city: string;

	constructor(obj?: any)
	{
		this.id							 = obj && obj.id						 || null;
		this.name						 = obj && obj.name					 || null;
		this.url							= obj && obj.url						|| null;
		this.catalogUrl			 = obj && obj.catalogUrl		 || null;
		this.city						 = obj && obj.city					 || null;
	}

}
