import {Component, OnInit, Output, EventEmitter, Input} from '@angular/core';

@Component({
  selector: 'app-datetime-picker',
  templateUrl: './datetime-picker.component.html',
  styleUrls: ['./datetime-picker.component.css']
})
export class DatetimePickerComponent implements OnInit {

  @Output() emitDate = new EventEmitter<Date>();

  @Input() description: string = "";

  @Input() initDate;

  refDate: Date;

  isTimeChooser: boolean = false;

  isHourSelection: boolean = true;
  isMinuteSelection: boolean = false;

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

    return calendarMatrix;
  }

  selectDate(date: Date){
    this.pickedDate = date == null ? null : date;
    this.emitDate.emit(this.pickedDate);
  }

  nextHour(offset: number){
    this.pickedDate = new Date(this.pickedDate.getFullYear(), this.pickedDate.getMonth(), this.pickedDate.getDate(), this.pickedDate.getHours() + offset, this.pickedDate.getMinutes());
    this.refDate = this.pickedDate;

  }
  nextMinute(offset: number){
    this.pickedDate = new Date(this.pickedDate.getFullYear(), this.pickedDate.getMonth(), this.pickedDate.getDate(), this.pickedDate.getHours(), this.pickedDate.getMinutes() + offset);
    this.refDate = this.pickedDate;
  }

  ngOnInit() {
    this.refDate = new Date();
    this.pickedDate = this.initDate;
  }

  today(){
    var today = new Date(Date.now());
    this.selectDate(today);
    this.refDate = this.pickedDate;
  }


  getTimeMatrix(): any{
    var timeMatrix = [];
    var step = this.isMinuteSelection ? 5 : 1;
    var periodOfTime = this.isMinuteSelection ? 60 : 24;


    var currentTime = 0;

    // 4 because of max length of the rows is 4
    for (let row = 0; row < periodOfTime / (4 * step); ++row){
      timeMatrix.push(new Array(4));
        for (let col = 0; col <  4; ++col){
          let aux = new Date(this.pickedDate);
          if(this.isMinuteSelection){
            timeMatrix[row][col] = new Date(aux.setMinutes(currentTime));
          }else {
            timeMatrix[row][col] = new Date(aux.setHours(currentTime));
          }

          currentTime += step;
        }
    }

    return timeMatrix;
  }


  setHourSelection(){
    if (this.isTimeChooser  && this.isHourSelection){
      this.isTimeChooser = false;
    }else {
      this.isTimeChooser = true;
    }
    this.isHourSelection = true;
    this.isMinuteSelection = false;
  }

  setMinuteSelection(){
    if (this.isTimeChooser && this.isMinuteSelection){
      this.isTimeChooser = false;
    }else {
      this.isTimeChooser = true;
    }
    this.isHourSelection = false;
    this.isMinuteSelection = true;
  }


}
