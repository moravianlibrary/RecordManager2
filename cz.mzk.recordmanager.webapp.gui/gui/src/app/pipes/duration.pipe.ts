import {Pipe, PipeTransform} from "@angular/core";
@Pipe({
	name: "durationPipe",
	pure: true
})
export class DurationPipe implements PipeTransform{
	transform(value: any, ...args: any[]): any {
		let timestamp = "";
		let difference = new Date(value);
		if (difference.getTime() > 0){
			let days = difference.getTime() / (1000*60*60*24);
			let hours = (days - Math.floor(days)) * 24;
			let minutes = (hours - Math.floor(hours)) * 60;
			let seconds = (minutes - Math.floor(minutes)) * 60;

			timestamp = Math.floor(days) + "d " + Math.floor(hours) + "h " + Math.floor(minutes) + "m " + Math.floor(seconds) + "s";
		}

		return timestamp;
	}

}