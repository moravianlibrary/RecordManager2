import {Field} from "./Field";
import {Injectable} from "@angular/core";
@Injectable()
export class SortControl{
  public sortByMe(name: string, fields: Field[]): string{

  fields.forEach(field => {
      if (field.style.visibility == 'visible'){
        field.style.switchVisibility();
      }
    });
    var me = fields.filter(field => field.name == name)[0];

    me.style.switchArrow();
    me.style.switchVisibility();

    return me.style.arrow == "sortArrowUp" ? "+" + name : "-" + name;
  }

  public getArrow(name: string, fields: Field[]): string{
    return fields.filter(field => field.name == name)[0].style.arrow;
  }
  public getVisibility(name: string, fields: Field[]): string{
    return fields.filter(field => field.name == name)[0].style.visibility;
  }
}

