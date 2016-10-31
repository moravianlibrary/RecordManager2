import {Field} from "./Field";
export abstract class SortControl{
  public static sortByMe(name: string, fields: Field[]): string{

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

  public static getArrow(name: string, fields: Field[]): string{
    return fields.filter(field => field.name == name)[0].style.arrow;
  }
  public static getVisibility(name: string, fields: Field[]): string{
    return fields.filter(field => field.name == name)[0].style.visibility;
  }
}

