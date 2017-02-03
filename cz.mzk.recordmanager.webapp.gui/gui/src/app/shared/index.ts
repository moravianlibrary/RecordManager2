import {NgModule} from "@angular/core";
import {OrderByPipe} from "../pipes/order-by.pipe";
import {ErrorPageComponent} from "./error-page/error-page.component";
import {errorsRouting} from "./error.routing";
import {KeyExtractorPipe} from "../pipes/key-extractor.pipe";
import {DurationPipe} from "../pipes/duration.pipe";

@NgModule({
  declarations: [OrderByPipe, ErrorPageComponent, KeyExtractorPipe, DurationPipe],
  imports: [errorsRouting],
  exports: [OrderByPipe, ErrorPageComponent, KeyExtractorPipe, DurationPipe]
})
export class IndexModule{}
