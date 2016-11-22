import {Component, OnInit, Input, Output, EventEmitter} from '@angular/core';

@Component({
  selector: 'app-multi-select',
  templateUrl: './multi-select.component.html',
  styleUrls: ['./multi-select.component.css']
})
export class MultiSelectComponent implements OnInit {

  @Input() options: any[];

  @Input() placeholder: string;

  @Input() itemsAmount: number;

  @Input() multiSelectContent: string = "Choose the following ...";

  @Output() onSelect = new EventEmitter<any[]>();

  inputValue: string = "";

  selected: any[] = [];

  constructor() { }

  selectMe(item: any){
    if (!this.amISelected(item))
      this.selected.push(item);
    else
      this.selected.splice(this.selected.indexOf(item), 1);

    if (this.selected.length == 0){
        this.multiSelectContent = "Choose the following ...";
    }else{
      if (this.selected.length <= this.itemsAmount){
        this.multiSelectContent = "";
        this.selected.forEach(sel => {
          this.multiSelectContent = sel.value + " " + this.multiSelectContent;
        });
      }else{
        this.multiSelectContent = "Selected " + this.selected.length + " items";
      }
    }

    this.onSelect.emit(this.selected);
  }




  amISelected(item: any): boolean{
    return this.selected.find(obj => obj.id === item.id && obj.value === item.value) != null;
  }
  ngOnInit() {
  }

}
