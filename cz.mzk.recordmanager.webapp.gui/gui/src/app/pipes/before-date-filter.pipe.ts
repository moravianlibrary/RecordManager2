import { Pipe, PipeTransform } from '@angular/core';
import {OaiHarvestJobStatistics} from "../model/oai-harvest-job-statistics";

@Pipe({
  name: 'beforeDateFilter'
})
export class BeforeDateFilterPipe implements PipeTransform {

  transform(statistics: OaiHarvestJobStatistics[], before: Date): OaiHarvestJobStatistics[] {
    var filteredStatistics: OaiHarvestJobStatistics[] = [];

    if (statistics != null){
      statistics.forEach(item => {

        if (before != null && !isNaN(before.getTime())){

          let endTime = new Date(item.endTime);

          if (item.endTime != null && !isNaN(endTime.getTime()) && before >= endTime){
            filteredStatistics.push(item);
          }
        }else {
          filteredStatistics.push(item);
        }

      });
    }

    return filteredStatistics;
  }

}
