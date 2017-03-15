import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  pure: false,
  name: 'typeahead'
})
export class TypeaheadPipe implements PipeTransform {

  transform(value: any, filterVal: string): any {
    var options: any[] = [];

    if (value != null){
      value.forEach(item => {

        let aux = item.value.replace("\s+", " ").split(" ");

        for(let index = 0; index < aux.length; ++index){

          if (filterVal.length > 0 && aux[index].toLowerCase().startsWith(filterVal.toLowerCase())){
            options.push(item);
            break;
          }
        }
      })
    }
    if (filterVal.length <= 0){
      options = value;
    }
    return options;
  }

}
