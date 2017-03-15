import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'keyExtractor',
  pure: true
})
export class KeyExtractorPipe implements PipeTransform {

  transform(value: any, args?: any): any {
    let keys: any[] = [];
    for (let key in value){
      keys.push({k: key, v: value[key]});
    }
    return keys;
  }

}
