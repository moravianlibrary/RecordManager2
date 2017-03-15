package cz.mzk.recordmanager.api.model.statistics;


import cz.mzk.recordmanager.api.model.LibraryDto;
import cz.mzk.recordmanager.api.model.PeriodDto;

import java.util.List;

public class PeriodsAndLibrariesDto {
    private PeriodDto startEnd;
    private PeriodDto fromTo;
    private List<LibraryDto> libraries;

    public PeriodDto getStartEnd() {
        return startEnd;
    }

    public void setStartEnd(PeriodDto startEnd) {
        this.startEnd = startEnd;
    }

    public PeriodDto getFromTo() {
        return fromTo;
    }

    public void setFromTo(PeriodDto fromTo) {
        this.fromTo = fromTo;
    }

    public List<LibraryDto> getLibraries() {
        return libraries;
    }

    public void setLibraries(List<LibraryDto> libraries) {
        this.libraries = libraries;
    }
}
