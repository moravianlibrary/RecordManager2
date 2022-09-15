package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class NkcEbookMetadataMarcRecord extends MetadataMarcRecord {

	public NkcEbookMetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public List<Issn> getISSNs() {
		return Collections.emptyList();
	}

	@Override
	public List<Cnb> getCNBs() {
		return Collections.emptyList();
	}

	@Override
	public String getISSNSeries() {
		return null;
	}

	@Override
	public Long getPublicationYear() {
		return null;
	}

	@Override
	public List<Title> getTitle() {
		return Collections.emptyList();
	}

	@Override
	public List<HarvestedRecordFormat.HarvestedRecordFormatEnum> getDetectedFormatList() {
		return Collections.emptyList();
	}

	@Override
	public String getUUId() {
		return null;
	}

	@Override
	public String getAuthorAuthKey() {
		return null;
	}

	@Override
	public String getAuthorString() {
		return null;
	}

	@Override
	public String getClusterId() {
		return null;
	}

	@Override
	public List<Oclc> getOclcs() {
		return Collections.emptyList();
	}

	@Override
	public List<String> getLanguages() {
		return Collections.emptyList();
	}

	@Override
	public String getRaw001Id() {
		return null;
	}

	@Override
	public List<Ismn> getISMNs() {
		return Collections.emptyList();
	}

	@Override
	public List<Ean> getEANs() {
		return Collections.emptyList();
	}

	@Override
	public List<ShortTitle> getShortTitles() {
		return Collections.emptyList();
	}

	@Override
	public List<PublisherNumber> getPublisherNumber() {
		return Collections.emptyList();
	}

	@Override
	public String getSourceInfoXZ() {
		return null;
	}

	@Override
	public String getSourceInfoT() {
		return null;
	}

	@Override
	public String getSourceInfoG() {
		return null;
	}

	@Override
	public String getPublisher() {
		return null;
	}

	@Override
	public String getEdition() {
		return null;
	}

	@Override
	public Set<AnpTitle> getAnpTitle() {
		return Collections.emptySet();
	}

	@Override
	public List<Uuid> getUuids() {
		return Collections.emptyList();
	}

}
