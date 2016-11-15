import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'keyExtractor',
  pure: true
})
export class KeyExtractorPipe implements PipeTransform {

  transform(value: any, args?: any): any {
    let keys: string[] = [];
    for (let key in value){
      keys.push(key);
    }
    return keys;
  }

}
