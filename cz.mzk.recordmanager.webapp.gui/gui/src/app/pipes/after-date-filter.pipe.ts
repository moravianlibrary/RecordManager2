import { Pipe, PipeTransform } from '@angular/core';
import {OaiHarvestJobStatistics} from "../model/oai-harvest-job-statistics";
import {stat} from "fs";

@Pipe({
  name: 'afterDateFilter'
})
export class AfterDateFilterPipe implements PipeTransform {

  transform(statistics: OaiHarvestJobStatistics[], after: string): OaiHarvestJobStatistics[] {
    var filteredStatistics: OaiHarvestJobStatistics[] = [];
    var afterDate = new Date(after);


    if (statistics != null){
      statistics.forEach(item => {

        if (afterDate != null && !isNaN(afterDate.getTime())){

          let startedDate = new Date(item.startTime);


          if (item.startTime != null && !isNaN(startedDate.getTime()) && afterDate <= startedDate){
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
