import {ContactPerson} from "./contact-person";
export class OaiHarvestConfiguration {
	id: number;
	contact: ContactPerson;
	idPrefix: string;
	baseWeight: number;
	clusterIdEnabled: boolean;
	filteringEnabled: boolean;
	interceptionEnabled: boolean;
	url: string ;
	extractIdRegex: string;
	harvestJobName: string;
	metadataPrefix: string;
	set: string;
	library: boolean;


	constructor(obj?: any) {
		this.id									= obj && obj.id										|| null;
		this.url								 = obj && obj.url									 || null;
		this.set								 = obj && obj.set									 || null;
		this.metadataPrefix			= obj && obj.metadataPrefix				|| null;
		this.contact						 = obj && obj.contact							 || new ContactPerson();
		this.idPrefix						= obj && obj.idPrefix							|| null;
		this.baseWeight					= obj && obj.baseWeight						|| 0;
		this.clusterIdEnabled		= obj && obj.clusterIdEnabled			|| false;
		this.filteringEnabled		= obj && obj.filteringEnabled			|| false;
		this.interceptionEnabled = obj && obj.interceptionEnabled	 || false;
		this.extractIdRegex			= obj && obj.extractIdRegex				|| null;
		this.harvestJobName			= obj && obj.harvestJobName				|| null;
		this.library						 = obj && obj.library							 || false;
	}


}
