import { Pipe, PipeTransform } from '@angular/core';
import {OaiHarvestJobStatistics} from "../model/oai-harvest-job-statistics";

@Pipe({
  name: 'statusFilter'
})
export class StatusFilterPipe implements PipeTransform {

  transform(statistics: OaiHarvestJobStatistics[], status: string): OaiHarvestJobStatistics[] {
    var filteredStatistics: OaiHarvestJobStatistics[] = [];

    if (statistics != null){
      statistics.forEach(item => {
        if (status != null && status.length > 0){
          if (item.status.toLowerCase() == status){
            filteredStatistics.push(item);
          }
        }else{
          filteredStatistics.push(item);
        }
      })
    }
    return filteredStatistics;
  }

}
