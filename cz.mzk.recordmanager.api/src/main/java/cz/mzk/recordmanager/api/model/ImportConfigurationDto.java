package cz.mzk.recordmanager.api.model;

/**
 * Created by sergey on 10/31/16.
 */
public class ImportConfigurationDto extends IdDto {
	private LibraryDto library;

	private String idPrefix;

	public LibraryDto getLibrary() {
		return library;
	}

	public void setLibrary(LibraryDto library) {
		this.library = library;
	}

	public String getIdPrefix() {
		return idPrefix;
	}

	public void setIdPrefix(String idPrefix) {
		this.idPrefix = idPrefix;
	}
}
