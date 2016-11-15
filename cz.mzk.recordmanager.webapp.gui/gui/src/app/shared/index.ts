import {NgModule} from "@angular/core";
import {OrderByPipe} from "../pipes/order-by.pipe";
import {ErrorPageComponent} from "./error-page/error-page.component";
import {errorsRouting} from "./error.routing";
import {KeyExtractorPipe} from "../pipes/key-extractor.pipe";

@NgModule({
  declarations: [OrderByPipe, ErrorPageComponent, KeyExtractorPipe],
  imports: [errorsRouting],
  exports: [OrderByPipe, ErrorPageComponent, KeyExtractorPipe]
})
export class IndexModule{}
