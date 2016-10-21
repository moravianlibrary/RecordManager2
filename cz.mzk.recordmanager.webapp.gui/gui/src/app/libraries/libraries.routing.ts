import {LibrariesComponent} from "./libraries.component";
import {ModuleWithProviders} from "@angular/core";
import {RouterModule} from "@angular/router";
import {LibraryComponent} from "./library/library.component";

const librariesRoutes = [
  {
    path: 'library',
    component: LibrariesComponent,
  },
  {
    path: "library/:id",
    component: LibraryComponent
  },
  {
    path: '',
    redirectTo: 'library',
    pathMatch: 'full'
  }

];
export const librariesRouting : ModuleWithProviders = RouterModule.forChild(librariesRoutes);
