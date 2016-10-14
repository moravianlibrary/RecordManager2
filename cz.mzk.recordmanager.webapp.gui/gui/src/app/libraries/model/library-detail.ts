import {Library} from "./library";
import {OaiHarvestConfiguration} from "./oai-harvest-configuration";

export class LibraryDetail extends Library {

	oaiHarvestConfigurations: OaiHarvestConfiguration[];

	constructor(obj?: any)
	{
		super(obj);
		this.oaiHarvestConfigurations	 =	 obj && obj.oaiHarvestConfigurations	 || null;
	}
}
