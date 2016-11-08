import { Pipe, PipeTransform } from '@angular/core';
import {ImportConfig} from "../model/import-config";

@Pipe({
  name: 'autocomplete'
})
export class AutocompletePipe implements PipeTransform {

  transform(importConfig: ImportConfig[], startWith: string): any {
    var desiredConfigs: ImportConfig[] = [];
    importConfig.forEach(conf => {
      if (conf.idPrefix != null && conf.idPrefix.match("^" + startWith + ".*"))
        desiredConfigs.push(conf);
    });
    return desiredConfigs;
  }

}
