package cz.mzk.recordmanager.api.model;

import java.io.Serializable;

/**
 * Created by sergey on 10/13/16.
 */
public class ImportConfigurationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private ContactPersonDto contact;

    private String idPrefix;

    private Long baseWeight;

    private boolean clusterIdEnabled;

    private boolean filteringEnabled;

    private boolean interceptionEnabled;

    private boolean isLibrary;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ContactPersonDto getContact() {
        return contact;
    }

    public void setContact(ContactPersonDto contact) {
        this.contact = contact;
    }

    public String getIdPrefix() {
        return idPrefix;
    }

    public void setIdPrefix(String idPrefix) {
        this.idPrefix = idPrefix;
    }

    public Long getBaseWeight() {
        return baseWeight;
    }

    public void setBaseWeight(Long baseWeight) {
        this.baseWeight = baseWeight;
    }

    public boolean isClusterIdEnabled() {
        return clusterIdEnabled;
    }

    public void setClusterIdEnabled(boolean clusterIdEnabled) {
        this.clusterIdEnabled = clusterIdEnabled;
    }

    public boolean isFilteringEnabled() {
        return filteringEnabled;
    }

    public void setFilteringEnabled(boolean filteringEnabled) {
        this.filteringEnabled = filteringEnabled;
    }

    public boolean isInterceptionEnabled() {
        return interceptionEnabled;
    }

    public void setInterceptionEnabled(boolean interceptionEnabled) {
        this.interceptionEnabled = interceptionEnabled;
    }

    public boolean isLibrary() {
        return isLibrary;
    }

    public void setLibrary(boolean library) {
        isLibrary = library;
    }
}
