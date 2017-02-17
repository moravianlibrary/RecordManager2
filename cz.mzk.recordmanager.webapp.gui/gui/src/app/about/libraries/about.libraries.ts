import {Component, OnInit} from "@angular/core";
@Component({
	selector: "app-about-libraries",
	templateUrl: './about.libraries.html',
	styleUrls: ['./about.libraries.css']
})
export class AboutLibrariesComponent implements OnInit{
	imagesRoot : string = "../../../images";
	addLibrary : string = this.imagesRoot + "/AddLibrary.png";
	addLibraryForm : string = this.imagesRoot + "/AddLibraryForm.png";
	ngOnInit(): void {
	}

}