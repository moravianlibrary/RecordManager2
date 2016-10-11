/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { LibrariesService } from './libraries.service';

describe('Service: Libraries', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [LibrariesService]
    });
  });

  it('should ...', inject([LibrariesService], (service: LibrariesService) => {
    expect(service).toBeTruthy();
  }));
});
