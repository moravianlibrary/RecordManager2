import {Library} from "./library";
import {OaiHarvestConfiguration} from "./oai-harvest-configuration";
import {ImportConfig} from "./import-config";

export class LibraryDetail extends Library {

	oaiHarvestConfigurations: ImportConfig[];

	constructor(obj?: any)
	{
		super(obj);
		this.oaiHarvestConfigurations	 =	 obj && obj.oaiHarvestConfigurations	 || null;
	}
}
