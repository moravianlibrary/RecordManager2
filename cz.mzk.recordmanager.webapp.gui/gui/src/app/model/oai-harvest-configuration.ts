import {ImportConfig} from "./import-config";
export class OaiHarvestConfiguration extends ImportConfig{
	baseWeight: number;
	clusterIdEnabled: boolean;
	filteringEnabled: boolean;
	interceptionEnabled: boolean;
	url: string ;
	extractIdRegex: string;
	harvestJobName: string;
	metadataPrefix: string;
	set: string;



	constructor(obj?: any) {
	  super(obj);
		this.url								 = obj && obj.url									  || null;
		this.set								 = obj && obj.set									  || null;
		this.metadataPrefix			 = obj && obj.metadataPrefix				|| null;
		this.idPrefix						 = obj && obj.idPrefix							|| null;
		this.baseWeight					 = obj && obj.baseWeight						|| 0;
		this.clusterIdEnabled		 = obj && obj.clusterIdEnabled			|| false;
		this.filteringEnabled		 = obj && obj.filteringEnabled			|| false;
		this.interceptionEnabled = obj && obj.interceptionEnabled	  || false;
		this.extractIdRegex			 = obj && obj.extractIdRegex				|| null;
		this.harvestJobName			 = obj && obj.harvestJobName				|| null;
	}


}
