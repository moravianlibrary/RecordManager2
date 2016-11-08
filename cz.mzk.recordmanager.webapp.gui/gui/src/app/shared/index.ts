import {NgModule} from "@angular/core";
import {OrderByPipe} from "../pipes/order-by.pipe";
import {ErrorPageComponent} from "./error-page/error-page.component";
import {errorsRouting} from "./error.routing";

@NgModule({
  declarations: [OrderByPipe, ErrorPageComponent],
  imports: [errorsRouting],
  exports: [OrderByPipe, ErrorPageComponent]
})
export class IndexModule{}
