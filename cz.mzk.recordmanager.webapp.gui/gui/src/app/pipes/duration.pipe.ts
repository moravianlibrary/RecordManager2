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

			let days = Math.floor(difference.getTime() / (24*60*60*1000));
			let daysms = difference.getTime() % (24*60*60*1000);
			let hours = Math.floor((daysms)/(60*60*1000));
			let hoursms = difference.getTime() % (60*60*1000);
			let minutes = Math.floor((hoursms)/(60*1000));
			let minutesms = difference.getTime() % (60*1000);
			let seconds = Math.floor((minutesms)/(1000));

			timestamp = days + "d " + hours + "h " + minutes + "m " + seconds + "s";
		}

		return timestamp;
	}

}