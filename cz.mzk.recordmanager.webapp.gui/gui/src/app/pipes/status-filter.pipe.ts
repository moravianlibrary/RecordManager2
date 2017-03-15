import { Pipe, PipeTransform } from '@angular/core';
import {OaiHarvestJobStatistics} from "../model/oai-harvest-job-statistics";

@Pipe({
  name: 'statusFilter',
  pure: false
})
export class StatusFilterPipe implements PipeTransform {

  transform(statistics: OaiHarvestJobStatistics[], statuses: string[]): OaiHarvestJobStatistics[] {
    var filteredStatistics: OaiHarvestJobStatistics[] = [];

    if (statistics != null){

      statistics.forEach(item => {
        if (statuses != null && statuses.length > 0){

          if (statuses.indexOf(item.status.toUpperCase()) != -1){
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
