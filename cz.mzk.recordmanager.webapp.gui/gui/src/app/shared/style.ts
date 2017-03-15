export class Style{
  get visibility(): string {
    return this._visibility;
  }

  set visibility(value: string) {
    this._visibility = value;
  }
  get arrow(): string {
    return this._arrow;
  }

  set arrow(value: string) {
    this._arrow = value;
  }

  switchArrow(){
    this._arrow = this._arrow == "glyphicon-arrow-up" ? "glyphicon glyphicon-arrow-down" : "glyphicon-arrow-up";
  }

  switchVisibility(){
    this._visibility = this._visibility == "invisible" ? "visible" : "invisible";
  }

  constructor(obj?: any) {
    this._arrow       = obj && obj._arrow    ||  "glyphicon-arrow-up";
    this._visibility  = obj && obj._arrow    ||  "invisible";
  }

  private _arrow: string;
  private _visibility: string;

}
