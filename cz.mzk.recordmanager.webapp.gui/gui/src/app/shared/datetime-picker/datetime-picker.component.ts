import {Component, OnInit, Output, EventEmitter, Input} from '@angular/core';

@Component({
  selector: 'app-datetime-picker',
  templateUrl: './datetime-picker.component.html',
  styleUrls: ['./datetime-picker.component.css']
})
export class DatetimePickerComponent implements OnInit {

  @Output() emitDate = new EventEmitter<Date>();

  @Input() description: string = "";

  today: Date = new Date();

  refDate: Date = new Date(this.today.getFullYear(), this.today.getMonth(), 1);

  month: Date;
  weekday: string[] = [
    "Mon",
    "Tue",
    "Wed",
    "Thu",
    "Fri",
    "Sat",
    "Sun",

];


  pickedDate: Date;

  constructor() { }

  nextYear(offset: number){
    this.refDate = new Date(this.refDate.getFullYear() + offset, this.refDate.getMonth(), 1);
  }

  nextMonth(offset: number){
    this.refDate = new Date(this.refDate.getFullYear() , this.refDate.getMonth() + offset, 1);
  }

  getDaysOfMonth(): any[]{
    var calendarMatrix: any[] = [];
    var lastDay = new Date(this.refDate.getFullYear(), this.refDate.getMonth() + 1, 0);
    var monthIndex = 1;
    for(let i = 0; i < Math.ceil(lastDay.getDate() / 7); ++i){
      calendarMatrix.push(new Array(7));

      for (let j = 0; j < this.weekday.length && monthIndex <= lastDay.getDate(); ++j){

        let day = new Date(this.refDate.getFullYear(), this.refDate.getMonth(), monthIndex).getDay() - 1 < 0 ? this.weekday.length - 1 : new Date(this.refDate.getFullYear(), this.refDate.getMonth(), monthIndex).getDay() - 1;

        if (day == j){
          calendarMatrix[i][j] = new Date(this.refDate.getFullYear(), this.refDate.getMonth(), monthIndex);
          monthIndex = monthIndex + 1;
        }

      }
    }
    return calendarMatrix;
  }

  selectDate(date: Date){
    this.pickedDate = date;
    this.emitDate.emit(this.pickedDate);
  }
  ngOnInit() {
  }

}
