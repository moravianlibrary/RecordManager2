import {Component, OnInit, Output, EventEmitter, Input} from '@angular/core';

@Component({
  selector: 'app-datetime-picker',
  templateUrl: './datetime-picker.component.html',
  styleUrls: ['./datetime-picker.component.css']
})
export class DatetimePickerComponent implements OnInit {

  @Output() emitDate = new EventEmitter<Date>();

  @Input() description: string = "";

  refDate: Date;

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

  daysOfMonth: any[];

  pickedDate: Date;

  constructor() { }

  nextYear(offset: number){
    this.refDate = new Date(this.refDate.getFullYear() + offset, this.refDate.getMonth(), 1);
  }

  nextMonth(offset: number){
    this.refDate = new Date(this.refDate.getFullYear() , this.refDate.getMonth() + offset, 1);
  }

  setDaysOfMonth(){
    var calendarMatrix: any[] = [];
    var lastDay = new Date(this.refDate.getFullYear(), this.refDate.getMonth() + 1, 0);
    var dayIndex = 1;

    var i = 0;
    while(dayIndex <= lastDay.getDate()){
      calendarMatrix.push(new Array(7));
      for (let j = 0; j < this.weekday.length && dayIndex <= lastDay.getDate(); ++j){
        let day = new Date(this.refDate.getFullYear(), this.refDate.getMonth(), dayIndex).getDay() - 1 < 0 ? this.weekday.length - 1 : new Date(this.refDate.getFullYear(), this.refDate.getMonth(), dayIndex).getDay() - 1;

        if (day == j){
          let aux = this.pickedDate == null ? null : new Date(this.pickedDate.getFullYear(), this.pickedDate.getMonth(), this.pickedDate.getDate());
          let now = new Date(this.refDate.getFullYear(), this.refDate.getMonth(), dayIndex);
          let selected = false;
          if (aux != null)
            selected = now.getTime() === aux.getTime();
          calendarMatrix[i][j] = {day: now, isSelected: selected};
          dayIndex = dayIndex + 1;
        }
      }
      ++i;
    }
    this.daysOfMonth = calendarMatrix;
  }

  selectDate(date: Date){
    this.pickedDate = date == null ? null : date;
    this.emitDate.emit(this.pickedDate);
    this.setDaysOfMonth();
  }

  nextHour(offset: number){
    this.pickedDate = new Date(this.pickedDate.getFullYear(), this.pickedDate.getMonth(), this.pickedDate.getDate(), this.pickedDate.getHours() + offset, this.pickedDate.getMinutes());
    this.setDaysOfMonth();
  }
  nextMinute(offset: number){
    this.pickedDate = new Date(this.pickedDate.getFullYear(), this.pickedDate.getMonth(), this.pickedDate.getDate(), this.pickedDate.getHours(), this.pickedDate.getMinutes() + offset);
    this.setDaysOfMonth();
  }

  ngOnInit() {
    this.refDate = new Date();
    this.setDaysOfMonth();
  }

  today(){
    var today = new Date(Date.now());
    this.selectDate(today);
    this.refDate = this.pickedDate;
    this.setDaysOfMonth();
  }

}
