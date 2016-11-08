import { Pipe, PipeTransform } from '@angular/core';
import {OaiHarvestJobStatistics} from "../model/oai-harvest-job-statistics";

@Pipe({
  name: 'beforeDateFilter'
})
export class BeforeDateFilterPipe implements PipeTransform {

  transform(statistics: OaiHarvestJobStatistics[], before: string): OaiHarvestJobStatistics[] {
    var filteredStatistics: OaiHarvestJobStatistics[] = [];
    var beforeDate = new Date(before);


    if (statistics != null){
      statistics.forEach(item => {

        if (beforeDate != null && !isNaN(beforeDate.getTime())){

          let startedDate = new Date(item.startTime);

          if (item.startTime != null && !isNaN(startedDate.getTime()) && beforeDate >= startedDate){
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
