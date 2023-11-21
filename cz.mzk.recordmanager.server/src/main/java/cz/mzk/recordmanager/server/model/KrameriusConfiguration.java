package cz.mzk.recordmanager.server.model;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = KrameriusConfiguration.TABLE_NAME)
@PrimaryKeyJoinColumn(name="import_conf_id")
public class KrameriusConfiguration extends ImportConfiguration {

	public static final String TABLE_NAME = "kramerius_conf";

	@Column(name = "import_conf_id", updatable = false, insertable = false)
	private Long importConfId;

	@Column(name="url")
	private String url;

	@Column(name="url_solr")
	private String urlSolr;

	@Column(name="query_rows")
	private Long queryRows;

	@Column(name="metadata_stream")
	private String metadataStream;

	@Column(name="auth_token")
	private String authToken;

	@Column(name="download_private_fulltexts")
	private boolean downloadPrivateFulltexts;

	@Column(name="fulltext_harvest_type")
	private String fulltextHarvestType = "fedora";

	@Column(name="harvest_job_name")
	private String harvestJobName;

	@Column(name = "collection")
	private String collection;

	@Column(name = "availability_source_url")
	private String availabilitySourceUrl;

	@Column(name = "availability_dest_url")
	private String availabilityDestUrl;

	@Column(name = "dnnt_dest_url")
	private String dnntDestUrl;

	@Column(name = "fulltext_version")
	private String fulltextVersion;

	@Column(name = "dedup_fulltext")
	private boolean dedupFulltext;

	@Column(name = "harvest_periodical_fulltext")
	private boolean harvestPeriodicalFulltext;

	@Type(
			type = "cz.mzk.recordmanager.server.hibernate.CharEnumUserType",
			parameters = {
					@Parameter(name = "enumClassName", value = "cz.mzk.recordmanager.server.model.HarvestFrequency"),
			}
	)
	@Column(name = "availability_harvest_frequency")
	private HarvestFrequency availabilityHarvestFrequency = HarvestFrequency.UNSPECIFIED;

	@Type(
			type="cz.mzk.recordmanager.server.hibernate.CharEnumUserType",
			parameters= {
					@Parameter(name="enumClassName", value="cz.mzk.recordmanager.server.model.HarvestFrequency"),
			}
	)
	@Column(name = "fulltext_harvest_frequency")
	private HarvestFrequency fulltextHarvestFrequency = HarvestFrequency.UNSPECIFIED;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrlSolr() {
		return urlSolr;
	}

	public void setUrlSolr(String urlSolr) {
		this.urlSolr = urlSolr;
	}

	public Long getQueryRows() {
		return queryRows;
	}

	public void setQueryRows(Long queryRows) {
		this.queryRows = queryRows;
	}

	public String getMetadataStream() {
		return metadataStream;
	}

	public void setMetadataStream(String metadataStream) {
		this.metadataStream = metadataStream;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public boolean isDownloadPrivateFulltexts() {
		return downloadPrivateFulltexts;
	}

	public void setDownloadPrivateFulltexts(boolean downloadPrivateFulltexts) {
		this.downloadPrivateFulltexts = downloadPrivateFulltexts;
	}

	public String getFulltextHarvestType() {
		return fulltextHarvestType;
	}

	public void setFulltextHarvestType(String fulltextHarvestType) {
		this.fulltextHarvestType = fulltextHarvestType;
	}

	public String getHarvestJobName() {
		return harvestJobName;
	}

	public void setHarvestJobName(String harvestJobName) {
		this.harvestJobName = harvestJobName;
	}

	public String getCollection() {
		return collection;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}

	public String getAvailabilitySourceUrl() {
		return (availabilitySourceUrl == null) ? url : availabilitySourceUrl;
	}

	public void setAvailabilitySourceUrl(String availabilitySourceUrl) {
		this.availabilitySourceUrl = availabilitySourceUrl;
	}

	public String getAvailabilityDestUrl() {
		return availabilityDestUrl;
	}

	public void setAvailabilityDestUrl(String availabilityDestUrl) {
		this.availabilityDestUrl = availabilityDestUrl;
	}

	public HarvestFrequency getAvailabilityHarvestFrequency() {
		return availabilityHarvestFrequency;
	}

	public void setAvailabilityHarvestFrequency(HarvestFrequency availabilityHarvestFrequency) {
		this.availabilityHarvestFrequency = availabilityHarvestFrequency;
	}

	public String getDnntDestUrl() {
		return dnntDestUrl;
	}

	public void setDnntDestUrl(String dnntDestUrl) {
		this.dnntDestUrl = dnntDestUrl;
	}

	public HarvestFrequency getFulltextHarvestFrequency() {
		return fulltextHarvestFrequency;
	}

	public void setFulltextHarvestFrequency(HarvestFrequency fulltextHarvestFrequency) {
		this.fulltextHarvestFrequency = fulltextHarvestFrequency;
	}

	public String getFulltextVersion() {
		return fulltextVersion;
	}

	public void setFulltextVersion(String fulltextVersion) {
		this.fulltextVersion = fulltextVersion;
	}

	public boolean isDedupFulltext() {
		return dedupFulltext;
	}

	public void setDedupFulltext(boolean dedupFulltext) {
		this.dedupFulltext = dedupFulltext;
	}

	public boolean isHarvestPeriodicalFulltext() {
		return harvestPeriodicalFulltext;
	}

	public void setHarvestPeriodicalFulltext(boolean harvestPeriodicalFulltext) {
		this.harvestPeriodicalFulltext = harvestPeriodicalFulltext;
	}

	@Override
	public String toString() {
		return String.format("%s[id=%s, url='%s', urlSolr='%s']", this.getClass().getSimpleName(),
				this.getId(), this.getUrl(), this.getUrlSolr());
	}

}
